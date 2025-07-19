package com.starline.base.api.subscriptions.impl;

import com.starline.base.api.dto.ApiResponse;
import com.starline.base.api.subscriptions.SubscriptionService;
import com.starline.base.api.subscriptions.dto.payment.CreateOrderRequest;
import com.starline.base.api.subscriptions.dto.payment.OrderSummary;
import com.starline.base.api.subscriptions.dto.payment.PaymentInfo;
import com.starline.base.api.subscriptions.dto.plan.PlanInfo;
import com.starline.base.api.subscriptions.dto.subscriptions.HasActiveSubscription;
import com.starline.base.api.subscriptions.dto.subscriptions.SubscriptionInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;


@Service
@RequiredArgsConstructor
public class SubscriptionSvc implements SubscriptionService {

    @Qualifier("subscriptionClient")
    private final WebClient subscriptionWebClient;

    @Override
    public Mono<ApiResponse<PaymentInfo>> createOrder(CreateOrderRequest request) {
        return subscriptionWebClient.post()
                .uri("/orders")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<>() {});
    }

    @Override
    public Mono<ApiResponse<OrderSummary>> getOrderSummary(Long planId) {
        return subscriptionWebClient.get()
                .uri("/orders/{planId}/summary", planId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<>() {});
    }



    @Override
    public Mono<ApiResponse<List<SubscriptionInfo>>> getSubscriptions(Long userId) {
        return subscriptionWebClient.get()
                .uri("/subscriptions/users/{userId}", userId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<>() {});
    }

    @Override
    public Mono<ApiResponse<HasActiveSubscription>> hasActiveSubscription(Long userId) {
        return subscriptionWebClient.get()
                .uri("/subscriptions/users/{userId}/has-active-subscription", userId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<>() {});
    }

    @Override
    public Mono<ApiResponse<List<PlanInfo>>> getAvailablePlans() {
        return subscriptionWebClient.get()
                .uri("/plans")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<>() {});
    }
}
