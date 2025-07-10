package com.starline.base.api.users.impl;

import com.starline.base.api.dto.ApiResponse;
import com.starline.base.api.users.ResetPasswordService;
import com.starline.base.api.users.dto.ResetPasswordRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ResetPasswordSvc implements ResetPasswordService {

    private

    @Override
    public Mono<ApiResponse<String>> resetPassword(ResetPasswordRequest payload) {
        return null;
    }
}
