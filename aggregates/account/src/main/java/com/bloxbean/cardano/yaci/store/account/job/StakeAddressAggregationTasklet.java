package com.bloxbean.cardano.yaci.store.account.job;

import com.bloxbean.cardano.yaci.store.account.AccountStoreProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.CommonTableExpression;
import org.jooq.DSLContext;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;

import java.util.concurrent.atomic.AtomicLong;

import static com.bloxbean.cardano.yaci.store.account.job.AccountJobConstants.*;
import static com.bloxbean.cardano.yaci.store.account.jooq.Tables.*;
import static org.jooq.impl.DSL.*;

@RequiredArgsConstructor
@Slf4j
public class StakeAddressAggregationTasklet implements Tasklet {

    private final AccountStoreProperties accountStoreProperties;
    private final DSLContext dsl;
    private final static AtomicLong count = new AtomicLong(0);

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        ExecutionContext executionContext = chunkContext.getStepContext().getStepExecution().getExecutionContext();
        long startOffset = executionContext.getLong(START_OFFSET, 0L); // Default to 0 if not set
        long finalEndOffset = executionContext.getLong(END_OFFSET);

        var snapshotBlock = chunkContext.getStepContext().getStepExecution()
                .getJobParameters().getLong(SNAPSHOT_BLOCK);

        var snapshotSlot = chunkContext.getStepContext().getStepExecution()
                .getJobParameters().getLong(SNAPSHOT_SLOT);

        long limit = accountStoreProperties.getBalanceCalcJobBatchSize();

        long to = startOffset + limit;

        if (to > finalEndOffset) {
            to = finalEndOffset;
            limit = finalEndOffset - startOffset;
        }

        log.info("Processing stake addresses from {}, limit: {}, to: {}, FinalEndOffSet {}, stepId: {}", startOffset, limit, to, finalEndOffset, chunkContext.getStepContext().getId());

        calculateStakeAddressBalance(startOffset, limit, snapshotSlot);

        log.info("Total stake addresses processed: {}", count.addAndGet(limit));

        // Update ExecutionContext with the new startOffset for the next chunk
        executionContext.putLong(START_OFFSET, Long.valueOf(to));

        boolean shouldContinue = to < finalEndOffset;

        return shouldContinue ? RepeatStatus.CONTINUABLE : RepeatStatus.FINISHED;
    }

    private void calculateStakeAddressBalance(long startOffset, long limit, Long snapshotSlot) {
        CommonTableExpression<?> incremental = name("incremental").as(
                select(field(ADDRESS_TX_AMOUNT.STAKE_ADDRESS).as("address"),
                        coalesce(sum(field(ADDRESS_TX_AMOUNT.QUANTITY)), 0).as("quantity"),
                        max(field(ADDRESS_TX_AMOUNT.SLOT)).as("slot"),
                        max(field(ADDRESS_TX_AMOUNT.BLOCK)).as("block"),
                        max(field(ADDRESS_TX_AMOUNT.BLOCK_TIME)).as("block_time"),
                        max(field(ADDRESS_TX_AMOUNT.EPOCH)).as("epoch")
                )
                        .from(ADDRESS_TX_AMOUNT)
                        .where(ADDRESS_TX_AMOUNT.STAKE_ADDRESS.isNotNull()
                                .and(ADDRESS_TX_AMOUNT.SLOT.le(snapshotSlot))
                                .and(ADDRESS_TX_AMOUNT.STAKE_ADDRESS
                                        .in(selectDistinct(ADDRESS.STAKE_ADDRESS)
                                                .from(ADDRESS)
                                                .where(ADDRESS.STAKE_ADDRESS.isNotNull())
                                                .offset(startOffset)
                                                .limit(limit)
                                        )
                                )
                        )
                        .groupBy(ADDRESS_TX_AMOUNT.STAKE_ADDRESS)
        );

        dsl.with(incremental)
                .insertInto(STAKE_ADDRESS_BALANCE,
                        STAKE_ADDRESS_BALANCE.ADDRESS,
                        STAKE_ADDRESS_BALANCE.QUANTITY,
                        STAKE_ADDRESS_BALANCE.SLOT,
                        STAKE_ADDRESS_BALANCE.BLOCK,
                        STAKE_ADDRESS_BALANCE.BLOCK_TIME,
                        STAKE_ADDRESS_BALANCE.EPOCH,
                        STAKE_ADDRESS_BALANCE.UPDATE_DATETIME
                ).select(select(
                        incremental.field(STAKE_ADDRESS_BALANCE.ADDRESS),
                        incremental.field(STAKE_ADDRESS_BALANCE.QUANTITY),
                        incremental.field(STAKE_ADDRESS_BALANCE.SLOT),
                        incremental.field(STAKE_ADDRESS_BALANCE.BLOCK),
                        incremental.field(STAKE_ADDRESS_BALANCE.BLOCK_TIME),
                        incremental.field(STAKE_ADDRESS_BALANCE.EPOCH),
                        currentLocalDateTime())
                        .from(incremental))
                .onConflict(
                        STAKE_ADDRESS_BALANCE.ADDRESS,
                        STAKE_ADDRESS_BALANCE.SLOT
                )
                .doUpdate()
                .set(STAKE_ADDRESS_BALANCE.QUANTITY, excluded(STAKE_ADDRESS_BALANCE.QUANTITY))
                .set(STAKE_ADDRESS_BALANCE.SLOT, excluded(STAKE_ADDRESS_BALANCE.SLOT))
                .set(STAKE_ADDRESS_BALANCE.BLOCK, excluded(STAKE_ADDRESS_BALANCE.BLOCK))
                .set(STAKE_ADDRESS_BALANCE.BLOCK_TIME, excluded(STAKE_ADDRESS_BALANCE.BLOCK_TIME))
                .set(STAKE_ADDRESS_BALANCE.EPOCH, excluded(STAKE_ADDRESS_BALANCE.EPOCH))
                .execute();
    }

}


