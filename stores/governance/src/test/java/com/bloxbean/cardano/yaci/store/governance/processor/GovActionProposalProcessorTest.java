package com.bloxbean.cardano.yaci.store.governance.processor;

import com.bloxbean.cardano.yaci.core.model.Credential;
import com.bloxbean.cardano.yaci.core.model.ProtocolParamUpdate;
import com.bloxbean.cardano.yaci.core.model.ProtocolVersion;
import com.bloxbean.cardano.yaci.core.model.certs.StakeCredType;
import com.bloxbean.cardano.yaci.core.model.governance.*;
import com.bloxbean.cardano.yaci.core.model.governance.actions.*;
import com.bloxbean.cardano.yaci.core.util.Tuple;
import com.bloxbean.cardano.yaci.store.events.EventMetadata;
import com.bloxbean.cardano.yaci.store.events.GovernanceEvent;
import com.bloxbean.cardano.yaci.store.events.domain.TxGovernance;
import com.bloxbean.cardano.yaci.store.governance.domain.GovActionProposal;
import com.bloxbean.cardano.yaci.store.governance.storage.GovActionProposalStorage;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import static com.bloxbean.cardano.yaci.core.model.governance.GovActionType.INFO_ACTION;
import static com.bloxbean.cardano.yaci.core.model.governance.GovActionType.UPDATE_COMMITTEE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GovActionProposalProcessorTest {

    @Mock
    private GovActionProposalStorage govActionProposalStorage;

    @Captor
    private ArgumentCaptor<List<GovActionProposal>> govActionProposalsCaptor;

    private GovActionProposalProcessor govActionProposalProcessor;

    @BeforeEach
    void setUp() {
        govActionProposalProcessor = new GovActionProposalProcessor(govActionProposalStorage);
    }

    @Test
    void givenGovernanceEvent_WhenNotExistsProposalProcedure_ShouldNotSaveAnything() {
        final GovernanceEvent governanceEvent = GovernanceEvent.builder()
                .metadata(eventMetadata())
                .txGovernanceList(List.of(
                        TxGovernance.builder().proposalProcedures(List.of()).build(),
                        TxGovernance.builder().proposalProcedures(null).build()
                ))
                .build();

        govActionProposalProcessor.handleGovernanceAction(governanceEvent);

        verify(govActionProposalStorage, never()).saveAll(any());
    }

    @Test
    void givenGovernanceEvent_ShouldHandleGovernanceEventAndSaveGovernanceProposals() {
        final GovernanceEvent governanceEvent = GovernanceEvent.builder()
                .metadata(eventMetadata())
                .txGovernanceList(
                        List.of(
                                TxGovernance.builder()
                                        .txHash("498e6ee7063e94c6f459257a89cbd0bc953c5409e6b24460900cc997ba9d1f2f")
                                        .proposalProcedures(proposalProcedures())
                                        .build()
                        )
                )
                .build();

        govActionProposalProcessor.handleGovernanceAction(governanceEvent);

        verify(govActionProposalStorage, times(1)).saveAll(govActionProposalsCaptor.capture());

        List<GovActionProposal> govActionProposalsSaved = govActionProposalsCaptor.getValue();
        assertThat(govActionProposalsSaved).hasSize(2);

        for (var govActionProposal : govActionProposalsSaved) {
            assertThat(govActionProposal.getSlot()).isEqualTo(eventMetadata().getSlot());
            assertThat(govActionProposal.getBlockNumber()).isEqualTo(eventMetadata().getBlock());
            assertThat(govActionProposal.getBlockTime()).isEqualTo(eventMetadata().getBlockTime());
            assertThat(govActionProposal.getTxHash()).isEqualTo("498e6ee7063e94c6f459257a89cbd0bc953c5409e6b24460900cc997ba9d1f2f");
            assertThat(govActionProposal.getEpoch()).isEqualTo(eventMetadata().getEpochNumber());
        }

        assertThat(govActionProposalsSaved).map(GovActionProposal::getIndex).contains(0L, 1L);
        assertThat(govActionProposalsSaved).map(GovActionProposal::getType).contains(INFO_ACTION, UPDATE_COMMITTEE);
        assertThat(govActionProposalsSaved).map(GovActionProposal::getReturnAddress).contains(
                "stake_test1urvwycygqdv0pgs0vvjnsjt4w4934lvlu3w3vntg57w5dqgt4prgt",
                "stake_test1upuhx0upngft6d832ds2sug76c76ynm94mehgmd4rfm4tnsxs62hr"
        );

        assertThat(govActionProposalsSaved.get(0).getAnchorUrl()).isEqualTo(proposalProcedures().get(0).getAnchor().getAnchor_url());
        assertThat(govActionProposalsSaved.get(0).getAnchorHash()).isEqualTo(proposalProcedures().get(0).getAnchor().getAnchor_data_hash());
        assertThat(govActionProposalsSaved.get(1).getAnchorUrl()).isNull();
        assertThat(govActionProposalsSaved.get(1).getAnchorHash()).isNull();

        // test json
        JsonNode details = govActionProposalsSaved.get(0).getDetails();
        assertThat(details.get("type").asText())
                .isEqualTo("UPDATE_COMMITTEE");

        JsonNode govActionIdNode = details.get("govActionId");
        assertThat(govActionIdNode.get("transactionId").asText())
                .isEqualTo("bd5d786d745ec7c1994f8cff341afee513c7cdad73e8883d540ff71c41763fd1");
        assertThat(govActionIdNode.get("gov_action_index").asInt())
                .isEqualTo(2);

        JsonNode protocolVersionNode = details.get("protocolVersion");
        assertThat(protocolVersionNode.get("_1").asInt())
                .isEqualTo(9);
        assertThat(protocolVersionNode.get("_2").asInt())
                .isEqualTo(3);
    }

    @Test
    void givenGovernanceEventWithParameterChangeAction_ShouldExcludeNullFieldsInDetails() {
        final GovernanceEvent governanceEvent = GovernanceEvent.builder()
                .metadata(eventMetadata())
                .txGovernanceList(List.of(
                        TxGovernance.builder()
                                .txHash("1234abcd5678efgh9012ijkl3456mnop7890qrstuvwx")
                                .proposalProcedures(List.of(
                                        ProposalProcedure.builder()
                                                .govAction(govAction(GovActionType.PARAMETER_CHANGE_ACTION))
                                                .deposit(BigInteger.valueOf(1000))
                                                .rewardAccount("E0D8E260880358F0A20F6325384975754B1AFD9FE45D164D68A79D4681")
                                                .anchor(Anchor.builder()
                                                        .anchor_url("https://example.com/proposal")
                                                        .anchor_data_hash("1111222233334444555566667777888899990000aaaabbbbccccdddd")
                                                        .build())
                                                .build()
                                ))
                                .build()
                ))
                .build();

        govActionProposalProcessor.handleGovernanceAction(governanceEvent);

        verify(govActionProposalStorage, times(1)).saveAll(govActionProposalsCaptor.capture());

        List<GovActionProposal> govActionProposalsSaved = govActionProposalsCaptor.getValue();
        assertThat(govActionProposalsSaved).hasSize(1);

        GovActionProposal savedProposal = govActionProposalsSaved.get(0);

        assertThat(savedProposal.getTxHash()).isEqualTo("1234abcd5678efgh9012ijkl3456mnop7890qrstuvwx");
        assertThat(savedProposal.getType()).isEqualTo(GovActionType.PARAMETER_CHANGE_ACTION);
        assertThat(savedProposal.getAnchorUrl()).isEqualTo("https://example.com/proposal");
        assertThat(savedProposal.getAnchorHash()).isEqualTo("1111222233334444555566667777888899990000aaaabbbbccccdddd");

        JsonNode details = savedProposal.getDetails();

        assertThat(details).isNotNull();
        assertThat(details.get("protocolParamUpdate").get("minFeeA").asInt()).isEqualTo(100);
        assertThat(details.get("protocolParamUpdate").get("minFeeB").asInt()).isEqualTo(200);
        assertThat(details.get("protocolParamUpdate").get("keyDeposit").asText()).isEqualTo("100000000");
        assertThat(details.get("protocolParamUpdate").has("drepVotingThresholds")).isFalse();
        assertThat(details.get("protocolParamUpdate").has("poolVotingThresholds")).isFalse();
        assertThat(details.get("protocolParamUpdate").has("drepDeposit")).isFalse();
    }

    private EventMetadata eventMetadata() {
        return EventMetadata.builder()
                .epochNumber(90)
                .block(100L)
                .blockTime(99999L)
                .slot(10000L)
                .build();
    }

    private List<ProposalProcedure> proposalProcedures() {
        return List.of(
                ProposalProcedure.builder()
                        .govAction(govAction(UPDATE_COMMITTEE))
                        .deposit(BigInteger.valueOf(1000))
                        .rewardAccount("E0D8E260880358F0A20F6325384975754B1AFD9FE45D164D68A79D4681")
                        .anchor(Anchor.builder()
                                .anchor_url("https://bit.ly/3zCH2HL")
                                .anchor_data_hash("1111111111111111111111111111111111111111111111111111111111111111")
                                .build()).build(),
                ProposalProcedure.builder()
                        .govAction(govAction(INFO_ACTION))
                        .deposit(BigInteger.valueOf(1000))
                        .rewardAccount("E079733F819A12BD34F15360A8711ED63DA24F65AEF3746DB51A7755CE")
                        .build()
        );
    }

    private GovAction govAction(GovActionType type) {
        switch (type) {
            case INFO_ACTION:
                return new InfoAction();
            case HARD_FORK_INITIATION_ACTION:
                return HardForkInitiationAction.builder()
                        .govActionId(GovActionId.builder()
                                .transactionId("bd5d786d745ec7c1994f8cff341afee513c7cdad73e8883d540ff71c41763fd1")
                                .gov_action_index(2)
                                .build())
                        .protocolVersion(
                                ProtocolVersion.builder()
                                        ._1(9)
                                        ._2(3).
                                        build()
                        )
                        .build();
            case TREASURY_WITHDRAWALS_ACTION:
                Map<String, BigInteger> withdrawals = new LinkedHashMap<>();
                withdrawals.put("e086dcecee2ca5017ed3a8bef8386f4ea19411872975818b6c8e40d101", BigInteger.valueOf(3000L));
                return TreasuryWithdrawalsAction.builder()
                        .withdrawals(withdrawals)
                        .build();

            case UPDATE_COMMITTEE:
                Set<Credential> membersForRemoval = new LinkedHashSet<>();
                membersForRemoval.add(Credential.builder().type(StakeCredType.SCRIPTHASH)
                        .hash("e99454a98c986da98191342e156514d1d029f03acbe3e9d60e8d32b2").build());
                Map<Credential, Integer> newMembersAndTerms = new LinkedHashMap<>();
                newMembersAndTerms.put(Credential.builder().type(StakeCredType.ADDR_KEYHASH)
                        .hash("e99454a98c986da98191342e156514d1d029f03acbe3e9d60e8d32b2").build(), 300);

                return UpdateCommittee.builder()
                        .govActionId(GovActionId.builder()
                                .transactionId("5159366367679d1050a48e94fa19f77b6b91c4d7a4f7b90a4b111f38c0746de8")
                                .gov_action_index(0)
                                .build())
                        .membersForRemoval(membersForRemoval)
                        .quorumThreshold(BigDecimal.valueOf(0.4))
                        .newMembersAndTerms(newMembersAndTerms)
                        .build();
            case NO_CONFIDENCE:
                return NoConfidence.builder()
                        .govActionId(GovActionId.builder()
                                .transactionId("5159366367679d1050a48e94fa19f77b6b91c4d7a4f7b90a4b111f38c0746de8")
                                .gov_action_index(0)
                                .build())
                        .build();
            case NEW_CONSTITUTION:
                return NewConstitution.builder()
                        .govActionId(null)
                        .constitution(Constitution.builder()
                                .anchor(Anchor.builder()
                                        .anchor_url("http://bit.ly/rtXr")
                                        .anchor_data_hash("9ee0ceb37b183bf3aa30a70378f230ac9f9d4574cda5f8a8937c21765c4c6296")
                                        .build())
                                .scripthash(null).build())
                        .build();
            case PARAMETER_CHANGE_ACTION:
                return ParameterChangeAction.builder()
                        .govActionId(null)
                        .protocolParamUpdate(protocolParamUpdate())
                        .build();
            default:
                throw new IllegalArgumentException();
        }
    }

    private ProtocolParamUpdate protocolParamUpdate() {
        return ProtocolParamUpdate.builder()
                .nOpt(5)
                .minFeeA(100)
                .minFeeB(200)
                .minUtxo(new BigInteger("500"))
                .maxEpoch(200)
                .priceMem(null)
                .maxTxSize(1000)
                .priceStep(new BigDecimal("0.01"))
                .costModels(Collections.singletonMap(1, "model1"))
                .keyDeposit(new BigInteger("100000000"))
                .maxTxExMem(new BigInteger("200000"))
                .maxValSize(500L)
                .drepDeposit(null)
                .minPoolCost(new BigInteger("1000000"))
                .poolDeposit(new BigInteger("1000000000"))
                .extraEntropy(new Tuple<>(42, "entropy"))
                .maxBlockSize(2000)
                .maxTxExSteps(new BigInteger("10000"))
                .expansionRate(new BigDecimal("0.02"))
                .maxBlockExMem(new BigInteger("300000"))
                .adaPerUtxoByte(new BigInteger("2"))
                .costModelsHash("hash123")
                .maxBlockExSteps(new BigInteger("15000"))
                .committeeMinSize(5)
                .protocolMajorVer(1)
                .protocolMinorVer(2)
                .collateralPercent(10)
                .committeeMaxTermLength(4)
                .drepVotingThresholds(null)
                .maxBlockHeaderSize(300)
                .treasuryGrowthRate(new BigDecimal("0.05"))
                .maxCollateralInputs(50)
                .poolPledgeInfluence(new BigDecimal("0.1"))
                .drepActivity(30)
                .poolVotingThresholds(null)
                .decentralisationParam(new BigDecimal("0.3"))
                .govActionDeposit(new BigInteger("1000000002"))
                .govActionLifetime(14)
                .build();
    }
}
