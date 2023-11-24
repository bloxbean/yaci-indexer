package com.bloxbean.cardano.yaci.store.starter.account;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "store", ignoreUnknownFields = true)
public class AccountStoreAutoConfigProperties {
    private Account account;

    @Getter
    @Setter
    public static final class Account {
        private boolean enabled = false;
        private boolean apiEnabled = false;

        private boolean balanceAggregationEnabled = false;
        private boolean historyCleanupEnabled = false;

        private int maxBalanceRecordsPerAddressPerBatch = 3;
        private boolean stakeAddressBalanceEnabled = true;
    }

}
