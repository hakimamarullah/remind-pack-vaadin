package com.starline.base.api.resi.impl;

import com.starline.base.api.dto.ApiResponse;
import com.starline.base.api.resi.ResiService;
import com.starline.base.api.resi.dto.AddResiRequest;
import com.starline.base.api.resi.dto.ResiInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ResiSvc implements ResiService {

    @Qualifier("resiClient")
    private final WebClient webClient;

    @Override
    public Mono<ApiResponse<String>> addResi(AddResiRequest payload) {
        return webClient.post()
                .uri("/resi")
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<>() {});
    }

    @Override
    public Mono<ApiResponse<List<ResiInfo>>> getResiByUserId(Long userId) {
        return webClient.get()
                .uri("/resi/{userId}", userId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<>() {});
    }

    @Override
    public Mono<Void> deleteResiByTrackingNumberAndUserId(String trackingNumber, Long userId) {
        return webClient.delete()
                .uri("/resi/{trackingNumber}/{userId}", trackingNumber, userId)
                .retrieve()
                .toBodilessEntity()
                .then();
    }
}
