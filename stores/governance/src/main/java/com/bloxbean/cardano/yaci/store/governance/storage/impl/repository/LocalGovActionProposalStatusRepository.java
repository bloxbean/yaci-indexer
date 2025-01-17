package com.bloxbean.cardano.yaci.store.governance.storage.impl.repository;

import com.bloxbean.cardano.yaci.store.common.domain.GovActionStatus;
import com.bloxbean.cardano.yaci.store.governance.storage.impl.model.LocalGovActionProposalStatusEntity;
import com.bloxbean.cardano.yaci.store.governance.storage.impl.model.LocalGovActionProposalStatusId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocalGovActionProposalStatusRepository extends JpaRepository<LocalGovActionProposalStatusEntity,
        LocalGovActionProposalStatusId> {
    List<LocalGovActionProposalStatusEntity> findByEpochAndStatusIn(int epoch, List<GovActionStatus> statuses);
    int deleteBySlotGreaterThan(long slot);
}
