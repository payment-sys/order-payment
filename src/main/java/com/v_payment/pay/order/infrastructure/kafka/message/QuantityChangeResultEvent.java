package com.v_payment.pay.order.infrastructure.kafka.message;

public record QuantityChangeResultEvent(
        String orderCode,
        Long productId,
        String productName,
        Long unitPrice,
        Integer changeCount,
        QuantityChangeStatus changeStatus,
        Integer productsCount
) {
}
