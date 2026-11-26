package com.davemuirhead.metered_llm_gateway.stripe;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.Price;
import com.stripe.model.billing.Meter;
import com.stripe.param.PriceCreateParams;
import com.stripe.param.PriceListParams;

@Component
public class StripePrices {

    private final StripeClient stripe;
    private final StripeProperties props;

    public StripePrices(StripeClient stripe, StripeProperties props) {
        this.stripe = stripe;
        this.props = props;
    }

    public Optional<Price> find(MeteredPrice spec) throws StripeException {
        Price existing = stripe.v1().prices()
                .list(PriceListParams.builder().addLookupKey(spec.lookupKey()).build())
                .getData().stream().findFirst().orElse(null);
        if (existing != null) {
            return Optional.of(existing);
        }
        return Optional.empty();
    }

    public Price create(MeteredPrice spec, Meter meter) throws StripeException {
        return stripe.v1().prices().create(PriceCreateParams.builder()
                .setCurrency("usd")
                .setUnitAmountDecimal(spec.centsPerToken())
                .setLookupKey(spec.lookupKey())
                .setNickname(spec.displayName())
                .setRecurring(PriceCreateParams.Recurring.builder()
                        .setInterval(PriceCreateParams.Recurring.Interval.MONTH)
                        .setUsageType(PriceCreateParams.Recurring.UsageType.METERED)
                        .setMeter(meter.getId())
                        .build())
                .setProductData(PriceCreateParams.ProductData.builder()
                        .setName(spec.displayName())
                        .putMetadata("app", props.metadataApp())
                        .build())
                .putMetadata("app", props.metadataApp())
                .build());
    }

    public Price findOrCreate(MeteredPrice spec, Meter meter) throws StripeException {
        Optional<Price> existing = find(spec);
        if (existing.isPresent()) {
            return existing.get();
        }
        return create(spec, meter);
    }
}
