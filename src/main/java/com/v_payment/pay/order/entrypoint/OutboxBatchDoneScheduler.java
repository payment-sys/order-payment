package com.v_payment.pay.order.entrypoint;

import com.v_payment.pay.order.application.QuantityChangeEventUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxBatchDoneScheduler {
    private static final int BATCH_SIZE = 100;

    private final QuantityChangeEventUseCase quantityChangeEventUseCase;
    private final BlockingQueue<Long> doneOutboxIds =
            new LinkedBlockingQueue<>(50_000);

    public void enqueue(Long outboxId) {
        if (outboxId == null) {
            return;
        }

        try {
            doneOutboxIds.put(outboxId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                    "outbox DONE enqueue interrupted. outboxId=" + outboxId, e);
        }
    }

    @Scheduled(fixedDelayString = "100")
    public void flush() {
        List<Long> outboxIds = new ArrayList<>(BATCH_SIZE);
        doneOutboxIds.drainTo(outboxIds, BATCH_SIZE);

        if (outboxIds.isEmpty()) {
            return;
        }

        try {
            quantityChangeEventUseCase.markDoneBatch(outboxIds);
        } catch (Exception e) {
            requeue(outboxIds);
            log.error(
                    "outbox DONE batch update 실패. count={}",
                    outboxIds.size(),
                    e
            );
        }
    }

    private void requeue(List<Long> outboxIds) {
        for (Long outboxId : outboxIds) {
            try {
                doneOutboxIds.put(outboxId);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error(
                        "outbox DONE 재등록 중 interrupt 발생. outboxId={}",
                        outboxId,
                        e
                );
                return;
            }
        }
    }
}