package com.v_payment.pay.order.config;

import com.v_payment.pay.order.infrastructure.kafka.QuantityChangeResultAggregationState;
import com.v_payment.pay.order.infrastructure.kafka.message.QuantityChangeResultEvent;
import com.v_payment.pay.order.infrastructure.kafka.message.QuantityChangeSummaryEvent;
import org.apache.kafka.common.serialization.Serde;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JacksonJsonSerde;

@Configuration
public class KafkaStreamsSerdeConfig {
    @Bean
    public Serde<QuantityChangeResultEvent> quantityChangeResultEventSerde() {
        return jsonSerde(QuantityChangeResultEvent.class);
    }

    @Bean
    public Serde<QuantityChangeResultAggregationState> quantityChangeResultAggregationStateSerde() {
        return jsonSerde(QuantityChangeResultAggregationState.class);
    }

    @Bean
    public Serde<QuantityChangeSummaryEvent> orderQuantityChangeSummaryEventSerde() {
        return jsonSerde(QuantityChangeSummaryEvent.class);
    }

    private <T> Serde<T> jsonSerde(Class<T> type) {
        JacksonJsonSerde<T> serde = new JacksonJsonSerde<>(type);
        serde.deserializer().addTrustedPackages("com.v_payment.pay");
        serde.deserializer().ignoreTypeHeaders();
        serde.serializer().setAddTypeInfo(false);
        return serde;
    }
}
