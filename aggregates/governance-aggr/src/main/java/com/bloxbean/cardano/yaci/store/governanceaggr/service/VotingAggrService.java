package com.bloxbean.cardano.yaci.store.governanceaggr.service;

import com.bloxbean.cardano.yaci.core.model.governance.GovActionId;
import com.bloxbean.cardano.yaci.core.model.governance.Vote;
import com.bloxbean.cardano.yaci.core.model.governance.VoterType;
import com.bloxbean.cardano.yaci.store.governance.domain.VotingProcedure;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.Row2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.bloxbean.cardano.yaci.store.adapot.jooq.Tables.EPOCH_STAKE;
import static com.bloxbean.cardano.yaci.store.governance.jooq.Tables.VOTING_PROCEDURE;
import static com.bloxbean.cardano.yaci.store.governance_aggr.jooq.Tables.DREP_DIST;
import static org.jooq.impl.DSL.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class VotingAggrService {
    private final DSLContext dsl;

    @Transactional
    public List<VotingProcedure> getVotesBySPO(int epoch, List<GovActionId> govActionIds) {
        if (govActionIds == null || govActionIds.isEmpty()) {
            return List.of();
        }

        List<Row2<String, Integer>> govActionPairs = govActionIds.stream()
                .map(id -> row(id.getTransactionId(), id.getGov_action_index()))
                .collect(Collectors.toList());

        var cteQuery = select(
                VOTING_PROCEDURE.VOTER_HASH.as("voter_hash"),
                VOTING_PROCEDURE.VOTER_TYPE.as("voter_type"),
                VOTING_PROCEDURE.GOV_ACTION_TX_HASH.as("gov_action_tx_hash"),
                VOTING_PROCEDURE.GOV_ACTION_INDEX.as("gov_action_index"),
                max(VOTING_PROCEDURE.SLOT).as("max_slot")
        )
                .from(VOTING_PROCEDURE)
                .where(VOTING_PROCEDURE.EPOCH.le(epoch))
                .and(row(VOTING_PROCEDURE.GOV_ACTION_TX_HASH, VOTING_PROCEDURE.GOV_ACTION_INDEX).in(govActionPairs))
                .and(VOTING_PROCEDURE.VOTER_TYPE.eq("STAKING_POOL_KEY_HASH"))
                .groupBy(
                        VOTING_PROCEDURE.VOTER_HASH,
                        VOTING_PROCEDURE.VOTER_TYPE,
                        VOTING_PROCEDURE.GOV_ACTION_TX_HASH,
                        VOTING_PROCEDURE.GOV_ACTION_INDEX
                );

        var voteWithMaxSlot = table(name("vote_with_max_slot"));

        var vVoterHash = field(name("vote_with_max_slot", "voter_hash"), String.class);
        var vGovActionTxHash = field(name("vote_with_max_slot", "gov_action_tx_hash"), String.class);
        var vGovActionIndex = field(name("vote_with_max_slot", "gov_action_index"), Integer.class);
        var vMaxSlot = field(name("vote_with_max_slot", "max_slot"), Long.class);

        Result<Record> result = dsl.with("vote_with_max_slot").as(cteQuery)
                .select(VOTING_PROCEDURE.fields())
                .from(VOTING_PROCEDURE)
                .join(voteWithMaxSlot)
                .on(VOTING_PROCEDURE.VOTER_HASH.eq(vVoterHash)
                        .and(VOTING_PROCEDURE.GOV_ACTION_TX_HASH.eq(vGovActionTxHash))
                        .and(VOTING_PROCEDURE.GOV_ACTION_INDEX.eq(vGovActionIndex))
                        .and(VOTING_PROCEDURE.SLOT.eq(vMaxSlot)))
                .andExists(
                        selectOne()
                                .from(EPOCH_STAKE)
                                .where(EPOCH_STAKE.POOL_ID.eq(VOTING_PROCEDURE.VOTER_HASH))
                                .and(EPOCH_STAKE.EPOCH.eq(epoch))
                )
                .fetch();

        return mapToVotingProcedures(result);
    }

    @Transactional
    public List<VotingProcedure> getVotesByCommittee(int epoch, List<GovActionId> govActionIds, List<String> committeeHotKeys) {
        if (govActionIds == null || govActionIds.isEmpty()) {
            return List.of();
        }

        List<Row2<String, Integer>> govActionPairs = govActionIds.stream()
                .map(id -> row(id.getTransactionId(), id.getGov_action_index()))
                .collect(Collectors.toList());

        var cteQuery = select(
                VOTING_PROCEDURE.VOTER_HASH.as("voter_hash"),
                VOTING_PROCEDURE.VOTER_TYPE.as("voter_type"),
                VOTING_PROCEDURE.GOV_ACTION_TX_HASH.as("gov_action_tx_hash"),
                VOTING_PROCEDURE.GOV_ACTION_INDEX.as("gov_action_index"),
                max(VOTING_PROCEDURE.SLOT).as("max_slot")
        )
                .from(VOTING_PROCEDURE)
                .where(VOTING_PROCEDURE.EPOCH.le(epoch))
                .and(row(VOTING_PROCEDURE.GOV_ACTION_TX_HASH, VOTING_PROCEDURE.GOV_ACTION_INDEX).in(govActionPairs))
                .and(VOTING_PROCEDURE.VOTER_TYPE.in(
                        "CONSTITUTIONAL_COMMITTEE_HOT_SCRIPT_HASH",
                        "CONSTITUTIONAL_COMMITTEE_HOT_KEY_HASH"
                ))
                .and(VOTING_PROCEDURE.VOTER_HASH.in(committeeHotKeys))
                .groupBy(
                        VOTING_PROCEDURE.VOTER_HASH,
                        VOTING_PROCEDURE.VOTER_TYPE,
                        VOTING_PROCEDURE.GOV_ACTION_TX_HASH,
                        VOTING_PROCEDURE.GOV_ACTION_INDEX
                );

        var voteWithMaxSlot = table(name("vote_with_max_slot"));

        var vVoterHash = field(name("vote_with_max_slot", "voter_hash"), String.class);
        var vGovActionTxHash = field(name("vote_with_max_slot", "gov_action_tx_hash"), String.class);
        var vGovActionIndex = field(name("vote_with_max_slot", "gov_action_index"), Integer.class);
        var vMaxSlot = field(name("vote_with_max_slot", "max_slot"), Long.class);

        Result<org.jooq.Record> result = dsl.with("vote_with_max_slot").as(
                        cteQuery
                )
                .select(VOTING_PROCEDURE.fields())
                .from(VOTING_PROCEDURE)
                .join(voteWithMaxSlot)
                .on(VOTING_PROCEDURE.VOTER_HASH.eq(vVoterHash)
                        .and(VOTING_PROCEDURE.GOV_ACTION_TX_HASH.eq(vGovActionTxHash))
                        .and(VOTING_PROCEDURE.GOV_ACTION_INDEX.eq(vGovActionIndex))
                        .and(VOTING_PROCEDURE.SLOT.eq(vMaxSlot)))
                .fetch();

        return mapToVotingProcedures(result);
    }

    @Transactional
    public List<VotingProcedure> getVotesByDRep(int epoch, List<GovActionId> govActionIds) {
        if (govActionIds == null || govActionIds.isEmpty()) {
            return List.of();
        }

        List<Row2<String, Integer>> govActionPairs = govActionIds.stream()
                .map(id -> row(id.getTransactionId(), id.getGov_action_index()))
                .collect(Collectors.toList());

        var cteQuery = select(
                VOTING_PROCEDURE.VOTER_HASH.as("voter_hash"),
                VOTING_PROCEDURE.VOTER_TYPE.as("voter_type"),
                VOTING_PROCEDURE.GOV_ACTION_TX_HASH.as("gov_action_tx_hash"),
                VOTING_PROCEDURE.GOV_ACTION_INDEX.as("gov_action_index"),
                max(VOTING_PROCEDURE.SLOT).as("max_slot")
        )
                .from(VOTING_PROCEDURE)
                .where(VOTING_PROCEDURE.EPOCH.le(epoch))
                .and(row(VOTING_PROCEDURE.GOV_ACTION_TX_HASH, VOTING_PROCEDURE.GOV_ACTION_INDEX).in(govActionPairs))
                .and(VOTING_PROCEDURE.VOTER_TYPE.in("DREP_KEY_HASH", "DREP_SCRIPT_HASH"))
                .groupBy(
                        VOTING_PROCEDURE.VOTER_HASH,
                        VOTING_PROCEDURE.VOTER_TYPE,
                        VOTING_PROCEDURE.GOV_ACTION_TX_HASH,
                        VOTING_PROCEDURE.GOV_ACTION_INDEX
                );

        var voteWithMaxSlot = table(name("vote_with_max_slot"));

        var vVoterHash = field(name("vote_with_max_slot", "voter_hash"), String.class);
        var vGovActionTxHash = field(name("vote_with_max_slot", "gov_action_tx_hash"), String.class);
        var vGovActionIndex = field(name("vote_with_max_slot", "gov_action_index"), Integer.class);
        var vMaxSlot = field(name("vote_with_max_slot", "max_slot"), Long.class);

        Result<Record> result = dsl.with("vote_with_max_slot").as(cteQuery)
                .select(VOTING_PROCEDURE.fields())
                .from(VOTING_PROCEDURE)
                .join(voteWithMaxSlot)
                .on(VOTING_PROCEDURE.VOTER_HASH.eq(vVoterHash)
                        .and(VOTING_PROCEDURE.GOV_ACTION_TX_HASH.eq(vGovActionTxHash))
                        .and(VOTING_PROCEDURE.GOV_ACTION_INDEX.eq(vGovActionIndex))
                        .and(VOTING_PROCEDURE.SLOT.eq(vMaxSlot)))
                .andExists(
                        selectOne()
                                .from(DREP_DIST)
                                .where(DREP_DIST.DREP_HASH.eq(VOTING_PROCEDURE.VOTER_HASH))
                                .and(DREP_DIST.EPOCH.eq(epoch))
                )
                .fetch();

        return mapToVotingProcedures(result);
    }

    private List<VotingProcedure> mapToVotingProcedures(Result<Record> result) {
        return result.map(record -> VotingProcedure.builder()
                .id(UUID.fromString(record.get(VOTING_PROCEDURE.ID, String.class)))
                .txHash(record.get(VOTING_PROCEDURE.TX_HASH, String.class))
                .index(record.get(VOTING_PROCEDURE.IDX, Long.class))
                .slot(record.get(VOTING_PROCEDURE.SLOT, Long.class))
                .voterType(VoterType.valueOf(record.get(VOTING_PROCEDURE.VOTER_TYPE, String.class)))
                .voterHash(record.get(VOTING_PROCEDURE.VOTER_HASH, String.class))
                .govActionTxHash(record.get(VOTING_PROCEDURE.GOV_ACTION_TX_HASH, String.class))
                .govActionIndex(record.get(VOTING_PROCEDURE.GOV_ACTION_INDEX, Integer.class))
                .vote(record.get(VOTING_PROCEDURE.VOTE, String.class) != null ? Vote.valueOf(record.get(VOTING_PROCEDURE.VOTE, String.class)) : null)
                .anchorUrl(record.get(VOTING_PROCEDURE.ANCHOR_URL, String.class))
                .anchorHash(record.get(VOTING_PROCEDURE.ANCHOR_HASH, String.class))
                .epoch(record.get(VOTING_PROCEDURE.EPOCH, Integer.class))
                .build());
    }
}
