package com.davemuirhead.metered_llm_gateway.spike;

import java.time.Instant;
import java.util.Optional;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.davemuirhead.metered_llm_gateway.stripe.MeteredPrice;
import com.davemuirhead.metered_llm_gateway.stripe.StripeCustomers;
import com.davemuirhead.metered_llm_gateway.stripe.StripeMeterEvents;
import com.davemuirhead.metered_llm_gateway.stripe.StripeMeters;
import com.davemuirhead.metered_llm_gateway.stripe.setup.DemoCustomer;
import com.stripe.model.Customer;
import com.stripe.model.billing.Meter;
import com.stripe.model.billing.MeterEvent;

@Component
@Profile("stripe-spike")
public class MeterEventSpike implements ApplicationRunner {

    private final StripeCustomers customers;
    private final StripeMeters meters;
    private final StripeMeterEvents meterEvents;
    private final DemoCustomer demoCustomer;

    public MeterEventSpike(StripeCustomers customers, StripeMeters meters, StripeMeterEvents meterEvents,DemoCustomer demoCustomer) {
        this.customers = customers;
        this.meters = meters;
        this.meterEvents = meterEvents;
        this.demoCustomer = demoCustomer;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Optional<Customer> customer = customers.find(demoCustomer.email());
        if (customer.isEmpty()) {
            System.out.println(
                    "No customer found for email: " + demoCustomer.email() + ". Please run StripeSetup first.");
            return;
        }

        Optional<Meter> inputMeter = meters.find(MeteredPrice.INPUT_TOKENS);
        if (inputMeter.isEmpty()) {
            System.out.println(
                    "No meter found for price: " + MeteredPrice.INPUT_TOKENS + ". Please run StripeSetup first.");
            return;
        }

        Optional<Meter> outputMeter = meters.find(MeteredPrice.OUTPUT_TOKENS);
        if (outputMeter.isEmpty()) {
            System.out.println(
                    "No meter found for price: " + MeteredPrice.OUTPUT_TOKENS + ". Please run StripeSetup first.");
            return;
        }

        testDuplicateIdentifier(customer.get(), inputMeter.get());
    }

    /*
     * Investigate what Stripe returns when a duplicate `identifier` is sent
     * and how long it takes for events to show up in event summaries.
     */
    private void testDuplicateIdentifier(Customer customer, Meter meter) throws Exception {

        String stripeCustomerId = customer.getId();
        MeteredPrice spec = MeteredPrice.INPUT_TOKENS;
        String identifier = "spike-dup-" + System.currentTimeMillis();
        Instant timestamp = Instant.now();
        long value_1 = 10L;
        long value_2 = 20L;

        sendEvent("original event", stripeCustomerId, spec, identifier, timestamp, value_1);
        sendEvent("duplicate event", stripeCustomerId, spec, identifier, timestamp, value_1);
        sendEvent("duplicate event with different value", stripeCustomerId, spec, identifier, timestamp, value_2);
    }

    private void sendEvent(String label, String stripeCustomerId, MeteredPrice spec, String identifier, Instant occurredAt, long value) {
        try {
            MeterEvent event = meterEvents.sendEvent(stripeCustomerId, spec, identifier, occurredAt, value);
            System.out.println(label + ": " + event);
        } catch (Exception e) {
            System.out.println(label + ": " + e.getMessage());
        }
    }
}