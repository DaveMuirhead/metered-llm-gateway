package com.davemuirhead.metered_llm_gateway.setup;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.stripe.StripeClient;

@Configuration
public class StripeConfig {
      @Bean
      StripeClient stripeClient(StripeProperties props) {
          return new StripeClient(props.apiKey());
      }
}
