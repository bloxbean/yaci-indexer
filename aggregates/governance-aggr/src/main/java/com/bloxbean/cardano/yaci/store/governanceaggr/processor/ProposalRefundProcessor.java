package com.bloxbean.cardano.yaci.store.governanceaggr.processor;

import com.bloxbean.cardano.yaci.core.model.governance.GovActionId;
import com.bloxbean.cardano.yaci.store.client.governance.ProposalStateClient;
import com.bloxbean.cardano.yaci.store.common.domain.GovActionProposal;
import com.bloxbean.cardano.yaci.store.common.domain.GovActionStatus;
import com.bloxbean.cardano.yaci.store.events.domain.ProposalStatusCapturedEvent;
import com.bloxbean.cardano.yaci.store.events.domain.RewardRestAmt;
import com.bloxbean.cardano.yaci.store.events.domain.RewardRestEvent;
import com.bloxbean.cardano.yaci.store.events.domain.RewardRestType;
import com.bloxbean.cardano.yaci.store.governance.storage.GovActionProposalStorage;
import com.bloxbean.cardano.yaci.store.governanceaggr.domain.Proposal;
import com.bloxbean.cardano.yaci.store.governanceaggr.storage.impl.mapper.ProposalMapper;
import com.bloxbean.cardano.yaci.store.governanceaggr.util.ProposalUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Component
@Slf4j
public class ProposalRefundProcessor {
    private final GovActionProposalStorage govActionProposalStorage;
    private final ProposalStateClient proposalStateClient;
    private final ProposalMapper proposalMapper;
    private final ApplicationEventPublisher publisher;

    public ProposalRefundProcessor(GovActionProposalStorage govActionProposalStorage, ProposalStateClient proposalStateClient,
                                   ProposalMapper proposalMapper, ApplicationEventPublisher publisher) {
        this.govActionProposalStorage = govActionProposalStorage;
        this.proposalStateClient = proposalStateClient;
        this.proposalMapper = proposalMapper;
        this.publisher = publisher;
    }

    @EventListener
    @Transactional
    public void handleProposalStatusCapturedEvent(ProposalStatusCapturedEvent event) {
        int epoch = event.getEpoch();

        List<Proposal> expiredProposalsInThisEpoch =
                proposalStateClient.getProposalsByStatusAndEpoch(GovActionStatus.EXPIRED, epoch)
                .stream()
                .map(this::mapToProposal)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        List<Proposal> ratifiedProposalsInThisEpoch =
                proposalStateClient.getProposalsByStatusAndEpoch(GovActionStatus.RATIFIED, epoch)
                        .stream()
                        .map(this::mapToProposal)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .toList();

        List<Proposal> proposalListInThisEpoch =
                proposalStateClient.getProposalsByStatusListAndEpoch(List.of(GovActionStatus.ACTIVE, GovActionStatus.RATIFIED, GovActionStatus.EXPIRED), epoch)
                .stream()
                .map(this::mapToProposal)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        List<Proposal> siblingsOrDescendantsBePrunedInNextEpoch = new ArrayList<>();

        for (Proposal proposal : expiredProposalsInThisEpoch) {
            List<Proposal> proposalsBePrunedInNextEpoch = ProposalUtils.findDescendantsAndSiblings(proposal, proposalListInThisEpoch);
            siblingsOrDescendantsBePrunedInNextEpoch.addAll(proposalsBePrunedInNextEpoch);
        }

        for (Proposal proposal : ratifiedProposalsInThisEpoch) {
            List<Proposal> proposalsBePrunedInNextEpoch = ProposalUtils.findDescendantsAndSiblings(proposal, proposalListInThisEpoch);
            siblingsOrDescendantsBePrunedInNextEpoch.addAll(proposalsBePrunedInNextEpoch);
        }

        handleProposalRefund(Stream.concat(siblingsOrDescendantsBePrunedInNextEpoch.stream(),
                Stream.concat(expiredProposalsInThisEpoch.stream(), ratifiedProposalsInThisEpoch.stream())
        ).map(Proposal::getGovActionId).toList(), epoch, event.getSlot());
    }

    private void handleProposalRefund(List<GovActionId> proposalsBeDropped, int earnedEpoch, long slot) {
        if (proposalsBeDropped.isEmpty()) {
            return;
        }

        List<RewardRestAmt> rewardRestAmts = new ArrayList<>();

        var proposals = govActionProposalStorage.findByGovActionIds(proposalsBeDropped);

        for (var proposal : proposals) {
            rewardRestAmts.add(RewardRestAmt.builder()
                    .address(proposal.getReturnAddress())
                    .type(RewardRestType.proposal_refund)
                    .amount(proposal.getDeposit())
                    .build());
        }

        if (!rewardRestAmts.isEmpty()) {
            var rewardRestEvent = RewardRestEvent.builder()
                    .earnedEpoch(earnedEpoch)
                    .spendableEpoch(earnedEpoch + 1)
                    .slot(slot)
                    .rewards(rewardRestAmts)
                    .build();
            publisher.publishEvent(rewardRestEvent);
        }
    }

    private Optional<Proposal> mapToProposal(GovActionProposal govActionProposal) {
        try {
            return Optional.of(proposalMapper.toProposal(govActionProposal));
        } catch (JsonProcessingException e) {
            return Optional.empty();
        }
    }
}
