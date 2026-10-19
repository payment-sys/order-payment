package com.v_payment.pay.order.domain;

import com.v_payment.pay.order.infrastructure.kafka.message.QuantityChangeSummaryEvent;
import com.v_payment.pay.order.infrastructure.kafka.message.QuantityChangeSummaryStatus;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class QuantityChangeSummaries {
    private final List<QuantityChangeSummaryEvent> messages;
    private final List<QuantityChangeSummaryEvent> failedMessages;
    private final List<QuantityChangeSummaryEvent> successMessages;
    private final Set<String> failedOrderCodes;
    private final Set<String> successOrderCodes;

    private QuantityChangeSummaries(List<QuantityChangeSummaryEvent> messages) {
        this.messages = validateMessages(messages);
        this.failedMessages = extractMessagesByStatus(this.messages, QuantityChangeSummaryStatus.FAILED);
        this.successMessages = extractSuccessMessagesExcludingFailedOrders(this.messages, this.failedMessages);

        this.failedOrderCodes = extractOrderCodes(this.failedMessages);
        this.successOrderCodes = extractOrderCodes(this.successMessages);
    }

    public boolean isEmpty() {
        return messages.isEmpty();
    }

    public boolean hasFailedOrders() {
        return !failedMessages.isEmpty();
    }

    public boolean hasSuccessOrders() {
        return !successMessages.isEmpty();
    }

    public List<QuantityChangeSummaryEvent> getFailedMessages() {
        return failedMessages;
    }

    public List<QuantityChangeSummaryEvent> getSuccessMessages() {
        return successMessages;
    }

    public Set<String> getFailedOrderCodes() {
        return failedOrderCodes;
    }

    public Set<String> getSuccessOrderCodes() {
        return successOrderCodes;
    }

    public List<String> getOrderCodes() {
        return messages.stream()
                .map(QuantityChangeSummaryEvent::orderCode)
                .toList();
    }

    public List<QuantityChangeSummaryEvent> getMessages() {
        return messages;
    }

    public static QuantityChangeSummaries create(List<QuantityChangeSummaryEvent> messages) {
        return new QuantityChangeSummaries(messages);
    }

    private List<QuantityChangeSummaryEvent> validateMessages(List<QuantityChangeSummaryEvent> messages) {
        if (messages == null || messages.isEmpty()) {
            return List.of();
        }

        messages.forEach(this::validateMessage);
        return List.copyOf(messages);
    }

    private void validateMessage(QuantityChangeSummaryEvent message) {
        if (message == null) {
            throw new IllegalArgumentException("quantity change summary message is required.");
        }
        if (message.orderCode() == null || message.orderCode().isBlank()) {
            throw new IllegalArgumentException("orderCode is required.");
        }
        if (message.status() == null) {
            throw new IllegalArgumentException("status is required.");
        }
        if (message.orderItems() == null) {
            throw new IllegalArgumentException("orderItems is required.");
        }
    }

    private List<QuantityChangeSummaryEvent> extractMessagesByStatus(
            List<QuantityChangeSummaryEvent> messages,
            QuantityChangeSummaryStatus status
    ) {
        return messages.stream()
                .filter(message -> message.status() == status)
                .toList();
    }

    private List<QuantityChangeSummaryEvent> extractSuccessMessagesExcludingFailedOrders(
            List<QuantityChangeSummaryEvent> messages,
            List<QuantityChangeSummaryEvent> failedMessages
    ) {
        Set<String> failedOrderCodes = extractOrderCodes(failedMessages);

        return messages.stream()
                .filter(message -> message.status() == QuantityChangeSummaryStatus.SUCCESS)
                .filter(message -> !failedOrderCodes.contains(message.orderCode()))
                .toList();
    }

    private Set<String> extractOrderCodes(List<QuantityChangeSummaryEvent> messages) {
        return messages.stream()
                .map(QuantityChangeSummaryEvent::orderCode)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
