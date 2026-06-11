package com.ncba.rdas.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().info(new Info()
                .title("RDAS — Reference Data Aggregation Service")
                .description("Single REST/JSON API for country, currency, language and geographical reference data. Wraps the CountryInfo SOAP service with caching and resilience.")
                .version("1.0.0"));
    }
}
