package com.bloxbean.cardano.yaci.store.governance.domain;

import com.bloxbean.cardano.yaci.core.model.CredentialType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CommitteeMemberDetails {
    private String coldKey;
    private String hotKey;
    private CredentialType credType;
    private Integer startEpoch;
    private Integer expiredEpoch;
}
