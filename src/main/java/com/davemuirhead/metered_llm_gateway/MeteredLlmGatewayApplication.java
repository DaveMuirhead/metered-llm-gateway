package com.davemuirhead.metered_llm_gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class MeteredLlmGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(MeteredLlmGatewayApplication.class, args);
	}

}
