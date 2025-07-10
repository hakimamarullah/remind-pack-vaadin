package com.starline.base.api.users.impl;

import com.starline.base.api.dto.ApiResponse;
import com.starline.base.api.users.ResetPasswordService;
import com.starline.base.api.users.dto.ResetPasswordRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ResetPasswordSvc implements ResetPasswordService {

    @Qualifier("userClient")
    private final WebClient webClient;

    @Override
    public Mono<ApiResponse<String>> resetPassword(ResetPasswordRequest payload) {
        return webClient.post()
                .uri("/reset-password")
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<>() {});
    }
}
