package com.davemuirhead.metered_llm_gateway.stripe;

import java.math.BigDecimal;
import java.time.Instant;

import org.springframework.stereotype.Component;

import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.billing.Meter;
import com.stripe.model.billing.MeterEvent;
import com.stripe.model.billing.MeterEventSummary;
import com.stripe.param.billing.MeterEventCreateParams;
import com.stripe.param.billing.MeterEventSummaryListParams;

@Component
public class StripeMeterEvents {

    private final StripeClient stripe;

    public StripeMeterEvents(StripeClient stripe) {
        this.stripe = stripe;
    }

    public MeterEvent sendEvent(String stripeCustomerId, MeteredPrice spec, String identifier, Instant occurredAt,
            long value) throws StripeException {
        return stripe.v1().billing().meterEvents().create(
                MeterEventCreateParams.builder()
                        .setEventName(spec.eventName())
                        .setIdentifier(identifier)
                        .setTimestamp(occurredAt.getEpochSecond())
                        .putPayload("stripe_customer_id", stripeCustomerId)
                        .putPayload("value", Long.toString(value))
                        .build());
    }

    public BigDecimal summaryTotal(String stripeCustomerId, Meter meter, long start, long end,
            MeterEventSummaryListParams.ValueGroupingWindow groupingWindow) throws StripeException {
        MeterEventSummaryListParams.Builder builder = MeterEventSummaryListParams.builder()
                .setCustomer(stripeCustomerId)
                .setStartTime(start)
                .setEndTime(end);
        if (groupingWindow != null) {
            builder.setValueGroupingWindow(groupingWindow);
        }
        MeterEventSummaryListParams params = builder.build();
        BigDecimal total = BigDecimal.ZERO;
        for (MeterEventSummary s : stripe.v1().billing().meters()
                .eventSummaries().list(meter.getId(), params).autoPagingIterable()) {
            total = total.add(s.getAggregatedValue());
        }
        return total;
    }

}
