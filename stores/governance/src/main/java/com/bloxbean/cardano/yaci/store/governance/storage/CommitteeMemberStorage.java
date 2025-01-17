package com.bloxbean.cardano.yaci.store.governance.storage;

import com.bloxbean.cardano.yaci.store.governance.domain.CommitteeMember;
import com.bloxbean.cardano.yaci.store.governance.domain.CommitteeMemberDetails;

import java.util.List;

public interface CommitteeMemberStorage {
    void saveAll(List<CommitteeMember> committeeMembers);
    List<CommitteeMember> getCommitteeMembersByEpoch(int epoch);
    List<CommitteeMemberDetails> getActiveCommitteeMembersDetailsByEpoch(int epoch);
    int deleteBySlotGreaterThan(long slot);
}
