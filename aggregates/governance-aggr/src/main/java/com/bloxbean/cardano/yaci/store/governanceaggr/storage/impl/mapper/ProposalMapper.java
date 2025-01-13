package com.bloxbean.cardano.yaci.store.governanceaggr.storage.impl.mapper;

import com.bloxbean.cardano.yaci.core.model.governance.GovActionId;
import com.bloxbean.cardano.yaci.core.model.governance.actions.*;
import com.bloxbean.cardano.yaci.store.common.domain.GovActionProposal;
import com.bloxbean.cardano.yaci.store.governanceaggr.domain.Proposal;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProposalMapper {
    private final ObjectMapper objectMapper;

    public Proposal toProposal(GovActionProposal govActionProposal) throws JsonProcessingException {
        return Proposal.builder()
                .type(govActionProposal.getType())
                .govActionId(GovActionId.builder()
                        .gov_action_index(govActionProposal.getIndex())
                        .transactionId(govActionProposal.getTxHash())
                        .build())
                .previousGovActionId(getPrevGovActionId(govActionProposal))
                .build();
    }

    private GovActionId getPrevGovActionId(GovActionProposal govActionProposal) throws JsonProcessingException {
        return switch (govActionProposal.getType()) {
            case TREASURY_WITHDRAWALS_ACTION, INFO_ACTION -> null;
            case HARD_FORK_INITIATION_ACTION -> {
                var govAction = objectMapper.treeToValue(govActionProposal.getDetails(), HardForkInitiationAction.class);
                yield govAction.getGovActionId();
            }
            case NO_CONFIDENCE -> {
                var govAction = objectMapper.treeToValue(govActionProposal.getDetails(), NoConfidence.class);
                yield govAction.getGovActionId();
            }
            case UPDATE_COMMITTEE -> {
                var govAction = objectMapper.treeToValue(govActionProposal.getDetails(), UpdateCommittee.class);
                yield govAction.getGovActionId();
            }
            case NEW_CONSTITUTION -> {
                var govAction = objectMapper.treeToValue(govActionProposal.getDetails(), NewConstitution.class);
                yield govAction.getGovActionId();
            }
            case PARAMETER_CHANGE_ACTION -> {
                var govAction = objectMapper.treeToValue(govActionProposal.getDetails(), ParameterChangeAction.class);
                yield govAction.getGovActionId();
            }
        };
    }

}
