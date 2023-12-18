package com.bloxbean.cardano.yaci.store.transaction.storage.impl.mapper;

import com.bloxbean.cardano.yaci.store.transaction.domain.Txn;
import com.bloxbean.cardano.yaci.store.transaction.domain.TxnWitness;
import com.bloxbean.cardano.yaci.store.transaction.storage.impl.model.TxnEntity;
import com.bloxbean.cardano.yaci.store.transaction.storage.impl.model.TxnWitnessEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public abstract class TxnMapper {
    public abstract TxnEntity toTxnEntity(Txn txn);
    public abstract Txn toTxn(TxnEntity entity);

    public abstract TxnWitness toTxnWitness(TxnWitnessEntity entity);
    public abstract TxnWitnessEntity toTxnWitnessEntity(TxnWitness txnWitness);
}