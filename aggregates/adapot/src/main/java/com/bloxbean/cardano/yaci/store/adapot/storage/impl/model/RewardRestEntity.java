package com.bloxbean.cardano.yaci.store.adapot.storage.impl.model;

import com.bloxbean.cardano.yaci.store.events.domain.RewardRestType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "reward_rest")
public class RewardRestEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "address")
    private String address;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private RewardRestType type; 

    @Column(name = "earned_epoch")
    private Integer earnedEpoch;

    @Column(name = "amount")
    private BigInteger amount;

    @Column(name = "spendable_epoch")
    private Integer spendableEpoch;

    @Column(name = "slot")
    private Long slot;

    @UpdateTimestamp
    @Column(name = "create_datetime")
    private LocalDateTime createDatetime;
}
