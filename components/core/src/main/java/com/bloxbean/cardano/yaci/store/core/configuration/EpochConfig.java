package com.bloxbean.cardano.yaci.store.core.configuration;

import com.bloxbean.cardano.yaci.core.model.Era;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("EpochConfig")
@RequiredArgsConstructor
public class EpochConfig {
    private final GenesisConfig genesisConfig;

    public int epochFromSlot(long shelleyStartSlot, Era era, long slot) {
        if (era == Era.Byron) {
//            return (int) (slot / genesisConfig.slotsPerEpoch(era));
            throw new IllegalArgumentException("epochFromSlot should not be called for Byron era. " +
                    "Byron era block already has epoch info");
        } else {
            long shelleyStartEpoch = shelleyStartSlot / genesisConfig.slotsPerEpoch(Era.Byron);
            long epochsAfterShelley = (slot - shelleyStartSlot) / genesisConfig.slotsPerEpoch(Era.Shelley);
            return (int) (shelleyStartEpoch + epochsAfterShelley);
        }
    }
}
