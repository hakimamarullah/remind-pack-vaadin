package com.starline.base.api.users.impl;

import com.starline.base.api.dto.ApiResponse;
import com.starline.base.api.users.RegistrationService;
import com.starline.base.api.users.dto.RegisterUserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class RegistrationSvc implements RegistrationService {

    @Qualifier("userClient")
    private final WebClient webClient;


    @Override
    public Mono<ApiResponse<String>> registerUser(RegisterUserRequest payload) {
       return webClient.post()
                .uri("/register")
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<>() {
                });

    }
}
