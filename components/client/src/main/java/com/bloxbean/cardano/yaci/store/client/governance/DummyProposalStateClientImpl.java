package com.bloxbean.cardano.yaci.store.client.governance;

import com.bloxbean.cardano.yaci.core.model.governance.GovActionType;
import com.bloxbean.cardano.yaci.store.common.domain.GovActionProposal;
import com.bloxbean.cardano.yaci.store.common.domain.GovActionStatus;

import java.util.List;
import java.util.Optional;

public class DummyProposalStateClientImpl implements ProposalStateClient {

    @Override
    public List<GovActionProposal> getProposalsByStatusAndEpoch(GovActionStatus status, int epoch) {
        return List.of();
    }

    @Override
    public List<GovActionProposal> getProposalsByStatusListAndEpoch(List<GovActionStatus> statusList, int epoch) {
        return List.of();
    }

    @Override
    public Optional<GovActionProposal> getLastEnactedProposal(GovActionType govActionType, int currentEpoch) {
        return Optional.empty();
    }
}
