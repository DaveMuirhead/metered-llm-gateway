package com.davemuirhead.metered_llm_gateway.stripe;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Price;
import com.stripe.model.Subscription;
import com.stripe.param.SubscriptionCreateParams;
import com.stripe.param.SubscriptionListParams;

@Component
public class StripeSubscriptions {

    private final StripeClient stripe;
    private final StripeProperties props;

    public StripeSubscriptions(StripeClient stripe, StripeProperties props) {
        this.stripe = stripe;
        this.props = props;
    }

    public Optional<Subscription> find(Customer customer) throws StripeException {
        Subscription existing = stripe.v1().subscriptions().list(
                SubscriptionListParams.builder()
                        .setCustomer(customer.getId())
                        .setLimit(1L)
                        .build())
                .getData()
                .stream()
                .findFirst()
                .orElse(null);
        return Optional.ofNullable(existing);
    }

    public Subscription create(Customer customer, List<Price> prices) throws StripeException {
        SubscriptionCreateParams.Builder builder = SubscriptionCreateParams.builder()
                .setCustomer(customer.getId())
                .putMetadata("app", props.metadataApp());
        for (Price price : prices) {
            builder.addItem(SubscriptionCreateParams.Item.builder()
                    .setPrice(price.getId())
                    .build());
        }
        return stripe.v1().subscriptions().create(builder.build());
    }

    public Subscription findOrCreate(Customer customer, List<Price> prices) throws StripeException {
        Optional<Subscription> existing = find(customer);
        if (existing.isPresent()) {
            return existing.get();
        }
        return create(customer, prices);
    }
}
