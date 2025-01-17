package com.bloxbean.cardano.yaci.store.adapot.snapshot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class StakeSnapshotService {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void takeStakeSnapshot(int epoch) {
        log.info("Taking stake snapshot for epoch : " + epoch);

        // Delete existing snapshot data if any for the epoch using jdbc tempalte
        jdbcTemplate.update("delete from epoch_stake where epoch = :epoch", new MapSqlParameterSource().addValue("epoch", epoch));

        var query = """
                    WITH RankedDelegations AS (
                        SELECT
                            address,
                            pool_id,
                            epoch,
                            slot,
                            tx_index,
                            cert_index,
                            ROW_NUMBER() OVER (
                                PARTITION BY address
                                ORDER BY slot DESC, tx_index DESC, cert_index DESC
                                ) AS rn
                        FROM
                            delegation
                        WHERE
                                epoch <= :epoch
                    ),
                         MaxSlotBalances AS (
                             SELECT
                                 address,
                                 MAX(slot) AS max_slot
                             FROM
                                 stake_address_balance
                             WHERE
                                     epoch <= :epoch
                             GROUP BY
                                 address
                         ),
                         last_withdrawal AS (
                              SELECT address, MAX(slot) AS max_slot
                              FROM withdrawal
                              where epoch <= :epoch
                              GROUP BY address
                          ),
                         pool_refund_rewards AS (
                                     SELECT r.address, SUM(r.amount) AS pool_refund_withdrawable_reward
                                     FROM reward r
                                              LEFT JOIN last_withdrawal lw ON r.address = lw.address
                                     WHERE (lw.max_slot IS NULL OR r.slot > lw.max_slot)
                                       AND r.spendable_epoch <= :epoch and r.type = 'refund'
                                     GROUP BY r.address
                                 ),
                        pool_rewards AS (
                                     SELECT r.address, SUM(r.amount) AS withdrawable_reward
                                     FROM reward r
                                              LEFT JOIN last_withdrawal lw ON r.address = lw.address
                                     WHERE (lw.max_slot IS NULL OR r.slot > lw.max_slot)
                                       AND  r.earned_epoch <= :epoch and (r.type = 'member' OR r.type = 'leader')
                                       AND  r.spendable_epoch <= :snapshot_epoch and (r.type = 'member' OR r.type = 'leader')
                                     GROUP BY r.address
                         ),
                         insta_spendable_rewards AS (
                                  SELECT r.address, SUM(r.amount) AS insta_withdrawable_reward
                                  FROM instant_reward r
                                           LEFT JOIN last_withdrawal lw ON r.address = lw.address
                                  WHERE (lw.max_slot IS NULL OR r.slot > lw.max_slot)
                                  AND r.spendable_epoch <= :snapshot_epoch
                                  GROUP BY r.address
                          ),
                          spendable_reward_rest AS (
                                  SELECT r.address, SUM(r.amount) AS withdrawable_reward_rest
                                  FROM reward_rest r
                                           LEFT JOIN last_withdrawal lw ON r.address = lw.address
                                  WHERE (lw.max_slot IS NULL OR r.slot > lw.max_slot)
                                  AND r.spendable_epoch <= :epoch
                                  GROUP BY r.address
                          ),
                        PoolStatus AS (
                                 SELECT
                                     pool_id,
                                     status,
                                     registration_slot,                    
                                     slot,
                                     ROW_NUMBER() OVER (
                                         PARTITION BY pool_id
                                         ORDER BY slot DESC, tx_index DESC, cert_index DESC
                                         ) AS rn
                                 FROM
                                     pool
                                 WHERE
                                         epoch <= :epoch
                        )

                    insert into epoch_stake
                    SELECT
                        :epoch,
                        d.address,
                        (COALESCE(s.quantity, 0) + COALESCE(r.withdrawable_reward, 0) + COALESCE(pr.pool_refund_withdrawable_reward, 0) 
                             + COALESCE(ir.insta_withdrawable_reward, 0) + COALESCE(rr.withdrawable_reward_rest, 0)),
                        d.pool_id,
                        d.epoch,
                        :activeEpoch,
                        now()
                    FROM
                        RankedDelegations d
                            LEFT JOIN
                        MaxSlotBalances msb ON d.address = msb.address
                            LEFT JOIN
                        stake_address_balance s ON msb.address = s.address AND msb.max_slot = s.slot
                            LEFT JOIN
                        pool_rewards r ON d.address = r.address           
                            LEFT JOIN
                        pool_refund_rewards pr ON d.address = pr.address
                            LEFT JOIN
                        insta_spendable_rewards ir ON d.address = ir.address     
                            LEFT JOIN
                        spendable_reward_rest rr ON d.address = rr.address
                    WHERE
                            d.rn = 1
                    and not exists(
                                    select 1 from PoolStatus p
                                    where d.pool_id = p.pool_id and p.rn = 1 and (p.status = 'RETIRED' or d.slot < p.registration_slot)
                            )
                            and  NOT EXISTS (
                                    SELECT 1
                                    FROM stake_registration sd
                                    WHERE sd.address = d.address
                                    AND sd.type = 'STAKE_DEREGISTRATION'
                                    AND sd.epoch <= :epoch
                                    AND (
                                        sd.slot > d.slot OR
                                        (sd.slot = d.slot AND sd.tx_index > d.tx_index) OR
                                        (sd.slot = d.slot AND sd.tx_index = d.tx_index AND sd.cert_index > d.cert_index)
                                    )
                            )
                """;

                var params = new MapSqlParameterSource();
                params.addValue("epoch", epoch);
                params.addValue("snapshot_epoch", epoch + 1);
                params.addValue("activeEpoch", epoch + 2);

                jdbcTemplate.update(query, params);

        log.info("Stake snapshot for epoch : {} is taken", epoch);
        log.info(">>>>>>>>>>>>>>>>>>>> Stake Snapshot taken for epoch : {} <<<<<<<<<<<<<<<<<<<<", epoch);
    }
}
