package com.bloxbean.cardano.yaci.store.governance.storage.local;

import com.bloxbean.cardano.yaci.store.governance.domain.local.LocalGovActionProposalStatus;
import com.bloxbean.cardano.yaci.store.common.domain.GovActionStatus;

import java.util.List;

public interface LocalGovActionProposalStatusStorage {
    void saveAll(List<LocalGovActionProposalStatus> localGovActionProposalStatusList);
    List<LocalGovActionProposalStatus> findByEpochAndStatusIn(Integer epochNo, List<GovActionStatus> statusList);
    int deleteBySlotGreaterThan(long slot);
}
