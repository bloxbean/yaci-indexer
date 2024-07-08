package com.bloxbean.cardano.yaci.store.adapot.snapshot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class StakeSnapshotService {
    private final DSLContext dsl;
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Transactional
    public void takeStakeSnapshot(int epoch) {
        log.info("Taking stake snapshot for epoch : " + epoch);

        var query = """
                    WITH RankedDelegations AS (
                        SELECT
                            address,
                            pool_id,
                            epoch,
                            slot,
                            cert_index,
                            ROW_NUMBER() OVER (
                                PARTITION BY address
                                ORDER BY slot DESC, cert_index DESC
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
                        PoolStatus AS (
                                 SELECT
                                     pool_id,
                                     status,
                                     slot,
                                     ROW_NUMBER() OVER (
                                         PARTITION BY pool_id
                                         ORDER BY slot DESC, cert_index DESC
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
                        (COALESCE(s.quantity, 0) + COALESCE(r.withdrawable_reward, 0) + COALESCE(pr.pool_refund_withdrawable_reward, 0) + COALESCE(ir.insta_withdrawable_reward, 0)) ,
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
                    WHERE
                            d.rn = 1
                    and not exists(
                                    select 1 from PoolStatus p
                                    where d.pool_id = p.pool_id and p.rn = 1 and p.status = 'RETIRED'
                            )
                            and  NOT EXISTS (
                                    SELECT 1
                                    FROM stake_registration sd
                                    WHERE sd.address = d.address
                                    AND sd.type = 'STAKE_DEREGISTRATION'
                                    AND sd.epoch <= d.epoch
                                    AND sd.slot > d.slot
                            )
                """;

                var params = new MapSqlParameterSource();
                params.addValue("epoch", epoch);
                params.addValue("snapshot_epoch", epoch + 1);
                params.addValue("activeEpoch", epoch + 2);

                jdbcTemplate.update(query, params);

        //TODO -- Exclude de-registered stake address
        //TODO -- Exclude stake address delegated to retired pool

//        var query = dsl.query("""
//                    WITH RankedDelegations AS (
//                        SELECT
//                            address,
//                            pool_id,
//                            epoch,
//                            slot,
//                            cert_index,
//                            ROW_NUMBER() OVER (
//                                PARTITION BY address
//                                ORDER BY slot DESC, cert_index DESC
//                                ) AS rn
//                        FROM
//                            delegation
//                        WHERE
//                                epoch <= ?
//                    ),
//                         MaxSlotBalances AS (
//                             SELECT
//                                 address,
//                                 MAX(slot) AS max_slot
//                             FROM
//                                 stake_address_balance
//                             WHERE
//                                     epoch <= ?
//                             GROUP BY
//                                 address
//                         ),
//                        RewardBalanceAccount AS (
//                            SELECT
//                                address,
//                                amount,
//                                epoch,
//                                slot,
//                                ROW_NUMBER() OVER (
//                                    PARTITION BY address
//                                    ORDER BY slot DESC
//                                    ) AS rn
//                            FROM
//                                reward_account
//                            WHERE
//                                    epoch <= ?
//                        ),
//                        PoolStatus AS (
//                                 SELECT
//                                     pool_id,
//                                     status,
//                                     slot,
//                                     ROW_NUMBER() OVER (
//                                         PARTITION BY pool_id
//                                         ORDER BY slot DESC, cert_index DESC
//                                         ) AS rn
//                                 FROM
//                                     pool
//                                 WHERE
//                                         epoch <= ?
//                        )
//
//                    insert into epoch_stake
//                    SELECT
//                        ?,
//                        d.address,
//                        (COALESCE(s.quantity, 0) + COALESCE(r.amount, 0)),
//                        d.pool_id,
//                        d.epoch,
//                        ?,
//                        now()
//                    FROM
//                        RankedDelegations d
//                            LEFT JOIN
//                        MaxSlotBalances msb ON d.address = msb.address
//                            LEFT JOIN
//                        stake_address_balance s ON msb.address = s.address AND msb.max_slot = s.slot
//                            LEFT JOIN
//                        RewardBalanceAccount r ON d.address = r.address and r.rn = 1
//                    WHERE
//                            d.rn = 1
//                    and not exists(
//                        select 1 from PoolStatus p
//                        where d.pool_id = p.pool_id and p.rn = 1 and p.status = 'RETIRED'
//                    )
//                    and  NOT EXISTS (
//                         SELECT 1
//                            FROM stake_registration sd
//                            WHERE sd.address = d.address
//                                AND sd.type = 'STAKE_DEREGISTRATION'
//                                AND sd.epoch <= d.epoch
//                                AND sd.slot > d.slot
//                    )
//                """, epoch, epoch, epoch, epoch, epoch, epoch + 2);

//        log.info(query.getSQL());
//        query.execute();
        log.info("Stake snapshot for epoch : {} is taken", epoch);

        //print log with ascii art of the epoch
        log.info(">>>>>>>>>>>>>>>>>>>> Stake Snapshot taken for epoch : {} <<<<<<<<<<<<<<<<<<<<", epoch);
    }
}
