package com.davemuirhead.metered_llm_gateway.setup;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;

@Validated 
@ConfigurationProperties(prefix = "stripe")
public record StripeProperties(@NotBlank String apiKey) {

}
