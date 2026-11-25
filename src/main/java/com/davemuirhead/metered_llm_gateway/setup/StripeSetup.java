package com.davemuirhead.metered_llm_gateway.setup;

import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Price;
import com.stripe.model.Subscription;
import com.stripe.model.billing.Meter;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.CustomerListParams;
import com.stripe.param.PriceCreateParams;
import com.stripe.param.PriceListParams;
import com.stripe.param.PriceRetrieveParams;
import com.stripe.param.SubscriptionCreateParams;
import com.stripe.param.SubscriptionListParams;
import com.stripe.param.billing.MeterCreateParams;
import com.stripe.param.billing.MeterListParams;

@Component
@Profile("stripe-setup")
public class StripeSetup implements ApplicationRunner {

    static final String DEMO_CUSTOMER_EMAIL = "demo-tenant@example.com";
    static final String DEMO_CUSTOMER_NAME = "Demo Tenant";
    static final String METADATA_APP = "metered-llm-gateway";

    private final StripeClient stripe;

    public StripeSetup(StripeClient stripe) {
        this.stripe = stripe;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Meter inputMeter = maybeCreateMeter(MeteredPrice.INPUT_TOKENS);
        Meter outputMeter = maybeCreateMeter(MeteredPrice.OUTPUT_TOKENS);
        Price inputPrice = maybeCreatePrice(MeteredPrice.INPUT_TOKENS, inputMeter);
        Price outputPrice = maybeCreatePrice(MeteredPrice.OUTPUT_TOKENS, outputMeter);
        Customer customer = maybeCreateCustomer();
        maybeCreateSubscription(customer, List.of(inputPrice, outputPrice));
    }

    private Meter maybeCreateMeter(MeteredPrice spec) throws StripeException {
        MeterListParams params = MeterListParams.builder()
                .setStatus(MeterListParams.Status.ACTIVE)
                .build();
        for (Meter existing : stripe.v1().billing().meters().list(params).autoPagingIterable()) {
            if (spec.eventName().equals(existing.getEventName())) {
                System.out.println("Meter " + spec.eventName() + " already exists: " + existing.getId());
                return existing;
            }
        }
        
        Meter created = stripe.v1().billing().meters().create(
                MeterCreateParams.builder()
                        .setDisplayName(spec.displayName())
                        .setEventName(spec.eventName())
                        .setDefaultAggregation(MeterCreateParams.DefaultAggregation.builder()
                                .setFormula(MeterCreateParams.DefaultAggregation.Formula.SUM)
                                .build())
                        .build());
        System.out.println("Created meter " + created.getEventName() + " with ID " + created.getId());
        return created;
    }

    private Price maybeCreatePrice(MeteredPrice spec, Meter meter) throws StripeException {
        Price existing = stripe.v1().prices()
                .list(PriceListParams.builder().addLookupKey(spec.lookupKey()).build())
                .getData().stream().findFirst().orElse(null);
        if (existing != null) {
            System.out.println("Price " + spec.lookupKey() + " already exists.");
            return existing;
        }

        Price created = stripe.v1().prices().create(PriceCreateParams.builder()
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
                        .putMetadata("app", METADATA_APP)
                        .build())
                .putMetadata("app", METADATA_APP)
                .build());
        System.out.println("Created price " + created.getId() + "  " + spec.lookupKey());
        return created;
    }

    private Customer maybeCreateCustomer() throws StripeException {
        CustomerListParams listParams = CustomerListParams.builder()
                .setEmail(DEMO_CUSTOMER_EMAIL)
                .setLimit(1L)
                .build();
        Customer existing = stripe.v1().customers().list(listParams).getData().stream()
                .findFirst()
                .orElse(null);
        if (existing != null) {
            System.out.println("Customer " + DEMO_CUSTOMER_EMAIL + " already exists.");
            return existing;
        }
        
        Customer created = stripe.v1().customers().create(CustomerCreateParams.builder()
                .setEmail(DEMO_CUSTOMER_EMAIL)
                .setName(DEMO_CUSTOMER_NAME)
                .putMetadata("app", METADATA_APP)
                .build());
        System.out.println("Created customer " + created.getId() + "  " + DEMO_CUSTOMER_EMAIL);
        return created;
    }

    private Subscription maybeCreateSubscription(Customer customer, List<Price> prices) throws StripeException {
        SubscriptionListParams listParams = SubscriptionListParams.builder()
                .setCustomer(customer.getId())
                .setLimit(1L)
                .build();
        Subscription existing = stripe.v1().subscriptions().list(listParams).getData().stream()
                .findFirst()
                .orElse(null);
        if (existing != null) {
            System.out.println("Subscription for customer " + customer.getId() + " already exists.");
            return existing;
        }

        SubscriptionCreateParams.Builder builder = SubscriptionCreateParams.builder()
                .setCustomer(customer.getId())
                .putMetadata("app", METADATA_APP);
        for (Price price : prices) {
            builder.addItem(SubscriptionCreateParams.Item.builder()
                    .setPrice(price.getId())
                    .build());
        }
        Subscription created = stripe.v1().subscriptions().create(builder.build());
        System.out.println("Created subscription " + created.getId() + " for customer " + customer.getId());
        return created;
    }
}
