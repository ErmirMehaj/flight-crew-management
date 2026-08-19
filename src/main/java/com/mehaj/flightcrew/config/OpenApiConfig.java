package com.mehaj.flightcrew.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI flightCrewManagementOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Flight Crew Management API")
                        .description("Manages pilots, crew members, aircraft, and flights, "
                                + "including scheduling, assignment, and availability business rules.")
                        .version("v1"));
    }
}
