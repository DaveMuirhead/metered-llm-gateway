package com.davemuirhead.metered_llm_gateway.stripe;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.CustomerListParams;

@Component
public class StripeCustomers {

    private final StripeClient stripe;
    private final StripeProperties props;

    public StripeCustomers(StripeClient stripe, StripeProperties props) {
        this.stripe = stripe;
        this.props = props;
    }

    public Optional<Customer> find(String email) throws StripeException {
        Customer existing = stripe.v1().customers().list(
                CustomerListParams.builder()
                        .setEmail(email)
                        .setLimit(1L)
                        .build())
                .getData()
                .stream()
                .findFirst()
                .orElse(null);
        return Optional.ofNullable(existing);
    }

    public Customer create(String email, String name) throws StripeException {
        return stripe.v1().customers().create(
                CustomerCreateParams.builder()
                        .setEmail(email)
                        .setName(name)
                        .putMetadata("app", props.metadataApp())
                        .build());
    }

    public Customer findOrCreate(String email, String name) throws StripeException {
        Optional<Customer> existing = find(email);
        if (existing.isPresent()) {
            return existing.get();
        }
        return create(email, name);
    }
}
