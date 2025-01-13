package com.bloxbean.cardano.yaci.store.governance.service;

import com.bloxbean.cardano.client.address.Address;
import com.bloxbean.cardano.client.util.HexUtil;
import com.bloxbean.cardano.yaci.core.model.Credential;
import com.bloxbean.cardano.yaci.core.model.CredentialType;
import com.bloxbean.cardano.yaci.core.model.certs.StakeCredType;
import com.bloxbean.cardano.yaci.core.model.governance.Committee;
import com.bloxbean.cardano.yaci.core.model.governance.Constitution;
import com.bloxbean.cardano.yaci.core.model.governance.GovActionId;
import com.bloxbean.cardano.yaci.core.model.governance.GovActionType;
import com.bloxbean.cardano.yaci.core.protocol.chainsync.messages.Tip;
import com.bloxbean.cardano.yaci.core.protocol.localstate.api.Era;
import com.bloxbean.cardano.yaci.core.protocol.localstate.queries.GovStateQuery;
import com.bloxbean.cardano.yaci.core.protocol.localstate.queries.GovStateQueryResult;
import com.bloxbean.cardano.yaci.core.protocol.localstate.queries.model.Proposal;
import com.bloxbean.cardano.yaci.helper.LocalClientProvider;
import com.bloxbean.cardano.yaci.store.common.util.Tuple;
import com.bloxbean.cardano.yaci.store.core.annotation.LocalSupport;
import com.bloxbean.cardano.yaci.store.core.annotation.ReadOnly;
import com.bloxbean.cardano.yaci.store.core.service.EraService;
import com.bloxbean.cardano.yaci.store.core.service.local.LocalClientProviderManager;
import com.bloxbean.cardano.yaci.store.events.BlockHeaderEvent;
import com.bloxbean.cardano.yaci.store.events.EpochChangeEvent;
import com.bloxbean.cardano.yaci.store.governance.annotation.LocalGovState;
import com.bloxbean.cardano.yaci.store.governance.domain.GovActionProposal;
import com.bloxbean.cardano.yaci.store.governance.domain.local.*;
import com.bloxbean.cardano.yaci.store.governance.storage.GovActionProposalStorage;
import com.bloxbean.cardano.yaci.store.common.domain.GovActionStatus;
import com.bloxbean.cardano.yaci.store.governance.storage.local.*;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import reactor.core.publisher.Mono;

import java.math.BigInteger;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@LocalSupport
@LocalGovState
@ReadOnly(false)
@Slf4j
public class LocalGovStateService {
    private final LocalClientProviderManager localClientProviderManager;
    private final LocalGovActionProposalStatusStorage localGovActionProposalStatusStorage;
    private final LocalConstitutionStorage localConstitutionStorage;
    private final LocalCommitteeMemberStorage localCommitteeMemberStorage;
    private final LocalCommitteeStorage localCommitteeStorage;
    private final LocalHardForkInitiationStorage localHardForkInitiationStorage;
    private final GovActionProposalStorage govActionProposalStorage;
    private final LocalTreasuryWithdrawalStorage localTreasuryWithdrawalStorage;
    private final PlatformTransactionManager transactionManager;
    private final EraService eraService;
    private TransactionTemplate transactionTemplate;

    @Getter
    private Era era;

    public LocalGovStateService(@Nullable LocalClientProviderManager localClientProviderManager,
                                LocalGovActionProposalStatusStorage localGovActionProposalStatusStorage,
                                LocalConstitutionStorage localConstitutionStorage,
                                LocalCommitteeMemberStorage localCommitteeMemberStorage,
                                LocalCommitteeStorage localCommitteeStorage,
                                LocalHardForkInitiationStorage localHardForkInitiationStorage,
                                GovActionProposalStorage govActionProposalStorage,
                                LocalTreasuryWithdrawalStorage localTreasuryWithdrawalStorage, LocalConstitutionStorageReader localConstitutionStorageReader,
                                PlatformTransactionManager transactionManager,
                                EraService eraService) {
        this.localClientProviderManager = localClientProviderManager;
        this.localGovActionProposalStatusStorage = localGovActionProposalStatusStorage;
        this.localConstitutionStorage = localConstitutionStorage;
        this.localCommitteeMemberStorage = localCommitteeMemberStorage;
        this.localCommitteeStorage = localCommitteeStorage;
        this.localTreasuryWithdrawalStorage = localTreasuryWithdrawalStorage;
        this.localHardForkInitiationStorage = localHardForkInitiationStorage;
        this.govActionProposalStorage = govActionProposalStorage;
        this.transactionManager = transactionManager;
        this.eraService = eraService;

        log.info("LocalGovActionStateService initialized >>>");
    }

    @PostConstruct
    public void init() {
        transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @EventListener
    public void blockEvent(BlockHeaderEvent blockHeaderEvent) {
        if (blockHeaderEvent.getMetadata().getEra() != null && blockHeaderEvent.getMetadata().getEra().value >= com.bloxbean.cardano.yaci.core.model.Era.Conway.value
                && (era == null || !blockHeaderEvent.getMetadata().getEra().name().equalsIgnoreCase(era.name()))) {
            era = Era.valueOf(blockHeaderEvent.getMetadata().getEra().name());
            log.info("Current era: {}", era.name());
            log.info("Fetching gov state ...");
            fetchAndSetGovState();
        }
    }

    @EventListener
    public void handleEpochChangeEvent(EpochChangeEvent epochChangeEvent) {
        if (!epochChangeEvent.getEventMetadata().isSyncMode()) {
            return;
        }

        era = Era.valueOf(epochChangeEvent.getEra().name());

        log.info("Epoch change event received. Fetching and updating local gov state");
        fetchAndSetGovState();
    }

    public synchronized void fetchAndSetGovState() {
        if (localClientProviderManager == null)
            throw new IllegalStateException("LocalClientProvider is not initialized. Please check n2c configuration.");

        Optional<LocalClientProvider> localClientProvider = localClientProviderManager.getLocalClientProvider();

        try {
            var localStateQueryClient = localClientProvider.map(LocalClientProvider::getLocalStateQueryClient).orElse(null);
            if (localStateQueryClient == null) {
                log.info("LocalStateQueryClient is not initialized. Please check if n2c-node-socket-path or n2c-host is configured properly.");
                return;
            }

            //Try to release first before a new query to avoid stale data
            try {
                localStateQueryClient.release().block(Duration.ofSeconds(5));
            } catch (Exception e) {
                //Ignore the error
            }

            try {
                localStateQueryClient.acquire().block(Duration.ofSeconds(5));
            } catch (Exception e) {
                // Ignore the error
            }

            Mono<GovStateQueryResult> mono = localStateQueryClient.executeQuery(new GovStateQuery(era));
            mono.doOnError(throwable ->
                            log.error("Gov state sync error {}", throwable.getMessage()))
                    .doFinally(signalType ->
                            localClientProviderManager.close(localClientProvider.get()))
                    .subscribe(govStateQueryResult ->
                            transactionTemplate.executeWithoutResult(status ->
                                    handleGovStateQueryResult(govStateQueryResult))
                    );

        } catch (Exception e) {
            localClientProviderManager.close(localClientProvider.get());
        }
    }

    private void handleGovStateQueryResult(GovStateQueryResult govStateQueryResult) {
        Optional<Tuple<Tip, Integer>> epochAndTip = eraService.getTipAndCurrentEpoch();
        if (epochAndTip.isEmpty()) {
            log.error("Tip is null. Cannot fetch gov state");
            return;
        }

        var tip = epochAndTip.get()._1;
        var currentEpoch = epochAndTip.get()._2;
        var slot = tip.getPoint().getSlot();

        List<LocalGovActionProposalStatus> ratifiedGovActionsInPrevEpoch = localGovActionProposalStatusStorage
                .findByEpochAndStatusIn(currentEpoch - 1, List.of(GovActionStatus.RATIFIED));

        List<LocalGovActionProposalStatus> proposalStatusListToSave = new ArrayList<>();

        proposalStatusListToSave.addAll(getExpiredProposals(govStateQueryResult, currentEpoch, slot));
        proposalStatusListToSave.addAll(getRatifiedProposals(govStateQueryResult, currentEpoch, slot));
        proposalStatusListToSave.addAll(getActiveProposals(govStateQueryResult, currentEpoch, slot));

        if (!proposalStatusListToSave.isEmpty()) {
            localGovActionProposalStatusStorage.saveAll(proposalStatusListToSave);
        }

        handleConstitution(govStateQueryResult, currentEpoch, slot);
        handleCommittee(govStateQueryResult, currentEpoch, slot);
        handleCommitteeMembers(govStateQueryResult, currentEpoch, slot);

        List<GovActionId> enactedGovActionIds = getEnactedGovActionIds(ratifiedGovActionsInPrevEpoch,govStateQueryResult);

        handleTreasuryWithdrawals(enactedGovActionIds, currentEpoch, slot);
        handleHardForkInitiation(enactedGovActionIds, currentEpoch, slot);
    }

    private List<LocalGovActionProposalStatus> getExpiredProposals(GovStateQueryResult govStateQueryResult, Integer currentEpoch, Long slot) {
        List<LocalGovActionProposalStatus> proposalStatusList = new ArrayList<>();

        List<GovActionId> expiredGovActions = govStateQueryResult.getNextRatifyState().getExpiredGovActions();
        expiredGovActions.forEach(govActionId -> proposalStatusList.add(buildLocalGovActionProposal(govActionId, GovActionStatus.EXPIRED, currentEpoch, slot)));

        return proposalStatusList;
    }

    private List<LocalGovActionProposalStatus> getRatifiedProposals(GovStateQueryResult govStateQueryResult, Integer currentEpoch, Long slot) {
        List<LocalGovActionProposalStatus> proposalStatusList = new ArrayList<>();

        List<GovActionId> ratifiedGovActions = govStateQueryResult.getNextRatifyState().getEnactedGovActions()
                .stream()
                .map(Proposal::getGovActionId)
                .toList();
        ratifiedGovActions.forEach(govActionId -> proposalStatusList.add(buildLocalGovActionProposal(govActionId, GovActionStatus.RATIFIED, currentEpoch, slot)));

        return proposalStatusList;
    }

    private List<GovActionId> getEnactedGovActionIds(
            List<LocalGovActionProposalStatus> ratifiedGovActionsInPrevEpoch, GovStateQueryResult govStateQueryResult) {
        List<GovActionId> results = new ArrayList<>();
        List<GovActionId> proposalsInNextRatify = govStateQueryResult.getProposals().stream().map(Proposal::getGovActionId).toList();

        ratifiedGovActionsInPrevEpoch.forEach(govAction -> {
            GovActionId govActionId = GovActionId.builder()
                    .transactionId(govAction.getGovActionTxHash())
                    .gov_action_index((int) govAction.getGovActionIndex())
                    .build();

            if (govAction.getStatus().equals(GovActionStatus.RATIFIED) && !proposalsInNextRatify.contains(govActionId)) {
                results.add(govActionId);
            }
        });

        return results;
    }

    private List<LocalGovActionProposalStatus> getActiveProposals(GovStateQueryResult govStateQueryResult, Integer currentEpoch, Long slot) {
        List<LocalGovActionProposalStatus> activeProposalStatusList = new ArrayList<>();

        List<GovActionId> expiredGovActions = govStateQueryResult.getNextRatifyState().getExpiredGovActions();
        List<GovActionId> ratifiedGovActions = govStateQueryResult.getNextRatifyState().getEnactedGovActions()
                .stream()
                .map(Proposal::getGovActionId)
                .toList();
        List<GovActionId> proposalsInNextRatify = govStateQueryResult.getProposals().stream().map(Proposal::getGovActionId).toList();

        proposalsInNextRatify.forEach(govActionId -> {
            if (!expiredGovActions.contains(govActionId) && !ratifiedGovActions.contains(govActionId)) {
                activeProposalStatusList.add(buildLocalGovActionProposal(govActionId, GovActionStatus.ACTIVE, currentEpoch, slot));
            }
        });

        return activeProposalStatusList;
    }

    private void handleConstitution(GovStateQueryResult govStateQueryResult, Integer currentEpoch, Long slot) {
        Constitution constitution = govStateQueryResult.getConstitution();
        LocalConstitution localConstitution = buildLocalConstitution(constitution, currentEpoch, slot);

        localConstitutionStorage.save(localConstitution);
    }

    private void handleCommittee(GovStateQueryResult govStateQueryResult, Integer currentEpoch, Long slot) {
        Committee committee = govStateQueryResult.getCommittee();
        LocalCommittee localCommittee = buildLocalCommittee(committee, currentEpoch, slot);

        localCommitteeStorage.save(localCommittee);
    }

    private void handleCommitteeMembers(GovStateQueryResult govStateQueryResult, Integer currentEpoch, Long slot) {
        Committee committee = govStateQueryResult.getCommittee();
        Map<Credential, Long> committeeColdCredentialEpoch = committee.getCommitteeColdCredentialEpoch();
        List<LocalCommitteeMember> localCommitteeMemberEntities = new ArrayList<>();
        committeeColdCredentialEpoch.forEach((credential, expiredEpoch) -> {
            LocalCommitteeMember entity = buildLocalCommitteeMember(credential, expiredEpoch.intValue(), currentEpoch, slot);
            localCommitteeMemberEntities.add(entity);
        });

        if (!localCommitteeMemberEntities.isEmpty()) {
            localCommitteeMemberStorage.saveAll(localCommitteeMemberEntities);
        }
    }

    private void handleTreasuryWithdrawals(List<GovActionId> enactedGovActionIds, Integer currentEpoch, Long slot) {
        List<GovActionProposal> enactedTreasuryWithdrawalProposals = govActionProposalStorage.findByGovActionIds(enactedGovActionIds)
                .stream()
                .filter(govActionProposal -> govActionProposal.getType() == GovActionType.TREASURY_WITHDRAWALS_ACTION)
                .toList();

        localTreasuryWithdrawalStorage.saveAll(
                enactedTreasuryWithdrawalProposals.stream()
                        .flatMap(govActionProposalEntity -> {
                            JsonNode actionDetail = govActionProposalEntity.getDetails();
                            JsonNode withdrawalsNode = actionDetail.get("withdrawals");
                            List<LocalTreasuryWithdrawal> treasuryWithdrawalEntities = new ArrayList<>();
                            withdrawalsNode.fields().forEachRemaining(entry -> {
                                String addressHash = entry.getKey();
                                BigInteger amount = entry.getValue().bigIntegerValue();
                                Address address = new Address(HexUtil.decodeHexString(addressHash));
                                var entity = LocalTreasuryWithdrawal.builder()
                                        .govActionTxHash(govActionProposalEntity.getTxHash())
                                        .govActionIndex((int) govActionProposalEntity.getIndex())
                                        .epoch(currentEpoch)
                                        .amount(amount.longValue())
                                        .address(address.getAddress())
                                        .slot(slot)
                                        .build();
                                treasuryWithdrawalEntities.add(entity);
                            });

                            return treasuryWithdrawalEntities.stream();
                        }).toList());
    }

    private void handleHardForkInitiation(List<GovActionId> enactedGovActionIds, Integer currentEpoch, Long slot) {
        List<GovActionProposal> enactedHardForkInitProposals = govActionProposalStorage.findByGovActionIds(enactedGovActionIds)
                .stream()
                .filter(govActionProposalEntity -> govActionProposalEntity.getType() == GovActionType.HARD_FORK_INITIATION_ACTION)
                .toList();

        localHardForkInitiationStorage.saveAll(
                enactedHardForkInitProposals.stream()
                        .map(govActionProposal -> {
                            JsonNode actionDetail = govActionProposal.getDetails();
                            JsonNode protocolVersionNode = actionDetail.get("protocolVersion");
                            Integer major = protocolVersionNode.get("_1") != null ? protocolVersionNode.get("_1").asInt() : null;
                            Integer minor = protocolVersionNode.get("_2") != null ? protocolVersionNode.get("_2").asInt() : null;

                            return LocalHardForkInitiation.builder()
                                    .govActionTxHash(govActionProposal.getTxHash())
                                    .govActionIndex((int) govActionProposal.getIndex())
                                    .epoch(currentEpoch)
                                    .majorVersion(major)
                                    .minorVersion(minor)
                                    .slot(slot)
                                    .build();
                        }).toList());
    }

    private LocalGovActionProposalStatus buildLocalGovActionProposal(GovActionId govActionId, GovActionStatus status, Integer epoch, Long slot) {
        return LocalGovActionProposalStatus.builder()
                .govActionTxHash(govActionId.getTransactionId())
                .govActionIndex(govActionId.getGov_action_index())
                .epoch(epoch)
                .slot(slot)
                .status(status)
                .build();
    }

    private LocalConstitution buildLocalConstitution(Constitution constitution, Integer epoch, Long slot) {
        return LocalConstitution.builder()
                .anchorHash(constitution.getAnchor().getAnchor_data_hash())
                .anchorUrl(constitution.getAnchor().getAnchor_url())
                .epoch(epoch)
                .slot(slot)
                .script(constitution.getScripthash())
                .build();
    }

    private LocalCommitteeMember buildLocalCommitteeMember(Credential credential, Integer expiredEpoch, Integer currentEpoch, Long slot) {
        return LocalCommitteeMember.builder()
                .hash(credential.getHash())
                .credType(credential.getType() == StakeCredType.ADDR_KEYHASH ? CredentialType.ADDR_KEYHASH : CredentialType.SCRIPTHASH)
                .expiredEpoch(expiredEpoch)
                .epoch(currentEpoch)
                .slot(slot)
                .build();
    }

    private LocalCommittee buildLocalCommittee(Committee committee, Integer currentEpoch, Long slot) {
        return LocalCommittee.builder()
                .threshold(committee.getThreshold().doubleValue())
                .epoch(currentEpoch)
                .slot(slot)
                .build();
    }
}