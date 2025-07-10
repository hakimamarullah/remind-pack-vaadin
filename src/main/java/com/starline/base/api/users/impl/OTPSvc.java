package com.starline.base.api.users.impl;

import com.starline.base.api.users.OTPService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class OTPSvc implements OTPService {

    @Qualifier("userClient")
    private final WebClient webClient;

    @Async
    @Override
    public void sendOTPAsync(String phoneNumber) {
        webClient.post()
                .uri("/otp/send")
                .bodyValue(Map.of("mobilePhone", phoneNumber))
                .retrieve()
                .toBodilessEntity()
                .subscribe();
    }
}
