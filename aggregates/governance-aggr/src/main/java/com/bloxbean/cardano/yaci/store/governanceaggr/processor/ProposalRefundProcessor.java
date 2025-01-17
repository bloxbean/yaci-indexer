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

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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

        List<Proposal> expiredProposalsInThisEpoch = getProposalsByStatusAndEpoch(GovActionStatus.EXPIRED, epoch);
        List<Proposal> ratifiedProposalsInThisEpoch = getProposalsByStatusAndEpoch(GovActionStatus.RATIFIED, epoch);
        List<Proposal> proposalListInThisEpoch = getProposalsByStatusListAndEpoch(
                List.of(GovActionStatus.ACTIVE, GovActionStatus.RATIFIED, GovActionStatus.EXPIRED), epoch);

        Map<GovActionId, Proposal> siblingsOrDescendantsBePrunedInNextEpoch = new HashMap<>();

        expiredProposalsInThisEpoch.forEach(proposal ->
                ProposalUtils.findDescendants(proposal, proposalListInThisEpoch)
                        .forEach(p -> siblingsOrDescendantsBePrunedInNextEpoch.putIfAbsent(p.getGovActionId(), p))
        );

        ratifiedProposalsInThisEpoch.forEach(proposal ->
                ProposalUtils.findDescendantsAndSiblings(proposal, proposalListInThisEpoch)
                        .forEach(p -> siblingsOrDescendantsBePrunedInNextEpoch.putIfAbsent(p.getGovActionId(), p))
        );

        Map<GovActionId, Proposal> droppedProposals = new HashMap<>();

        droppedProposals.putAll(siblingsOrDescendantsBePrunedInNextEpoch);
        droppedProposals.putAll(expiredProposalsInThisEpoch.stream().collect(Collectors.toMap(Proposal::getGovActionId, Function.identity())));
        droppedProposals.putAll(ratifiedProposalsInThisEpoch.stream().collect(Collectors.toMap(Proposal::getGovActionId, Function.identity())));

        handleProposalRefund(droppedProposals.keySet().stream().toList(), epoch, event.getSlot());
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

    private List<Proposal> getProposalsByStatusAndEpoch(GovActionStatus status, int epoch) {
        return proposalStateClient.getProposalsByStatusAndEpoch(status, epoch)
                .stream()
                .map(this::mapToProposal)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    private List<Proposal> getProposalsByStatusListAndEpoch(List<GovActionStatus> statuses, int epoch) {
        return proposalStateClient.getProposalsByStatusListAndEpoch(statuses, epoch)
                .stream()
                .map(this::mapToProposal)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    private Optional<Proposal> mapToProposal(GovActionProposal govActionProposal) {
        try {
            return Optional.of(proposalMapper.toProposal(govActionProposal));
        } catch (JsonProcessingException e) {
            return Optional.empty();
        }
    }
}
