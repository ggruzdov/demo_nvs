package com.ggruzdov.demo.apps.demo_nvs.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Demo NVS API")
                .version("1.0")
                .description("API documentation for the image management and slideshow operations"))
            .servers(List.of(
                new Server()
                    .url("http://localhost:8080/")
                    .description("Local server")
            ));
    }
}