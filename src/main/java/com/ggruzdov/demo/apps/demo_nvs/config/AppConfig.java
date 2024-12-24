package com.ggruzdov.demo.apps.demo_nvs.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class AppConfig {

    @Bean
    public RestClient restClient(RestTemplateBuilder builder) {
        builder.connectTimeout(Duration.ofSeconds(5));
        builder.readTimeout(Duration.ofSeconds(30));

        return RestClient.builder(builder.build()).build();
    }

    @Bean
    public ExecutorService executorService() {
        return Executors.newFixedThreadPool(10);
    }
}
