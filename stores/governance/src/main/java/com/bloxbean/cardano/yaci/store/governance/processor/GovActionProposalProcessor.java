package com.bloxbean.cardano.yaci.store.governance.processor;

import com.bloxbean.cardano.client.address.Address;
import com.bloxbean.cardano.client.util.HexUtil;
import com.bloxbean.cardano.yaci.core.model.Credential;
import com.bloxbean.cardano.yaci.core.model.ProtocolParamUpdate;
import com.bloxbean.cardano.yaci.core.model.governance.ProposalProcedure;
import com.bloxbean.cardano.yaci.core.model.governance.actions.ParameterChangeAction;
import com.bloxbean.cardano.yaci.store.events.EventMetadata;
import com.bloxbean.cardano.yaci.store.events.GovernanceEvent;
import com.bloxbean.cardano.yaci.store.events.domain.TxGovernance;
import com.bloxbean.cardano.yaci.store.governance.domain.GovActionProposal;
import com.bloxbean.cardano.yaci.store.governance.jackson.CredentialSerializer;
import com.bloxbean.cardano.yaci.store.governance.storage.GovActionProposalStorage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class GovActionProposalProcessor {

    private final GovActionProposalStorage govActionProposalStorage;
    private final ObjectMapper objectMapper;

    public GovActionProposalProcessor(GovActionProposalStorage govActionProposalStorage) {
        this.govActionProposalStorage = govActionProposalStorage;

        this.objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addKeySerializer(Credential.class, new CredentialSerializer());
        this.objectMapper.registerModule(module);

        log.info("GovActionProposalProcessor initialized");
    }


    @EventListener
    @Transactional
    @SneakyThrows
    public void handleGovernanceAction(GovernanceEvent governanceEvent) {
        EventMetadata eventMetadata = governanceEvent.getMetadata();

        List<GovActionProposal> govActionProposals = new ArrayList<>();
        for (TxGovernance txGovernance : governanceEvent.getTxGovernanceList()) {
            String txHash = txGovernance.getTxHash();

            List<ProposalProcedure> proposalProcedures = txGovernance.getProposalProcedures();

            if (proposalProcedures == null || proposalProcedures.isEmpty()) {
                continue;
            }

            int index = 0;
            for (ProposalProcedure proposalProcedure : proposalProcedures) {
                GovActionProposal govActionProposal = new GovActionProposal();

                govActionProposal.setTxHash(txHash);
                govActionProposal.setIndex(index++);
                govActionProposal.setType(proposalProcedure.getGovAction().getType());

                if (proposalProcedure.getGovAction() instanceof ParameterChangeAction action) {
                    ProtocolParamUpdate protocolParamUpdate = action.getProtocolParamUpdate();
                        Map<String, Object> fields = new HashMap<>();
                        
                        if (protocolParamUpdate != null) {
                            Map<String, Object> protocolParamUpdateMap = objectMapper.convertValue(protocolParamUpdate, Map.class);
                            protocolParamUpdateMap.entrySet().removeIf(entry -> entry.getValue() == null);
                            fields.put("protocolParamUpdate", protocolParamUpdateMap);
                        }

                        fields.put("type", action.getType());
                        fields.put("govActionId", action.getGovActionId());
                        fields.put("policyHash", action.getPolicyHash());

                        govActionProposal.setDetails(objectMapper.valueToTree(fields));
                } else {
                    govActionProposal.setDetails(objectMapper.valueToTree(proposalProcedure.getGovAction()));
                }

                govActionProposal.setDeposit(proposalProcedure.getDeposit());

                Address address = new Address(HexUtil.decodeHexString(proposalProcedure.getRewardAccount()));
                govActionProposal.setReturnAddress(address.getAddress());

                if (proposalProcedure.getAnchor() != null) {
                    govActionProposal.setAnchorUrl(proposalProcedure.getAnchor().getAnchor_url());
                    govActionProposal.setAnchorHash(proposalProcedure.getAnchor().getAnchor_data_hash());
                }

                govActionProposal.setBlockNumber(eventMetadata.getBlock());
                govActionProposal.setBlockTime(eventMetadata.getBlockTime());
                govActionProposal.setSlot(eventMetadata.getSlot());
                govActionProposal.setEpoch(eventMetadata.getEpochNumber());

                govActionProposals.add(govActionProposal);
            }
        }

        if (!govActionProposals.isEmpty()) {
            govActionProposalStorage.saveAll(govActionProposals);
        }
    }
}
