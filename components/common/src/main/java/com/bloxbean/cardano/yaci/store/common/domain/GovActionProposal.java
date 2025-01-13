package com.bloxbean.cardano.yaci.store.common.domain;

import com.bloxbean.cardano.yaci.core.model.governance.GovActionType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigInteger;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
// TODO: should add prevGovActionId?
public class GovActionProposal extends BlockAwareDomain {

    private String txHash;

    private int index;

    private Long slot;

    private BigInteger deposit;

    private String returnAddress;

    private GovActionType type;

    private JsonNode details;

    private String anchorUrl;

    private String anchorHash;

    private Integer epoch;
}
