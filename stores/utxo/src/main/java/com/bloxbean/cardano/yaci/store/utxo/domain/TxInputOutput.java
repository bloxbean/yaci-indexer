package com.bloxbean.cardano.yaci.store.utxo.domain;

import com.bloxbean.cardano.yaci.store.common.domain.AddressUtxo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TxInputOutput {
    private String txHash;
    private List<AddressUtxo> inputs;
    private List<AddressUtxo> outputs;
}