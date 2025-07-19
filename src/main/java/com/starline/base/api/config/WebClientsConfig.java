package com.starline.base.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientsConfig {


    @Bean
    @Primary
    public WebClient.Builder webClientBuilder(ObjectMapper objectMapper) {
        return WebClient.builder().filter(WebClientLoggingFilter.logAndHandleErrors(objectMapper));
    }

    @Bean(name = "userClient")
    public WebClient userClient(@Value("${url.svc.users:http://localhost:8082/users-svc}") String baseUrl,
                                WebClient.Builder clientBuilder) {
        return clientBuilder.baseUrl(baseUrl).build();

    }

    @Bean(name = "resiClient")
    public WebClient resiClient(@Value("${url.svc.resi:http://localhost:8083/resi-svc}") String baseUrl,
                                WebClient.Builder clientBuilder) {
        return clientBuilder.baseUrl(baseUrl).build();
    }

    @Bean(name = "subscriptionClient")
    public WebClient subscriptionClient(@Value("${url.svc.subscriptions:http://localhost:8083/resi-svc}") String baseUrl,
                                WebClient.Builder clientBuilder) {
        return clientBuilder.baseUrl(baseUrl).build();
    }

}
