package com.bloxbean.cardano.yaci.store.governance.storage.impl;

import com.bloxbean.cardano.yaci.core.model.CredentialType;
import com.bloxbean.cardano.yaci.store.governance.domain.CommitteeMember;
import com.bloxbean.cardano.yaci.store.governance.domain.CommitteeMemberDetails;
import com.bloxbean.cardano.yaci.store.governance.storage.CommitteeMemberStorage;
import com.bloxbean.cardano.yaci.store.governance.storage.impl.mapper.CommitteeMemberMapper;
import com.bloxbean.cardano.yaci.store.governance.storage.impl.repository.CommitteeMemberRepository;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;

import java.util.List;
import java.util.stream.Collectors;

import static com.bloxbean.cardano.yaci.store.governance.jooq.Tables.*;
import static org.jooq.impl.DSL.*;

@RequiredArgsConstructor
public class CommitteeMemberStorageImpl implements CommitteeMemberStorage {
    private final CommitteeMemberRepository committeeMemberRepository;
    private final CommitteeMemberMapper committeeMemberMapper;
    private final DSLContext dsl;

    @Override
    public void saveAll(List<CommitteeMember> committeeMembers) {
        committeeMemberRepository.saveAll(committeeMembers.stream()
                .map(committeeMemberMapper::toCommitteeMemberEntity)
                .collect(Collectors.toList()));
    }

    @Override
    public List<CommitteeMember> getCommitteeMembersByEpoch(int epoch) {
        return committeeMemberRepository.findCommitteeMembersByEpoch(epoch)
                .stream()
                .map(committeeMemberMapper::toCommitteeMember)
                .collect(Collectors.toList());
    }

    // todo: tx index?
    @Override
    public List<CommitteeMemberDetails> getActiveCommitteeMembersDetailsByEpoch(int epoch) {
        var latestRegistration = dsl.select(
                        COMMITTEE_REGISTRATION.COLD_KEY,
                        max(COMMITTEE_REGISTRATION.SLOT).as("latest_slot"),
                        max(COMMITTEE_REGISTRATION.CERT_INDEX).as("latest_cert_index")
                )
                .from(COMMITTEE_REGISTRATION)
                .where(COMMITTEE_REGISTRATION.EPOCH.le(epoch))
                .groupBy(COMMITTEE_REGISTRATION.COLD_KEY)
                .asTable("latest_registration");

        return dsl.select(
                        COMMITTEE_REGISTRATION.COLD_KEY,
                        COMMITTEE_REGISTRATION.HOT_KEY,
                        COMMITTEE_REGISTRATION.CRED_TYPE,
                        COMMITTEE_MEMBER.START_EPOCH,
                        COMMITTEE_MEMBER.EXPIRED_EPOCH
                )
                .from(COMMITTEE_REGISTRATION)
                .join(latestRegistration)
                .on(COMMITTEE_REGISTRATION.COLD_KEY.eq(latestRegistration.field(COMMITTEE_REGISTRATION.COLD_KEY)))
                .and(COMMITTEE_REGISTRATION.SLOT.eq(latestRegistration.field("latest_slot", Long.class)))
                .and(COMMITTEE_REGISTRATION.CERT_INDEX.eq(latestRegistration.field("latest_cert_index", Integer.class)))
                .join(COMMITTEE_MEMBER)
                .on(COMMITTEE_MEMBER.HASH.eq(COMMITTEE_REGISTRATION.COLD_KEY))
                .whereNotExists(
                        dsl.selectOne()
                                .from(COMMITTEE_DEREGISTRATION)
                                .where(COMMITTEE_DEREGISTRATION.COLD_KEY.eq(COMMITTEE_REGISTRATION.COLD_KEY))
                                .and(
                                        COMMITTEE_DEREGISTRATION.SLOT.gt(COMMITTEE_REGISTRATION.SLOT)
                                                .or(
                                                        COMMITTEE_DEREGISTRATION.SLOT.eq(COMMITTEE_REGISTRATION.SLOT)
                                                                .and(COMMITTEE_DEREGISTRATION.CERT_INDEX.gt(COMMITTEE_REGISTRATION.CERT_INDEX))
                                                )
                                )
                )
                .fetch(record -> new CommitteeMemberDetails(
                        record.get(COMMITTEE_REGISTRATION.COLD_KEY),
                        record.get(COMMITTEE_REGISTRATION.HOT_KEY),
                        CredentialType.valueOf(record.get(COMMITTEE_REGISTRATION.CRED_TYPE)),
                        record.get(COMMITTEE_MEMBER.START_EPOCH),
                        record.get(COMMITTEE_MEMBER.EXPIRED_EPOCH)
                ));
    }

    @Override
    public int deleteBySlotGreaterThan(long slot) {
        return committeeMemberRepository.deleteBySlotGreaterThan(slot);
    }
}
