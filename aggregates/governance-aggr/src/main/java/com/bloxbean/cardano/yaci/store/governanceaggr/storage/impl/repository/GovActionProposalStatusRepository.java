package com.bloxbean.cardano.yaci.store.governanceaggr.storage.impl.repository;

import com.bloxbean.cardano.yaci.core.model.governance.GovActionType;
import com.bloxbean.cardano.yaci.store.common.domain.GovActionStatus;
import com.bloxbean.cardano.yaci.store.governanceaggr.storage.impl.model.GovActionProposalStatusEntity;
import com.bloxbean.cardano.yaci.store.governanceaggr.storage.impl.model.GovActionProposalStatusId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GovActionProposalStatusRepository extends JpaRepository<GovActionProposalStatusEntity,
        GovActionProposalStatusId> {
    List<GovActionProposalStatusEntity> findByStatusAndEpoch(GovActionStatus status, int epoch);

    @Query("select g from GovActionProposalStatusEntity g where g.status in :statusList and g.epoch = :epoch")
    List<GovActionProposalStatusEntity> findByStatusListAndEpoch(List<GovActionStatus> statusList, int epoch);
    @Query("select g from GovActionProposalStatusEntity g where g.type = :govActionType and g.status = 'RATIFIED' and g.epoch < :epoch order by g.epoch desc limit 1")
    Optional<GovActionProposalStatusEntity> findLastEnactedProposal(GovActionType govActionType, int epoch);
}
