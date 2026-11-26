package com.davemuirhead.metered_llm_gateway.stripe.setup;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Validated
@ConfigurationProperties(prefix = "stripe.demo-customer")
public record DemoCustomer(@NotBlank @Email String email, @NotBlank String name) {
}