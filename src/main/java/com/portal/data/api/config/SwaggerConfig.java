package com.portal.data.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI customAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Portal Metadata API")
                        .description("Portal Metadata API Management")
                        .version("0.0.1")
                        .contact(new Contact()
                                .name("Developer: Nathan")
                                .email("nathan@jkt1.ebdesk.com")));
    }
}
