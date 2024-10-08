package com.heartlink.charge.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ChargeConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
