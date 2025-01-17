package com.bloxbean.cardano.yaci.store.governanceaggr.domain;

import com.bloxbean.cardano.yaci.core.model.governance.GovActionId;
import com.bloxbean.cardano.yaci.core.model.governance.GovActionType;
import lombok.*;

@Builder
@EqualsAndHashCode
@Getter
@ToString
public class Proposal {
    private GovActionId govActionId;
    private GovActionId previousGovActionId;
    private GovActionType type;

    public Proposal(GovActionId govActionId, GovActionId previousGovActionId, GovActionType type) {
        this.govActionId = govActionId;
        this.previousGovActionId = previousGovActionId;
        this.type = type;

        if (previousGovActionId != null &&
                (type.equals(GovActionType.INFO_ACTION) || type.equals(GovActionType.TREASURY_WITHDRAWALS_ACTION))) {
            throw new IllegalArgumentException("INFO ACTION and TREASURY WITHDRAWALS ACTION can not have prev gov action id");
        }
    }
}
