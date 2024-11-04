drop table if exists gov_action_proposal_info;
create table gov_action_proposal_info
(
    tx_hash         varchar(64) not null,
    idx             int         not null,
    expiration      int,
    ratified_epoch  int,
    enacted_epoch   int,
    dropped_epoch   int,
    status          varchar(64),
    create_datetime timestamp,
    update_datetime timestamp,
    primary key (tx_hash, idx)
);

drop table if exists latest_voting_procedure;
create table if not exists latest_voting_procedure
(
    id                 uuid        not null,
    tx_hash            varchar(64)  not null,
    idx                int         not null,
    voter_type         varchar(50),
    voter_hash         varchar(56),
    gov_action_tx_hash varchar(64),
    gov_action_index   int,
    vote_in_prev_aggr_slot  varchar(10),
    vote               varchar(10),
    anchor_url         varchar,
    anchor_hash        varchar(64),
    epoch              int,
    slot               bigint,
    block              bigint,
    block_time         bigint,
    update_datetime    timestamp,
    repeat_vote        boolean,
    primary key (voter_hash, gov_action_tx_hash, gov_action_index)
    );

drop table if exists committee_vote;
create table if not exists committee_vote
(
    gov_action_tx_hash varchar(64),
    gov_action_index   int,
    yes_cnt            int,
    no_cnt             int,
    abstain_cnt        int,
    voter_hash         varchar(56),
    vote               varchar(10),
    status             varchar(64),
    slot               bigint,
    update_datetime    timestamp,
    primary key (gov_action_tx_hash, gov_action_index, voter_hash, slot)
    );