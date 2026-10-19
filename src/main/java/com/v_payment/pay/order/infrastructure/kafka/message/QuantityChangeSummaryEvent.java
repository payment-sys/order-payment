package com.v_payment.pay.order.infrastructure.kafka.message;

import com.v_payment.pay.order.domain.order.OrderItemInfo;

import java.util.List;

public record QuantityChangeSummaryEvent(
        String orderCode,
        QuantityChangeSummaryStatus status,
        List<OrderItemInfo> orderItems
) {
    public QuantityChangeSummaryEvent {
        orderItems = orderItems == null ? List.of() : List.copyOf(orderItems);
    }
}
