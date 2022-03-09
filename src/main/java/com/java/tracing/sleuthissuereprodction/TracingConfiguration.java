package com.java.tracing.sleuthissuereprodction;

import brave.baggage.BaggageField;
import brave.baggage.CorrelationScopeConfig;
import brave.context.slf4j.MDCScopeDecorator;
import brave.propagation.CurrentTraceContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TracingConfiguration {

    private static final String ADDITIONAL_TRACE_ID = "additionalTraceId";

    @Bean
    BaggageField tescoTraceIdField() {
        return BaggageField.create(ADDITIONAL_TRACE_ID);
    }

    // todo: this bean causes the issue!! if you comment out line 21 the issue disappears
    @Bean
    CurrentTraceContext.ScopeDecorator mdcScopeDecorator() {
        return MDCScopeDecorator.newBuilder()
                .clear()
                .add(CorrelationScopeConfig.SingleCorrelationField.newBuilder(tescoTraceIdField())
                        .flushOnUpdate()
                        .build())
                .build();
    }
}
