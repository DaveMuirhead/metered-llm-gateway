package com.davemuirhead.metered_llm_gateway.stripe;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.billing.Meter;
import com.stripe.param.billing.MeterCreateParams;
import com.stripe.param.billing.MeterListParams;

@Component
public class StripeMeters {

    private final StripeClient stripe;

    public StripeMeters(StripeClient stripe) {
        this.stripe = stripe;
    }

    public Optional<Meter> find(MeteredPrice spec) throws StripeException {
        MeterListParams params = MeterListParams.builder()
                .setStatus(MeterListParams.Status.ACTIVE)
                .build();
        for (Meter meter : stripe.v1().billing().meters().list(params).autoPagingIterable()) {
            if (spec.eventName().equals(meter.getEventName())) {
                return Optional.of(meter);
            }
        }
        return Optional.empty();
    }

    public Meter create(MeteredPrice spec) throws StripeException {
        return stripe.v1().billing().meters().create(
                MeterCreateParams.builder()
                        .setDisplayName(spec.displayName())
                        .setEventName(spec.eventName())
                        .setDefaultAggregation(MeterCreateParams.DefaultAggregation.builder()
                                .setFormula(MeterCreateParams.DefaultAggregation.Formula.SUM)
                                .build())
                        .build());

    }

    public Meter findOrCreate(MeteredPrice spec) throws StripeException {
        Optional<Meter> existing = find(spec);
        if (existing.isPresent()) {
            return existing.get();
        }
        return create(spec);
    }
}
