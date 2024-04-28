package com.rando.springboot.randoJavaBackend.config;

import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public GroupedOpenApi api() {
        return GroupedOpenApi.builder()
                .group("/api/user")
                .packagesToScan("com.rando.springboot.randoJavaBackend") // 你的包名
                .build();
    }
}

