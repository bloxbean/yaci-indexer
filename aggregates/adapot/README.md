
### TODO

- [ ] Find refund type in reward table
- [ ] Find out earned epoch and spendable epoch for MIR
- [ ] Find out earned epoch and spendable epoch for pool rewards
- [ ] Remove 'epoch' store dependency from staking store
- [ ] If a pool is re-registered, what happens to the previous delegations? Still valid?
- [ ] Compare yaci-store with cardano-db-sync (stake snapshots)
- [ ] Check mainnet bootstrap amount (RewardConstants.MAINNET_BOOTSTRAP_ADDRESS_AMOUNT)
- [ ] Remove mainnet network specific constants from reward caculation project

## Rollback Script For Testing

To make sure stake address balance is calculated correctly, **we need to rollback to last block of an epoch.**

```sql
truncate cursor_;
insert into cursor_ (id, block_hash, slot, block_number, era) values (1000, :block_hash, :slot, :block_number, :era);
truncate account_config;
insert into account_config (config_id, status, slot, block, block_hash) values ('last_account_balance_processed_block', null, :slot, :block_number, :block_hash);
delete from adapot where slot > :slot;
delete from address_balance where slot > :slot;
delete from address_tx_amount where slot > :slot;
delete from address_utxo where slot > :slot;
delete from block where slot > :slot;
delete from cost_model where slot > :slot;
delete from delegation where slot > :slot;
delete from epoch_param where slot > :slot;
delete from epoch_stake where epoch_stake.epoch >= :epoch;
delete from instant_reward where slot > :slot;
delete from mir where slot > :slot;
delete from pool where slot > :slot;
delete from pool_registration where slot > :slot;
delete from pool_retirement where slot > :slot;
delete from protocol_params_proposal where slot > :slot;
delete from reward where slot > :slot;
delete from reward_rest where slot > :slot;
delete from stake_address_balance where slot > :slot;
delete from stake_registration where slot > :slot;
delete from transaction where slot > :slot;
delete  from transaction_witness where slot > :slot;
delete from tx_input where tx_input.spent_at_slot > :slot;
delete from withdrawal where slot > :slot;
delete from adapot_jobs where slot > :slot;
delete from drep_dist where drep_dist.epoch >= :epoch;
delete from drep where slot > :slot;
delete from delegation_vote where slot > :slot;
delete from drep_registration where slot > :slot;
delete from gov_action_proposal where slot > :slot;
delete from gov_action_proposal_status where slot > :slot;
delete from committee where slot > :slot;
delete from committee_deregistration where slot > :slot;
delete from committee_member where slot > :slot;
delete from committee_registration where slot > :slot;
delete from constitution where slot > :slot;
delete from voting_procedure where slot > :slot;



```

## Useful Query

Utxo balance of a stake_address from address_utxo before epoch x (example: x=184)

```
SELECT sum(lovelace_amount)
FROM address_utxo a
         LEFT JOIN tx_input t
                   ON a.tx_hash = t.tx_hash
                       AND a.output_index = t.output_index
                       AND t.spent_epoch < 184
WHERE a.owner_stake_addr = 'stake_test1uzc68v00j3sdc7l03l7lf88yuqd3esjst3s5ae3txg3lgkqsey2uy' and a.epoch < 184
  AND (t.tx_hash IS NULL OR t.spent_epoch >= 184);
```
