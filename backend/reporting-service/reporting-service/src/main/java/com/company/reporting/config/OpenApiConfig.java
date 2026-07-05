package com.company.reporting.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Reporting Service API")
                        .version("1.0.0")
                        .description("Analytics, trends, and reporting engine for Employee Pulse Survey Platform")
                        .contact(new Contact()
                                .name("HR Analytics Team")
                                .email("hr-analytics@company.com")));
    }
}
