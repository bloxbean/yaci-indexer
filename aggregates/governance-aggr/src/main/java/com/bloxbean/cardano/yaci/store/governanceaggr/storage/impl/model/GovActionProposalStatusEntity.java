package com.bloxbean.cardano.yaci.store.governanceaggr.storage.impl.model;

import com.bloxbean.cardano.yaci.core.model.governance.GovActionType;
import com.bloxbean.cardano.yaci.store.common.domain.GovActionStatus;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "gov_action_proposal_status")
@IdClass(GovActionProposalStatusId.class)
//todo: should add prev gov action id?
public class GovActionProposalStatusEntity {
    @Id
    @Column(name = "gov_action_tx_hash")
    private String govActionTxHash;

    @Id
    @Column(name = "gov_action_index")
    private int govActionIndex;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private GovActionType type;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private GovActionStatus status;

    @Id
    @Column(name = "epoch")
    private Integer epoch;

    @UpdateTimestamp
    @Column(name = "update_datetime")
    private LocalDateTime updateDateTime;
}
