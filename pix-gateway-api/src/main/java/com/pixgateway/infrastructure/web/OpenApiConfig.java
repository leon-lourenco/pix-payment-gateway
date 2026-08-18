package com.pixgateway.infrastructure.web;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI pixGatewayOpenApi() {
        return new OpenAPI().info(new Info()
                .title("PIX Gateway API")
                .description("Idempotent PIX-style transaction intake, backed by the outbox "
                        + "pattern for reliable event publishing to pix-ledger-worker.")
                .version("v1")
                .contact(new Contact().name("Leon Lourenço")
                        .url("https://github.com/leon-lourenco/pix-payment-gateway"))
                .license(new License().name("MIT").url("https://opensource.org/licenses/MIT")));
    }
}
