package com.bloxbean.cardano.yaci.store.governance.storage.local.impl;

import com.bloxbean.cardano.yaci.store.governance.domain.local.LocalGovActionProposalStatus;
import com.bloxbean.cardano.yaci.store.governance.storage.local.LocalGovActionProposalStatusStorage;
import com.bloxbean.cardano.yaci.store.governance.storage.impl.mapper.LocalGovActionProposalStatusMapper;
import com.bloxbean.cardano.yaci.store.common.domain.GovActionStatus;
import com.bloxbean.cardano.yaci.store.governance.storage.impl.repository.LocalGovActionProposalStatusRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class LocalGovActionProposalStatusStorageImpl implements LocalGovActionProposalStatusStorage {
    private final LocalGovActionProposalStatusRepository localGovActionProposalStatusRepository;
    private final LocalGovActionProposalStatusMapper localGovActionProposalStatusMapper;

    @Override
    public void saveAll(List<LocalGovActionProposalStatus> localGovActionProposalStatusList) {
        localGovActionProposalStatusRepository.saveAll(localGovActionProposalStatusList.stream()
                .map(localGovActionProposalStatusMapper::toLocalGovActionProposalStatusEntity).toList());
    }

    @Override
    public List<LocalGovActionProposalStatus> findByEpochAndStatusIn(Integer epochNo, List<GovActionStatus> statusList) {
        return localGovActionProposalStatusRepository.findByEpochAndStatusIn(epochNo, statusList)
                .stream()
                .map(localGovActionProposalStatusMapper::toLocalGovActionProposalStatus)
                .toList();
    }

    @Override
    public int deleteBySlotGreaterThan(long slot) {
        return localGovActionProposalStatusRepository.deleteBySlotGreaterThan(slot);
    }
}
