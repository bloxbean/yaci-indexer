package com.bloxbean.cardano.yaci.store.governanceaggr.storage.impl;

import com.bloxbean.cardano.yaci.core.model.governance.GovActionType;
import com.bloxbean.cardano.yaci.store.common.domain.GovActionStatus;
import com.bloxbean.cardano.yaci.store.governanceaggr.domain.GovActionProposalStatus;
import com.bloxbean.cardano.yaci.store.governanceaggr.storage.GovActionProposalStatusStorage;
import com.bloxbean.cardano.yaci.store.governanceaggr.storage.impl.mapper.GovActionProposalStatusMapper;
import com.bloxbean.cardano.yaci.store.governanceaggr.storage.impl.repository.GovActionProposalStatusRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class GovActionProposalStatusStorageImpl implements GovActionProposalStatusStorage {
    private final GovActionProposalStatusRepository govActionProposalStatusRepository;
    private final GovActionProposalStatusMapper mapper;

    @Override
    public void saveAll(List<GovActionProposalStatus> govActionProposalStatusList) {
        govActionProposalStatusRepository.saveAll(govActionProposalStatusList.stream()
                .map(mapper::toGovActionProposalStatusEntity).toList());
    }

    @Override
    public List<GovActionProposalStatus> findByStatusAndEpoch(GovActionStatus status, int epoch) {
        return govActionProposalStatusRepository.findByStatusAndEpoch(status, epoch).stream()
                .map(mapper::toGovActionProposalStatus).toList();
    }

    @Override
    public List<GovActionProposalStatus> findByStatusListAndEpoch(List<GovActionStatus> statusList, int epoch) {
        return govActionProposalStatusRepository.findByStatusListAndEpoch(statusList, epoch).stream()
                .map(mapper::toGovActionProposalStatus).toList();
    }

    @Override
    public Optional<GovActionProposalStatus> findLastEnactedProposal(GovActionType govActionType, int epoch) {
        return govActionProposalStatusRepository.findLastEnactedProposal(govActionType, epoch)
                .map(mapper::toGovActionProposalStatus);
    }
}
