package com.bloxbean.cardano.yaci.store.governance;

import com.bloxbean.cardano.yaci.store.governance.storage.*;
import com.bloxbean.cardano.yaci.store.governance.storage.impl.*;
import com.bloxbean.cardano.yaci.store.governance.storage.impl.mapper.*;
import com.bloxbean.cardano.yaci.store.governance.storage.impl.repository.*;
import org.jooq.DSLContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@ConditionalOnProperty(
        prefix = "store.governance",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
@ComponentScan(basePackages = {"com.bloxbean.cardano.yaci.store.governance"})
@EnableJpaRepositories(basePackages = {"com.bloxbean.cardano.yaci.store.governance"})
@EntityScan(basePackages = {"com.bloxbean.cardano.yaci.store.governance"})
@EnableTransactionManagement
public class GovernanceStoreConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public GovActionProposalStorage governanceActionStorage(GovActionProposalRepository govActionProposalRepository,
                                                            GovActionProposalMapper govActionProposalMapper) {
        return new GovActionProposalStorageImpl(govActionProposalRepository, govActionProposalMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public GovActionProposalStorageReader govActionProposalStorageReader(GovActionProposalRepository govActionProposalRepository,
                                                                         GovActionProposalMapper govActionProposalMapper) {
        return new GovActionProposalStorageReaderImpl(govActionProposalRepository, govActionProposalMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public VotingProcedureStorage votingProposalStorage(VotingProcedureRepository votingProcedureRepository,
                                                        VotingProcedureMapper votingProcedureMapper) {
        return new VotingProcedureStorageImpl(votingProcedureRepository, votingProcedureMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public VotingProcedureStorageReader votingProcedureStorageReader(VotingProcedureRepository votingProcedureRepository,
                                                                     VotingProcedureMapper votingProcedureMapper) {
        return new VotingProcedureStorageReaderImpl(votingProcedureRepository, votingProcedureMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public CommitteeDeRegistrationStorage committeeDeRegistrationStorage(CommitteeDeRegistrationRepository committeeDeRegistrationRepository,
                                                                         CommitteeDeRegistrationMapper committeeDeRegistrationMapper) {
        return new CommitteeDeRegistrationStorageImpl(committeeDeRegistrationRepository, committeeDeRegistrationMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public CommitteeDeRegistrationStorageReader committeeDeRegistrationStorageReader(CommitteeDeRegistrationRepository committeeDeRegistrationRepository,
                                                                                     CommitteeDeRegistrationMapper committeeDeRegistrationMapper) {
        return new CommitteeDeRegistrationStorageReaderImpl(committeeDeRegistrationRepository, committeeDeRegistrationMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public CommitteeRegistrationStorage committeeRegistrationStorage(CommitteeRegistrationRepository committeeRegistrationRepository,
                                                                     CommitteeRegistrationMapper committeeRegistrationMapper) {
        return new CommitteeRegistrationStorageImpl(committeeRegistrationRepository, committeeRegistrationMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public CommitteeRegistrationStorageReader committeeRegistrationStorageReader(CommitteeRegistrationRepository committeeRegistrationRepository,
                                                                                 CommitteeRegistrationMapper committeeRegistrationMapper) {
        return new CommitteeRegistrationStorageReaderImpl(committeeRegistrationRepository, committeeRegistrationMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public DelegationVoteStorage delegationVoteStorage(DelegationVoteRepository delegationVoteRepository,
                                                       DelegationVoteMapper delegationVoteMapper) {
        return new DelegationVoteStorageImpl(delegationVoteRepository, delegationVoteMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public DelegationVoteStorageReader delegationVoteStorageReader(DelegationVoteRepository delegationVoteRepository,
                                                                   DelegationVoteMapper delegationVoteMapper) {
        return new DelegationVoteStorageReaderImpl(delegationVoteRepository, delegationVoteMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public DRepRegistrationStorage drepRegistrationStorage(DRepRegistrationRepository drepRegistrationRepository,
                                                           DRepRegistrationMapper drepRegistrationMapper) {
        return new DRepRegistrationStorageImpl(drepRegistrationRepository, drepRegistrationMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public DRepRegistrationStorageReader dRepRegistrationStorageReader(DRepRegistrationRepository drepRegistrationRepository,
                                                                       DRepRegistrationMapper drepRegistrationMapper) {
        return new DRepRegistrationStorageReaderImpl(drepRegistrationRepository, drepRegistrationMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public CommitteeMemberStorage committeeMemberStorage(CommitteeMemberRepository committeeMemberRepository,
                                                         CommitteeMemberMapper committeeMemberMapper,
                                                         DSLContext dslContext) {
        return new CommitteeMemberStorageImpl(committeeMemberRepository, committeeMemberMapper, dslContext);
    }

    @Bean
    @ConditionalOnMissingBean
    public CommitteeMemberStorageReader committeeMemberStorageReader(CommitteeMemberRepository committeeMemberRepository,
                                                                     CommitteeMemberMapper committeeMemberMapper) {
        return new CommitteeMemberStorageReaderImpl(committeeMemberRepository, committeeMemberMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public ConstitutionStorage constitutionStorage(ConstitutionRepository constitutionRepository,
                                                   ConstitutionMapper constitutionMapper) {
        return new ConstitutionStorageImpl(constitutionRepository, constitutionMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public ConstitutionStorageReader constitutionStorageReader(ConstitutionRepository constitutionRepository,
                                                               ConstitutionMapper constitutionMapper) {
        return new ConstitutionStorageReaderImpl(constitutionRepository, constitutionMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public CommitteeStorage committeeStorage(CommitteeRepository committeeRepository,
                                             CommitteeMapper committeeMapper) {
        return new CommitteeStorageImpl(committeeRepository, committeeMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public CommitteeStorageReader committeeStorageReader(CommitteeRepository committeeRepository,
                                                         CommitteeMapper committeeMapper) {
        return new CommitteeStorageReaderImpl(committeeRepository, committeeMapper);
    }

}