package com.davemuirhead.metered_llm_gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

// Dummy key: StripeProperties requires one, and no test here calls Stripe
@SpringBootTest(properties = "stripe.api-key=sk_test_dummy")
@Import(TestcontainersConfiguration.class)
class MeteredLlmGatewayApplicationTests {

	@Test
	void contextLoads() {
	}

}
