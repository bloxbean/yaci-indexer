drop table if exists adapot;
create table adapot
(
    epoch               int primary key,
    slot                bigint,
    deposits_stake      numeric(38),
    fees                numeric(38),
    utxo                numeric(38),
    treasury            numeric(38),
    reserves            numeric(38),
    rewards             numeric(38),
    deposits_drep       numeric(38),
    deposits_proposal   numeric(38),
    block               bigint,
    block_time          bigint,
    update_datetime     timestamp
);

drop table if exists epoch_stake;
create table epoch_stake
(
    epoch              integer,
    address            varchar(255),
    amount             numeric(38),
    pool_id            varchar(56),
    delegation_epoch   integer,
    active_epoch       integer,
    create_datetime    timestamp,
    primary key (epoch, address)
);

create index epoch_stake_active_epoch_address_index
    on epoch_stake (active_epoch, address);

drop table if exists instant_reward;
create table instant_reward
(
    address         varchar(255),
    type            varchar(50),
    amount          numeric(38),
    earned_epoch    integer,
    spendable_epoch integer,
    slot            bigint,
    create_datetime timestamp,
    primary key (address, type, earned_epoch)
);

drop table if exists reward;
create table reward
(
    address         varchar(255),
    earned_epoch    integer,
    type            varchar(50),
    pool_id         varchar(56),
    amount          numeric(38),
    spendable_epoch integer,
    slot            bigint,
    update_datetime timestamp,
    primary key (address, earned_epoch, type, pool_id)
);

drop table if exists reward_rest;
create table reward_rest
(
    id              uuid  primary key,
    address         varchar(255),
    type            varchar(50),
    amount          numeric(38),
    earned_epoch    integer,
    spendable_epoch integer,
    slot            bigint,
    create_datetime timestamp
);

drop table if exists adapot_jobs;
create table adapot_jobs
(
    epoch  integer primary key ,
    slot   bigint,
    type   varchar(30) not null,
    status varchar(30)   not null,
    total_time bigint,
    reward_calc_time bigint,
    update_reward_time bigint,
    stake_snapshot_time bigint,
    error_message text
);