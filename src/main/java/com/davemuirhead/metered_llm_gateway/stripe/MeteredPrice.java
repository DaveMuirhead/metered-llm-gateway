package com.davemuirhead.metered_llm_gateway.stripe;

import java.math.BigDecimal;

/**
 * One billable token stream: a Stripe meter plus the metered price attached to it.
 *
 * @param eventName      meter event name the gateway sends usage under (immutable in Stripe)
 * @param displayName    human-readable meter and product name
 * @param lookupKey      stable price lookup key, used to find the price on later runs
 * @param centsPerToken  unit price in cents per token; Stripe allows up to 12 decimal places
 */
public record MeteredPrice(String eventName, String displayName, String lookupKey, BigDecimal centsPerToken) {

    /** $3.00 per million input tokens. */
    public static final MeteredPrice INPUT_TOKENS = new MeteredPrice(
            "llm_input_tokens", "LLM input tokens", "llm_input_tokens_usd_v1", new BigDecimal("0.0003"));

    /** $15.00 per million output tokens. */
    public static final MeteredPrice OUTPUT_TOKENS = new MeteredPrice(
            "llm_output_tokens", "LLM output tokens", "llm_output_tokens_usd_v1", new BigDecimal("0.0015"));
}
