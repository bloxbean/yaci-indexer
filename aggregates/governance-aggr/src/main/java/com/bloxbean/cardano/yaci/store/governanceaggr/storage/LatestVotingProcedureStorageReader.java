package com.bloxbean.cardano.yaci.store.governanceaggr.storage;

import com.bloxbean.cardano.yaci.store.governanceaggr.domain.LatestVotingProcedure;
import com.bloxbean.cardano.yaci.store.governanceaggr.storage.impl.model.LatestVotingProcedureId;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface LatestVotingProcedureStorageReader {
    Optional<Long> findLatestSlotOfVotingProcedure();

    List<LatestVotingProcedure> getAllByIdIn(List<LatestVotingProcedureId> votingProcedureIds);
    List<LatestVotingProcedure> findBySlotGreaterThan(Long slot);
}