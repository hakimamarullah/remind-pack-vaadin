package com.starline.base.api.subscriptions;

import com.starline.base.api.dto.ApiResponse;
import com.starline.base.api.subscriptions.dto.payment.CreateOrderRequest;
import com.starline.base.api.subscriptions.dto.payment.OrderSummary;
import com.starline.base.api.subscriptions.dto.payment.PaymentInfo;
import com.starline.base.api.subscriptions.dto.plan.PlanInfo;
import com.starline.base.api.subscriptions.dto.subscriptions.HasActiveSubscription;
import com.starline.base.api.subscriptions.dto.subscriptions.SubscriptionInfo;
import reactor.core.publisher.Mono;

import java.util.List;

public interface SubscriptionService {
    Mono<ApiResponse<PaymentInfo>> createOrder(CreateOrderRequest request);

    Mono<ApiResponse<OrderSummary>> getOrderSummary(Long planId);

    Mono<ApiResponse<List<SubscriptionInfo>>> getSubscriptions(Long userId);

    Mono<ApiResponse<HasActiveSubscription>> hasActiveSubscription(Long userId);

    Mono<ApiResponse<List<PlanInfo>>> getAvailablePlans();
}
