package com.bloxbean.cardano.yaci.store.governanceaggr.processor;

import com.bloxbean.cardano.yaci.core.model.governance.GovActionId;
import com.bloxbean.cardano.yaci.core.model.governance.GovActionType;
import com.bloxbean.cardano.yaci.store.client.governance.ProposalStateClient;
import com.bloxbean.cardano.yaci.store.common.domain.GovActionProposal;
import com.bloxbean.cardano.yaci.store.common.domain.GovActionStatus;
import com.bloxbean.cardano.yaci.store.events.domain.ProposalStatusCapturedEvent;
import com.bloxbean.cardano.yaci.store.events.domain.RewardRestAmt;
import com.bloxbean.cardano.yaci.store.events.domain.RewardRestEvent;
import com.bloxbean.cardano.yaci.store.governance.storage.GovActionProposalStorage;
import com.bloxbean.cardano.yaci.store.governanceaggr.domain.Proposal;
import com.bloxbean.cardano.yaci.store.governanceaggr.storage.impl.mapper.ProposalMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigInteger;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProposalRefundProcessorTest {
    @Mock
    private GovActionProposalStorage govActionProposalStorage;

    @Mock
    private ProposalStateClient proposalStateClient;

    @Mock
    private ProposalMapper proposalMapper;

    @Mock
    private ApplicationEventPublisher publisher;

    @InjectMocks
    private ProposalRefundProcessor proposalRefundProcessor;

    @Test
    void testHandleProposalStatusCapturedEvent() throws JsonProcessingException {

        ProposalStatusCapturedEvent event = new ProposalStatusCapturedEvent(1, 100);

        Proposal proposal1 = new Proposal(GovActionId.builder()
                .gov_action_index(0)
                .transactionId("aaaaaaaaaade062ade1e70d0040abde47a6626c7d8e98816a9d87e6bd6228b45")
                .build(), null, GovActionType.PARAMETER_CHANGE_ACTION);

        Proposal proposal2 = new Proposal(GovActionId.builder()
                .gov_action_index(0)
                .transactionId("bbbbbbbbbbde062ade1e70d0040abde47a6626c7d8e98816a9d87e6bd6228b45")
                .build(), null, GovActionType.PARAMETER_CHANGE_ACTION);

        Proposal proposal3 = new Proposal(GovActionId.builder()
                .gov_action_index(0)
                .transactionId("ccccccccccde062ade1e70d0040abde47a6626c7d8e98816a9d87e6bd6228b45")
                .build(), null, GovActionType.UPDATE_COMMITTEE);

        Proposal proposal4 = new Proposal(GovActionId.builder()
                .gov_action_index(0)
                .transactionId("ddddddddddde062ade1e70d0040abde47a6626c7d8e98816a9d87e6bd6228b45")
                .build(),
                GovActionId.builder()
                .gov_action_index(0)
                .transactionId("ccccccccccde062ade1e70d0040abde47a6626c7d8e98816a9d87e6bd6228b45")
                .build(), GovActionType.UPDATE_COMMITTEE);

        Proposal proposal5 = new Proposal(GovActionId.builder()
                .gov_action_index(0)
                .transactionId("eeeeeeeeeee062ade1e70d0040abde47a6626c7d8e98816a9d87e6bd6228b45")
                .build(), null, GovActionType.NEW_CONSTITUTION);

        GovActionProposal govProposal1 = new GovActionProposal();
        govProposal1.setReturnAddress("addr1");
        govProposal1.setDeposit(BigInteger.valueOf(1000));

        GovActionProposal govProposal2 = new GovActionProposal();
        govProposal2.setReturnAddress("addr2");
        govProposal2.setDeposit(BigInteger.valueOf(2000));

        GovActionProposal govProposal3 = new GovActionProposal();
        govProposal3.setReturnAddress("addr3");
        govProposal3.setDeposit(BigInteger.valueOf(3000));

        GovActionProposal govProposal4 = new GovActionProposal();
        govProposal4.setReturnAddress("addr4");
        govProposal4.setDeposit(BigInteger.valueOf(4000));

        GovActionProposal govProposal5 = new GovActionProposal();
        govProposal5.setReturnAddress("addr5");
        govProposal5.setDeposit(BigInteger.valueOf(5000));

        com.bloxbean.cardano.yaci.store.governance.domain.GovActionProposal domainGovProposal1 =
                new com.bloxbean.cardano.yaci.store.governance.domain.GovActionProposal();
        domainGovProposal1.setReturnAddress("addr1");
        domainGovProposal1.setDeposit(BigInteger.valueOf(1000));

        com.bloxbean.cardano.yaci.store.governance.domain.GovActionProposal domainGovProposal2 =
                new com.bloxbean.cardano.yaci.store.governance.domain.GovActionProposal();
        domainGovProposal2.setReturnAddress("addr2");
        domainGovProposal2.setDeposit(BigInteger.valueOf(2000));

        com.bloxbean.cardano.yaci.store.governance.domain.GovActionProposal domainGovProposal3 =
                new com.bloxbean.cardano.yaci.store.governance.domain.GovActionProposal();
        domainGovProposal3.setReturnAddress("addr3");
        domainGovProposal3.setDeposit(BigInteger.valueOf(3000));

        com.bloxbean.cardano.yaci.store.governance.domain.GovActionProposal domainGovProposal4 =
                new com.bloxbean.cardano.yaci.store.governance.domain.GovActionProposal();
        domainGovProposal4.setReturnAddress("addr4");
        domainGovProposal4.setDeposit(BigInteger.valueOf(4000));

        com.bloxbean.cardano.yaci.store.governance.domain.GovActionProposal domainGovProposal5 =
                new com.bloxbean.cardano.yaci.store.governance.domain.GovActionProposal();
        domainGovProposal5.setReturnAddress("addr5");
        domainGovProposal5.setDeposit(BigInteger.valueOf(5000));

        when(proposalStateClient.getProposalsByStatusAndEpoch(GovActionStatus.EXPIRED, 1))
                .thenReturn(List.of(govProposal1));
        when(proposalStateClient.getProposalsByStatusAndEpoch(GovActionStatus.RATIFIED, 1))
                .thenReturn(List.of(govProposal3));
        when(proposalStateClient.getProposalsByStatusListAndEpoch(anyList(), eq(1)))
                .thenReturn(List.of(govProposal1, govProposal2, govProposal3, govProposal4, govProposal5));

        when(proposalMapper.toProposal(govProposal1)).thenReturn(proposal1);
        when(proposalMapper.toProposal(govProposal2)).thenReturn(proposal2);
        when(proposalMapper.toProposal(govProposal3)).thenReturn(proposal3);
        when(proposalMapper.toProposal(govProposal4)).thenReturn(proposal4);
        when(proposalMapper.toProposal(govProposal5)).thenReturn(proposal5);

        when(govActionProposalStorage.findByGovActionIds(anyList())).thenReturn(
                List.of(domainGovProposal1, domainGovProposal3, domainGovProposal4));

        proposalRefundProcessor.handleProposalStatusCapturedEvent(event);

        ArgumentCaptor<List<GovActionId>> govActionIdsCaptor = ArgumentCaptor.forClass(List.class);
        verify(govActionProposalStorage).findByGovActionIds(govActionIdsCaptor.capture());
        List<GovActionId> capturedGovActionIds = govActionIdsCaptor.getValue();

        assertEquals(3, capturedGovActionIds.size());
        assertThat(capturedGovActionIds).containsExactlyInAnyOrder(proposal1.getGovActionId(),
                proposal3.getGovActionId(), proposal4.getGovActionId());

        ArgumentCaptor<RewardRestEvent> eventCaptor = ArgumentCaptor.forClass(RewardRestEvent.class);
        verify(publisher, times(1)).publishEvent(eventCaptor.capture());

        RewardRestEvent capturedEvent = eventCaptor.getValue();
        List<RewardRestAmt> rewards = capturedEvent.getRewards();

        assertEquals(3, rewards.size());
        assertThat(rewards).extracting(RewardRestAmt::getAddress).containsExactlyInAnyOrder("addr1", "addr3", "addr4");
        assertThat(rewards).extracting(RewardRestAmt::getAmount).containsExactlyInAnyOrder(BigInteger.valueOf(1000), BigInteger.valueOf(3000), BigInteger.valueOf(4000));
    }

}