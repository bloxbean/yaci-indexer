package com.bloxbean.cardano.yaci.store.utxo.storage.impl;

import com.bloxbean.cardano.client.address.Address;
import com.bloxbean.cardano.yaci.core.util.HexUtil;
import com.bloxbean.cardano.yaci.store.common.domain.AddressUtxo;
import com.bloxbean.cardano.yaci.store.common.domain.Amt;
import com.bloxbean.cardano.yaci.store.common.domain.TxInput;
import com.bloxbean.cardano.yaci.store.events.internal.CommitEvent;
import com.bloxbean.cardano.yaci.store.utxo.storage.impl.mapper.UtxoMapper;
import com.bloxbean.cardano.yaci.store.utxo.storage.impl.model.TxInputEntity;
import com.bloxbean.cardano.yaci.store.utxo.storage.impl.repository.TxInputRepository;
import com.bloxbean.cardano.yaci.store.utxo.storage.impl.repository.UtxoRepository;
import org.jooq.DSLContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;


@DataJpaTest
class UtxoStorageImplTest {


    @Autowired
    private UtxoRepository utxoRepository;
    @Autowired
    private TxInputRepository spentOutputRepository;
    @Autowired
    private DSLContext dsl;
    @Autowired
    private UtxoCache utxoCache;
    String address1 = "addr_test1xrk9x99ey79w25d3eyqd7zy0qmecl3h4ngqvy5w2yp06nxyhzcvnyv2r9xr86rm4f7dragz4ckudkrs06nylfrwllzcs63c0dy";
    String address2 = "addr_test1wr64gtafm8rpkndue4ck2nx95u4flhwf643l2qmg9emjajg2ww0nj";
    String paymentCred1 = HexUtil.encodeHexString(new Address(address1).getPaymentCredential().get().getBytes());
    String paymentCred2 = HexUtil.encodeHexString(new Address(address2).getPaymentCredential().get().getBytes());
    @Test
    public void findSpentBySlotGreaterThanEmpty() {
        var utxos = getUtxos();

        var spentUtxo1 = TxInput.builder()
                .txHash(utxos.get(0).getTxHash())
                .outputIndex(utxos.get(0).getOutputIndex())
                .spentAtBlock(6000L)
                .spentAtSlot(6000L)
                .spentTxHash("1f50e930ade51be0a99672d07c4ae17d66c9da4568293723a084371be4b88a9b")
                .spentAtBlockHash("2f50e930ade51be0a99672d07c4ae17d66c9da4568293723a084371be4b88a9b")
                .build();

        var spentUtxo2 = TxInput.builder()
                .txHash(utxos.get(3).getTxHash())
                .outputIndex(utxos.get(3).getOutputIndex())
                .spentAtBlock(6000L)
                .spentAtSlot(6000L)
                .spentTxHash("1f50e930ade51be0a99672d07c4ae17d66c9da4568293723a084371be4b88a9b")
                .spentAtBlockHash("2f50e930ade51be0a99672d07c4ae17d66c9da4568293723a084371be4b88a9b")
                .build();
        var spentUtxo3 = TxInput.builder()
                .txHash(utxos.get(1).getTxHash())
                .outputIndex(utxos.get(1).getOutputIndex())
                .spentAtBlock(1000L)
                .spentAtSlot(1000L)
                .spentTxHash("1f50e930ade51be0a99672d07c4ae17d66c9da4568293723a084371be4b88a9b")
                .spentAtBlockHash("2f50e930ade51be0a99672d07c4ae17d66c9da4568293723a084371be4b88a9b")
                .build();
        UtxoStorageImpl utxoStorage = new UtxoStorageImpl(utxoRepository, spentOutputRepository, dsl, utxoCache);

        //utxoStorage.saveUnspent(getUtxos());
        //utxoStorage.handleCommit(new CommitEvent(null, null));
        utxoStorage.saveSpent(List.of(spentUtxo1,spentUtxo2,spentUtxo3));
        //utxoStorage.handleCommit(new CommitEvent(null, null));

        List<TxInput> spentBySlotGreaterThan = utxoStorage.findSpentBySlotGreaterThan(1000L);
        List<TxInput> expected = new ArrayList<>();
        expected.add(spentUtxo1);
        expected.add(spentUtxo2);

        Assertions.assertEquals(expected,spentBySlotGreaterThan);
    }

    private List<AddressUtxo> getUtxos() {
        var addressUtxo1 = AddressUtxo
                .builder()
                .txHash("1f50e930ade51be0a99672d07c4ae17d66c9da4568293723a084371be4b88a9b")
                .outputIndex(0)
                .ownerAddr(address1)
                .ownerStakeAddr(null)
                .ownerPaymentCredential(paymentCred1)
                .amounts(List.of(Amt.builder().unit("lovelace").quantity(BigInteger.valueOf(1000000L)).build()))
                .blockHash("1f50e930ade51be0a99672d07c4ae17d66c9da4568293723a084371be4b88a9b")
                .blockNumber(1000L)
                .slot(1000L)
                .build();

        var addressUtxo2 = AddressUtxo
                .builder()
                .txHash("2f50e930ade51be0a99672d07c4ae17d66c9da4568293723a084371be4b88a9b")
                .outputIndex(1)
                .ownerAddr(address1)
                .ownerStakeAddr(null)
                .ownerPaymentCredential(paymentCred1)
                .amounts(List.of(Amt.builder().unit("lovelace").quantity(BigInteger.valueOf(2000000L)).build()))
                .blockHash("1f50e930ade51be0a99672d07c4ae17d66c9da4568293723a084371be4b88a9b")
                .blockNumber(1000L)
                .slot(2000L)
                .build();

        var addressUtxo3 = AddressUtxo
                .builder()
                .txHash("3f50e930ade51be0a99672d07c4ae17d66c9da4568293723a084371be4b88a9b")
                .outputIndex(5)
                .ownerAddr(address1)
                .ownerStakeAddr(null)
                .ownerPaymentCredential(paymentCred1)
                .amounts(List.of(Amt.builder().unit("lovelace").quantity(BigInteger.valueOf(3000000L)).build()))
                .blockHash("1f50e930ade51be0a99672d07c4ae17d66c9da4568293723a084371be4b88a9b")
                .blockNumber(1000L)
                .slot(3000L)
                .build();

        var addressUtxo4 = AddressUtxo
                .builder()
                .txHash("ff50e930ade51be0a99672d07c4ae17d66c9da4568293723a084371be4b88a9b")
                .outputIndex(1)
                .ownerAddr(address2)
                .ownerStakeAddr(null)
                .ownerPaymentCredential(paymentCred2)
                .amounts(List.of(Amt.builder().unit("lovelace").quantity(BigInteger.valueOf(9000000L)).build()))
                .blockHash("1f50e930ade51be0a99672d07c4ae17d66c9da4568293723a084371be4b88a9b")
                .blockNumber(1000L)
                .slot(5000L)
                .build();

        return List.of(addressUtxo1, addressUtxo2, addressUtxo3, addressUtxo4);
    }
}