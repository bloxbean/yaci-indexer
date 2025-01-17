package com.bloxbean.cardano.yaci.store.governance.storage.impl.model;

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
@Table(name = "local_gov_action_proposal_status")
@IdClass(LocalGovActionProposalStatusId.class)
public class LocalGovActionProposalStatusEntity {
    @Id
    @Column(name = "gov_action_tx_hash")
    private String govActionTxHash;

    @Id
    @Column(name = "gov_action_index")
    private long govActionIndex;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private GovActionStatus status;

    @Id
    @Column(name = "epoch")
    private Integer epoch;

    @Column(name = "slot")
    private Long slot;

    @UpdateTimestamp
    @Column(name = "update_datetime")
    private LocalDateTime updateDateTime;
}
