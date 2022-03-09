package com.java.tracing.sleuthissuereprodction;

import brave.baggage.BaggageField;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AdditionalTraceIdBaggageField {

    private final BaggageField tescoTraceIdBaggage;

    public boolean updateValue(String value) {
        return tescoTraceIdBaggage.updateValue(value);
    }
}
