package org.rafli.project.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI marketDataOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Market Data Service API")
                        .description("API for retrieving aggregated market data candles")
                        .version("v1.0.0"));
    }
}
