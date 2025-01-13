package com.bloxbean.cardano.yaci.store.adapot.storage.impl.repository;

import com.bloxbean.cardano.yaci.store.adapot.storage.impl.model.RewardRestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RewardRestRepository extends JpaRepository<RewardRestEntity, UUID> {

}
