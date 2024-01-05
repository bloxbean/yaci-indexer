package com.bloxbean.cardano.yaci.store.governance.storage.impl.model;

import com.bloxbean.cardano.yaci.core.model.governance.Vote;
import com.bloxbean.cardano.yaci.core.model.governance.VoterType;
import com.bloxbean.cardano.yaci.store.common.model.BlockAwareEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "voting_procedure")
@IdClass(VotingProcedureId.class)
public class VotingProcedureEntity extends BlockAwareEntity {

    @Id
    @Column(name = "tx_hash")
    private String txHash;

    @Id
    @Column(name = "index")
    private long index;

    @Column(name = "slot")
    private Long slot;

    @Column(name = "voter_type")
    @Enumerated(EnumType.STRING)
    private VoterType voterType;

    @Column(name = "voter_hash")
    private String voterHash;

    @Column(name = "gov_action_tx_hash")
    private String govActionTxHash;

    @Column(name = "gov_action_index")
    private Integer govActionIndex;

    @Column(name = "vote")
    @Enumerated(EnumType.STRING)
    private Vote vote;

    @Column(name = "anchor_url")
    private String anchorUrl;

    @Column(name = "anchor_hash")
    private String anchorHash;
}

