package com.bloxbean.cardano.yaci.store.utxo.processor;

import com.bloxbean.cardano.yaci.core.model.Amount;
import com.bloxbean.cardano.yaci.core.model.byron.ByronTx;
import com.bloxbean.cardano.yaci.core.model.byron.ByronTxOut;
import com.bloxbean.cardano.yaci.helper.model.Utxo;
import com.bloxbean.cardano.yaci.store.common.domain.AddressUtxo;
import com.bloxbean.cardano.yaci.store.common.domain.Amt;
import com.bloxbean.cardano.yaci.store.common.domain.UtxoKey;
import com.bloxbean.cardano.yaci.store.events.ByronMainBlockEvent;
import com.bloxbean.cardano.yaci.store.events.EventMetadata;
import com.bloxbean.cardano.yaci.store.utxo.domain.AddressUtxoEvent;
import com.bloxbean.cardano.yaci.store.utxo.storage.api.UtxoStorage;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.bloxbean.cardano.yaci.core.util.Constants.LOVELACE;

@Component
@RequiredArgsConstructor
@Slf4j
public class ByronUtxoProcessor {
    private final UtxoStorage utxoStorage;
    private final ApplicationEventPublisher publisher;

    @EventListener
    @Transactional
    public void handleByronTransactionEvent(ByronMainBlockEvent event) {
        EventMetadata metadata = event.getEventMetadata();
        List<ByronTx> byronTxList = event.getByronMainBlock().getBody().getTxPayload()
                .stream()
                .map(byronTxPayload -> byronTxPayload.getTransaction())
                .collect(Collectors.toList());

        for (ByronTx byronTx : byronTxList) {
            //set spent for input
            List<AddressUtxo> inputAddressUtxos = byronTx.getInputs().stream()
                    .map(txIn -> new UtxoKey(txIn.getTxId(), txIn.getIndex()))
                    .map(utxoKey -> {
                        AddressUtxo addressUtxo = utxoStorage.findById(utxoKey.getTxHash(), utxoKey.getOutputIndex())
                                .orElse(AddressUtxo.builder()        //If not present, then create a record with pk
                                        .txHash(utxoKey.getTxHash())
                                        .outputIndex(utxoKey.getOutputIndex()).build());
                        addressUtxo.setSpent(true);
                        addressUtxo.setSpentAtSlot(metadata.getSlot());
                        addressUtxo.setSpentEpoch(metadata.getEpochNumber());
                        addressUtxo.setSpentTxHash(byronTx.getTxHash());
                        return addressUtxo;
                    }).collect(Collectors.toList());

            List<Utxo> utxos = getUtxosFromByronOutput(byronTx);
            List<AddressUtxo> outputAddressUtxos = utxos.stream()
                    .map(utxo -> getAddressUtxo(metadata, utxo))
                    .map(addressUtxo -> { //Check if utxo is already there, only possible in a multi-instance environment
                        utxoStorage.findById(addressUtxo.getTxHash(), addressUtxo.getOutputIndex())
                                .ifPresent(existingAddressUtxo -> addressUtxo.setSpent(existingAddressUtxo.getSpent()));
                        return addressUtxo;
                    })
                    .collect(Collectors.toList());

            if (outputAddressUtxos.size() > 0) //unspent utxos
                utxoStorage.saveAll(outputAddressUtxos);

            //Update existing utxos as spent
            if (inputAddressUtxos.size() > 0) //spent utxos
                utxoStorage.saveAll(inputAddressUtxos);

            //publish event
            if (outputAddressUtxos.size() > 0)
                publisher.publishEvent(new AddressUtxoEvent(metadata, outputAddressUtxos));
        }
    }

    private List<Utxo> getUtxosFromByronOutput(ByronTx byronTx) {
        List<Utxo> utxos = new ArrayList<>();
        for (int index = 0; index < byronTx.getOutputs().size(); index++) {
            ByronTxOut txOut = byronTx.getOutputs().get(index);
            Utxo utxo = Utxo.builder()
                    .txHash(byronTx.getTxHash())
                    .index(index)
                    .address(txOut.getAddress().getBase58Raw())
                    .amounts(List.of(Amount.builder()
                            .unit(LOVELACE)
                            .assetName(LOVELACE)
                            .quantity(txOut.getAmount())
                            .build()))
                    .build();
            utxos.add(utxo);
        }

        return utxos;
    }

    private AddressUtxo getAddressUtxo(@NonNull EventMetadata eventMetadata, @NonNull Utxo utxo) {
        //Fix -- some asset name contains \u0000 -- postgres can't convert this to text. so replace
        List<Amt> amounts = utxo.getAmounts().stream().map(amount ->
                        Amt.builder()
                                .unit(amount.getUnit() != null? amount.getUnit().replace(".", ""): null) //remove . from unit as yaci provides policy.assetName as unit
                                .policyId(amount.getPolicyId())
                                .assetName(amount.getAssetName().replace('\u0000', ' '))
                                .quantity(amount.getQuantity())
                                .build())
                .collect(Collectors.toList());

        BigInteger lovelaceAmount = amounts.stream()
                .filter(amount -> LOVELACE.equals(amount.getUnit()))
                .findFirst()
                .map(Amt::getQuantity).orElse(BigInteger.ZERO);

        String stakeAddress = null;
        String paymentKeyHash = null;
        String stakeKeyHash = null;

        return AddressUtxo.builder()
                .slot(eventMetadata.getSlot())
                .block(eventMetadata.getBlock())
                .blockHash(eventMetadata.getBlockHash())
                .epoch(eventMetadata.getEpochNumber())
                .txHash(utxo.getTxHash())
                .outputIndex(utxo.getIndex())
                .ownerAddr(utxo.getAddress())
                .ownerStakeAddr(stakeAddress)
                .ownerPaymentCredential(paymentKeyHash)
                .ownerStakeCredential(stakeKeyHash)
                .lovelaceAmount(lovelaceAmount)
                .amounts(amounts)
                .build();
    }

}