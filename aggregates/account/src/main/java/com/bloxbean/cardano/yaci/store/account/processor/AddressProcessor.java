package com.bloxbean.cardano.yaci.store.account.processor;

import com.bloxbean.cardano.client.address.AddressProvider;
import com.bloxbean.cardano.yaci.core.util.HexUtil;
import com.bloxbean.cardano.yaci.store.account.AccountStoreProperties;
import com.bloxbean.cardano.yaci.store.account.domain.Address;
import com.bloxbean.cardano.yaci.store.account.storage.AddressStorage;
import com.bloxbean.cardano.yaci.store.cache.GuavaCache;
import com.bloxbean.cardano.yaci.store.cache.NoCache;
import com.bloxbean.cardano.yaci.store.common.cache.Cache;
import com.bloxbean.cardano.yaci.store.common.cache.MVMapCache;
import com.bloxbean.cardano.yaci.store.common.cache.MVStoreFactory;
import com.bloxbean.cardano.yaci.store.common.config.StoreProperties;
import com.bloxbean.cardano.yaci.store.events.GenesisBlockEvent;
import com.bloxbean.cardano.yaci.store.events.RollbackEvent;
import com.bloxbean.cardano.yaci.store.events.internal.CommitEvent;
import com.bloxbean.cardano.yaci.store.utxo.domain.AddressUtxoEvent;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class AddressProcessor {
    private final AddressStorage addressStorage;
    private final AccountStoreProperties accountStoreProperties;
    private final StoreProperties storeProperties;

    private Cache<String, String> cache;
    private Map<String, Address> addresseCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        log.info("-- Address Processor Initialized with address cache size: {}, expiry: {} min"
                , accountStoreProperties.getAddressCacheSize(), accountStoreProperties.getAddressCacheExpiryAfterAccess());

        if (accountStoreProperties.isAddressCacheEnabled() && !storeProperties.isMvstoreEnabled()) {
            cache = new GuavaCache<>(accountStoreProperties.getAddressCacheSize(),
                    accountStoreProperties.getAddressCacheExpiryAfterAccess());
            log.info("<< Using Guava Cache for Address >>");
        } else if (!accountStoreProperties.isAddressCacheEnabled()) {
            log.info("<< Address Cache is disabled >>");
            cache = new NoCache<>();
        }
    }

    @EventListener
    public void handleAddress(AddressUtxoEvent addressUtxoEvent) {
        if (cache == null && accountStoreProperties.isAddressCacheEnabled() && storeProperties.isMvstoreEnabled()) {
            cache = new MVMapCache<>(MVStoreFactory.getInstance().getStore(), "address");
            log.info("<< Using MVStore Cache for Address >>");
        }

        //Get Addresses
        addressUtxoEvent.getTxInputOutputs()
                .stream().flatMap(txInputOutput -> txInputOutput.getOutputs().stream())
                .filter(addressUtxo -> cache.get(addressUtxo.getOwnerAddr()) == null)
                .forEach(addressUtxo -> {
                    var address = Address.builder()
                            .address(addressUtxo.getOwnerAddr())
                            .stakeAddress(addressUtxo.getOwnerStakeAddr())
                            .paymentCredential(addressUtxo.getOwnerPaymentCredential())
                            .stakeCredential(addressUtxo.getOwnerStakeCredential())
                            .build();

                    addresseCache.putIfAbsent(address.getAddress(), address);
                });
    }

    @EventListener
    @Transactional
    public void handleCommit(CommitEvent commitEvent) {
        try {
            if (addresseCache.size() > 0) {
                long t1 = System.currentTimeMillis();
                addressStorage.save(addresseCache.values());

                if (!commitEvent.getMetadata().isSyncMode()) { //Store only for initial sync
                    addresseCache.values()
                            .forEach(address -> cache.put(address.getAddress(), ""));
                }

                long t2 = System.currentTimeMillis();
                log.info("Address save size : {}, time: {} ms", addresseCache.size(), (t2 - t1));
                log.info("Address Cache Size: {}", cache.size());
            }
        } finally {
            addresseCache.clear();
        }
    }

    @EventListener
    @Transactional
    public void handleGenesisBalances(GenesisBlockEvent genesisBlockEvent) {
        var genesisBalances = genesisBlockEvent.getGenesisBalances();
        if (genesisBalances == null || genesisBalances.isEmpty()) {
            log.info("No genesis balances found");
            return;
        }

        Set<Address> addresses = genesisBalances.stream().map(genesisBalance -> {
            String paymentCredential = null;
            String stakeCredential = null;
            String stakeAddress = null;
            try {
                com.bloxbean.cardano.client.address.Address address = new com.bloxbean.cardano.client.address.Address(genesisBalance.getAddress());
                paymentCredential = address.getPaymentCredential().map(credential -> HexUtil.encodeHexString(credential.getBytes()))
                        .orElse(null);

                stakeCredential = address.getDelegationCredential().map(delegCred -> HexUtil.encodeHexString(delegCred.getBytes()))
                        .orElse(null);
                stakeAddress = address.getDelegationCredential().map(delegCred -> AddressProvider.getStakeAddress(address).toBech32())
                        .orElse(null);
            } catch (Exception e) {
                //Not a valid shelley address
            }

            return Address.builder()
                    .address(genesisBalance.getAddress())
                    .paymentCredential(paymentCredential)
                    .stakeCredential(stakeCredential)
                    .stakeAddress(stakeAddress)
                    .build();
        }).collect(Collectors.toSet());

        addressStorage.save(addresses);
    }

    @EventListener
    @Transactional
    public void handleRollback(RollbackEvent rollbackEvent) {
        addresseCache.clear();
        if (cache != null)
            cache.clear();
    }
}
