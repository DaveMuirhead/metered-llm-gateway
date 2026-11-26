package com.davemuirhead.metered_llm_gateway.stripe.setup;

import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.davemuirhead.metered_llm_gateway.stripe.MeteredPrice;
import com.davemuirhead.metered_llm_gateway.stripe.StripeCustomers;
import com.davemuirhead.metered_llm_gateway.stripe.StripeMeters;
import com.davemuirhead.metered_llm_gateway.stripe.StripePrices;
import com.davemuirhead.metered_llm_gateway.stripe.StripeSubscriptions;
import com.stripe.model.Customer;
import com.stripe.model.Price;
import com.stripe.model.billing.Meter;

@Component
@Profile("stripe-setup")
public class StripeSetup implements ApplicationRunner {

    private final StripeMeters meters;
    private final StripePrices prices;
    private final StripeCustomers customers;
    private final StripeSubscriptions subscriptions;
    private final DemoCustomer demoCustomer;

    public StripeSetup(StripeMeters meters, StripePrices prices, StripeCustomers customers,
            StripeSubscriptions subscriptions, DemoCustomer demoCustomer) {
        this.meters = meters;
        this.prices = prices;
        this.customers = customers;
        this.subscriptions = subscriptions;
        this.demoCustomer = demoCustomer;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Meter inputMeter = meters.findOrCreate(MeteredPrice.INPUT_TOKENS);
        Meter outputMeter = meters.findOrCreate(MeteredPrice.OUTPUT_TOKENS);
        Price inputPrice = prices.findOrCreate(MeteredPrice.INPUT_TOKENS, inputMeter);
        Price outputPrice = prices.findOrCreate(MeteredPrice.OUTPUT_TOKENS, outputMeter);
        Customer customer = customers.findOrCreate(demoCustomer.email(), demoCustomer.name());
        subscriptions.findOrCreate(customer, List.of(inputPrice, outputPrice));
    }
}
