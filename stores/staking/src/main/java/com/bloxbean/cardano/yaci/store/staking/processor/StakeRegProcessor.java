package com.bloxbean.cardano.yaci.store.staking.processor;

import com.bloxbean.cardano.client.address.Address;
import com.bloxbean.cardano.yaci.core.model.certs.*;
import com.bloxbean.cardano.yaci.store.events.CertificateEvent;
import com.bloxbean.cardano.yaci.store.events.EventMetadata;
import com.bloxbean.cardano.yaci.store.events.RollbackEvent;
import com.bloxbean.cardano.yaci.store.events.domain.TxCertificates;
import com.bloxbean.cardano.yaci.store.staking.domain.Delegation;
import com.bloxbean.cardano.yaci.store.staking.domain.StakeRegistrationDetail;
import com.bloxbean.cardano.yaci.store.staking.domain.event.StakeRegDeregEvent;
import com.bloxbean.cardano.yaci.store.staking.storage.StakingCertificateStorage;
import com.bloxbean.cardano.yaci.store.staking.util.AddressUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class StakeRegProcessor {
    private final StakingCertificateStorage stakingStorage;
    private final ApplicationEventPublisher publisher;

    @EventListener
    @Transactional
    public void processStakeRegistration(CertificateEvent certificateEvent) {
        EventMetadata eventMetadata = certificateEvent.getMetadata();

        List<StakeRegistrationDetail> stakeRegDeRegs = new ArrayList<>();
        List<Delegation> delegations = new ArrayList<>();

        for (TxCertificates txCertificates : certificateEvent.getTxCertificatesList()) {
            String txHash = txCertificates.getTxHash();
            int txIndex = txCertificates.getTxIndex();
            List<Certificate> certificates = txCertificates.getCertificates();

            int index = 0;
            for (Certificate certificate : certificates) {
                var certType = certificate.getType();
                StakeRegistration stakeRegistration;
                StakeDeregistration stakeDeregistration;
                StakeDelegation stakeDelegation;
                StakeRegistrationDetail stakeRegistrationDetail;
                Delegation delegation;

                switch (certType) {
                    case STAKE_REGISTRATION, REG_CERT, VOTE_REG_DELEG_CERT:
                        if (certType == CertificateType.STAKE_REGISTRATION) {
                            stakeRegistration = (StakeRegistration) certificate;
                        } else if (certType == CertificateType.REG_CERT) {
                            stakeRegistration = StakeRegistration.builder()
                                    .stakeCredential(((RegCert) certificate).getStakeCredential()).build();
                        } else {
                            stakeRegistration = StakeRegistration.builder()
                                    .stakeCredential(((VoteRegDelegCert) certificate).getStakeCredential())
                                    .build();
                        }
                        stakeRegistrationDetail = buildStakeRegistrationDetail(
                                stakeRegistration, txHash, index, txIndex, eventMetadata);

                        stakeRegDeRegs.add(stakeRegistrationDetail);
                        break;

                    case STAKE_DEREGISTRATION, UNREG_CERT:
                        if (certType == CertificateType.STAKE_DEREGISTRATION) {
                            stakeDeregistration = (StakeDeregistration) certificate;
                        } else {
                            stakeDeregistration = StakeDeregistration.builder()
                                    .stakeCredential(((UnregCert) certificate).getStakeCredential())
                                    .build();
                        }
                        stakeRegistrationDetail = buildStakeRegistrationDetail(
                                stakeDeregistration, txHash, index, txIndex, eventMetadata);

                        stakeRegDeRegs.add(stakeRegistrationDetail);
                        break;

                    case STAKE_DELEGATION, STAKE_VOTE_DELEG_CERT:
                        if (certType == CertificateType.STAKE_VOTE_DELEG_CERT) {
                            var stakeVoteDelegCert = (StakeVoteDelegCert) certificate;
                            stakeDelegation = StakeDelegation.builder()
                                    .stakeCredential(stakeVoteDelegCert.getStakeCredential())
                                    .stakePoolId(StakePoolId.fromHexPoolId(stakeVoteDelegCert.getPoolKeyHash()))
                                    .build();
                        } else {
                            stakeDelegation = (StakeDelegation) certificate;
                        }

                        delegation = buildDelegation(stakeDelegation, txHash, index, txIndex, eventMetadata);
                        delegations.add(delegation);

                        break;

                    case STAKE_REG_DELEG_CERT, STAKE_VOTE_REG_DELEG_CERT:
                        if (certType == CertificateType.STAKE_REG_DELEG_CERT) {
                            var stakeRegDelegCert = (StakeRegDelegCert) certificate;
                            stakeDelegation = StakeDelegation.builder()
                                    .stakeCredential(stakeRegDelegCert.getStakeCredential())
                                    .stakePoolId(StakePoolId.builder().poolKeyHash(stakeRegDelegCert.getPoolKeyHash()).build())
                                    .build();
                            stakeRegistration = StakeRegistration.builder()
                                    .stakeCredential(stakeRegDelegCert.getStakeCredential())
                                    .build();
                        } else {
                            var stakeVoteRegDelegCert = (StakeVoteRegDelegCert) certificate;
                            stakeDelegation = StakeDelegation.builder()
                                    .stakeCredential(stakeVoteRegDelegCert.getStakeCredential())
                                    .stakePoolId(StakePoolId.builder().poolKeyHash(stakeVoteRegDelegCert.getPoolKeyHash()).build())
                                    .build();
                            stakeRegistration = StakeRegistration.builder()
                                    .stakeCredential(stakeVoteRegDelegCert.getStakeCredential())
                                    .build();
                        }

                        delegation = buildDelegation(stakeDelegation, txHash, index, txIndex, eventMetadata);
                        stakeRegistrationDetail = buildStakeRegistrationDetail(
                                stakeRegistration, txHash, index, txIndex, eventMetadata);

                        stakeRegDeRegs.add(stakeRegistrationDetail);
                        delegations.add(delegation);
                        break;
                    default:
                        break;
                }

                index++;
            }
        }

        if (!stakeRegDeRegs.isEmpty()) {
            stakingStorage.saveRegistrations(stakeRegDeRegs);

        }
        if (!delegations.isEmpty())
            stakingStorage.saveDelegations(delegations);

        //publish events
        if (!stakeRegDeRegs.isEmpty()) {
            publisher.publishEvent(new StakeRegDeregEvent(eventMetadata, stakeRegDeRegs));
        }
    }

    private StakeRegistrationDetail buildStakeRegistrationDetail(StakeRegistration stakeRegistration,
                                                                 String txHash,
                                                                 int certIndex,
                                                                 int txIndex,
                                                                 EventMetadata eventMetadata) {
        Address address =
                AddressUtil.getRewardAddress(stakeRegistration.getStakeCredential(), eventMetadata.isMainnet());

        return StakeRegistrationDetail.builder()
                .credential(stakeRegistration.getStakeCredential().getHash())
                .credentialType(getCredType(stakeRegistration.getStakeCredential())) //TODO -- add to db
                .address(address.toBech32())
                .slot(eventMetadata.getSlot())
                .txHash(txHash)
                .certIndex(certIndex)
                .txIndex(txIndex)
                .type(CertificateType.STAKE_REGISTRATION)
                .epoch(eventMetadata.getEpochNumber())
                .slot(eventMetadata.getSlot())
                .blockNumber(eventMetadata.getBlock())
                .blockHash(eventMetadata.getBlockHash())
                .blockTime(eventMetadata.getBlockTime())
                .build();
    }

    private StakeRegistrationDetail buildStakeRegistrationDetail(StakeDeregistration stakeDeregistration,
                                                                 String txHash,
                                                                 int certIndex,
                                                                 int txIndex,
                                                                 EventMetadata eventMetadata) {
        Address address =
                AddressUtil.getRewardAddress(stakeDeregistration.getStakeCredential(), eventMetadata.isMainnet());

        return StakeRegistrationDetail.builder()
                .credential(stakeDeregistration.getStakeCredential().getHash())
                .credentialType(getCredType(stakeDeregistration.getStakeCredential()))
                .address(address.toBech32())
                .slot(eventMetadata.getSlot())
                .txHash(txHash)
                .certIndex(certIndex)
                .txIndex(txIndex)
                .type(CertificateType.STAKE_DEREGISTRATION)
                .epoch(eventMetadata.getEpochNumber())
                .slot(eventMetadata.getSlot())
                .blockNumber(eventMetadata.getBlock())
                .blockHash(eventMetadata.getBlockHash())
                .blockTime(eventMetadata.getBlockTime())
                .build();
    }

    private Delegation buildDelegation(StakeDelegation stakeDelegation,
                                       String txHash,
                                       int certIndex,
                                       int txIndex,
                                       EventMetadata eventMetadata) {
        Address address =
                AddressUtil.getRewardAddress(stakeDelegation.getStakeCredential(), eventMetadata.isMainnet());

        return Delegation.builder()
                .credential(stakeDelegation.getStakeCredential().getHash())
                .credentialType(getCredType(stakeDelegation.getStakeCredential()))
                .address(address.toBech32())
                .slot(eventMetadata.getSlot())
                .txHash(txHash)
                .certIndex(certIndex)
                .txIndex(txIndex)
                .poolId(stakeDelegation.getStakePoolId().getPoolKeyHash())
                .epoch(eventMetadata.getEpochNumber())
                .slot(eventMetadata.getSlot())
                .blockNumber(eventMetadata.getBlock())
                .blockHash(eventMetadata.getBlockHash())
                .blockTime(eventMetadata.getBlockTime())
                .build();
    }

    private com.bloxbean.cardano.yaci.core.model.CredentialType getCredType(StakeCredential stakeCredential) {
        if (stakeCredential.getType() == StakeCredType.ADDR_KEYHASH)
            return com.bloxbean.cardano.yaci.core.model.CredentialType.ADDR_KEYHASH;
        else if (stakeCredential.getType() == StakeCredType.SCRIPTHASH)
            return com.bloxbean.cardano.yaci.core.model.CredentialType.SCRIPTHASH;
        else
            return null;
    }

    @EventListener
    @Transactional
    //TODO -- tests
    public void handleRollbackEvent(RollbackEvent rollbackEvent) {
        int count = stakingStorage.deleteRegistrationsBySlotGreaterThan(rollbackEvent.getRollbackTo().getSlot());
        log.info("Rollback -- {} staking_registrations records", count);

        count = stakingStorage.deleteDelegationsBySlotGreaterThan(rollbackEvent.getRollbackTo().getSlot());
        log.info("Rollback -- {} staking_delegations records", count);
    }
}
