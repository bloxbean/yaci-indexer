package com.bloxbean.cardano.yaci.store.governanceaggr.client;

import com.bloxbean.cardano.yaci.core.model.governance.GovActionId;
import com.bloxbean.cardano.yaci.core.model.governance.GovActionType;
import com.bloxbean.cardano.yaci.store.client.governance.ProposalStateClient;
import com.bloxbean.cardano.yaci.store.common.domain.GovActionProposal;
import com.bloxbean.cardano.yaci.store.governance.storage.GovActionProposalStorage;
import com.bloxbean.cardano.yaci.store.common.domain.GovActionStatus;
import com.bloxbean.cardano.yaci.store.governanceaggr.domain.GovActionProposalStatus;
import com.bloxbean.cardano.yaci.store.governanceaggr.storage.GovActionProposalStatusStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component("proposalStateClient")
@RequiredArgsConstructor
public class ProposalStateClientImpl implements ProposalStateClient {
    private final GovActionProposalStatusStorage govActionProposalStatusStorage;
    private final GovActionProposalStorage govActionProposalStorage;

    @Override
    public List<GovActionProposal> getProposalsByStatusAndEpoch(GovActionStatus status, int epoch) {
        var proposalStatusList = govActionProposalStatusStorage.findByStatusAndEpoch(status, epoch);

        return getGovActionProposals(proposalStatusList);
    }

    @Override
    public List<GovActionProposal> getProposalsByStatusListAndEpoch(List<GovActionStatus> statusList, int epoch) {
        var proposalStatusList = govActionProposalStatusStorage.findByStatusListAndEpoch(statusList, epoch);

        return getGovActionProposals(proposalStatusList);
    }

    private List<GovActionProposal> getGovActionProposals(List<GovActionProposalStatus> proposalStatusList) {
        return govActionProposalStorage.findByGovActionIds(proposalStatusList.stream()
                        .map(proposalStatus -> new GovActionId(proposalStatus.getGovActionTxHash(), proposalStatus.getGovActionIndex()))
                        .toList())
                .stream()
                .map(govActionProposal ->
                        GovActionProposal.builder()
                                .txHash(govActionProposal.getTxHash())
                                .index((int) govActionProposal.getIndex())
                                .anchorUrl(govActionProposal.getAnchorUrl())
                                .anchorHash(govActionProposal.getAnchorHash())
                                .deposit(govActionProposal.getDeposit())
                                .returnAddress(govActionProposal.getReturnAddress())
                                .type(govActionProposal.getType())
                                .blockNumber(govActionProposal.getBlockNumber())
                                .blockTime(govActionProposal.getBlockTime())
                                .slot(govActionProposal.getSlot())
                                .epoch(govActionProposal.getEpoch())
                                .details(govActionProposal.getDetails())
                                .build())
                .collect(Collectors.toList());
    }

    @Override
    public Optional<GovActionProposal> getLastEnactedProposal(GovActionType govActionType, int currentEpoch) {
        var govActionProposalStatusOpt = govActionProposalStatusStorage.findLastEnactedProposal(govActionType, currentEpoch);
        if (govActionProposalStatusOpt.isEmpty()) {
            return Optional.empty();
        }

        var govActionProposals = govActionProposalStorage.findByGovActionIds(
                List.of(new GovActionId(govActionProposalStatusOpt.get().getGovActionTxHash(), govActionProposalStatusOpt.get().getGovActionIndex())));

        if (govActionProposals.isEmpty()) {
            return Optional.empty();
        } else {
            var govActionProposal = govActionProposals.get(0);
            return Optional.of(GovActionProposal.builder()
                    .txHash(govActionProposal.getTxHash())
                    .index((int) govActionProposal.getIndex())
                    .anchorUrl(govActionProposal.getAnchorUrl())
                    .anchorHash(govActionProposal.getAnchorHash())
                    .deposit(govActionProposal.getDeposit())
                    .returnAddress(govActionProposal.getReturnAddress())
                    .type(govActionProposal.getType())
                    .blockNumber(govActionProposal.getBlockNumber())
                    .blockTime(govActionProposal.getBlockTime())
                    .slot(govActionProposal.getSlot())
                    .epoch(govActionProposal.getEpoch())
                    .details(govActionProposal.getDetails())
                    .build());
        }
    }
}
