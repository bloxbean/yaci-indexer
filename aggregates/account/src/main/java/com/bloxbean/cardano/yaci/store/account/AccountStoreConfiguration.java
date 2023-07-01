package com.bloxbean.cardano.yaci.store.account;

import com.bloxbean.cardano.yaci.store.account.storage.AccountBalanceStorage;
import com.bloxbean.cardano.yaci.store.account.storage.impl.jpa.AccountBalanceStorageImpl;
import com.bloxbean.cardano.yaci.store.account.storage.impl.jpa.mapper.AccountMapper;
import com.bloxbean.cardano.yaci.store.account.storage.impl.jpa.repository.AddressBalanceRepository;
import com.bloxbean.cardano.yaci.store.account.storage.impl.jpa.repository.StakeBalanceRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@ConditionalOnProperty(
        prefix = "store.account",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
@ComponentScan(basePackages = {"com.bloxbean.cardano.yaci.store.account"})
@EnableJpaRepositories( basePackages = {"com.bloxbean.cardano.yaci.store.account"})
@EntityScan(basePackages = {"com.bloxbean.cardano.yaci.store.account"})
@EnableTransactionManagement
public class AccountStoreConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AccountBalanceStorage accountBalanceStorage(AddressBalanceRepository addressBalanceRepository,
                                                       StakeBalanceRepository stakeBalanceRepository) {
        return new AccountBalanceStorageImpl(addressBalanceRepository, stakeBalanceRepository);
    }
}