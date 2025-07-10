package com.starline.base.api.users;

import com.starline.base.api.dto.ApiResponse;
import com.starline.base.api.users.dto.ResetPasswordRequest;
import reactor.core.publisher.Mono;

public interface ResetPasswordService {

    Mono<ApiResponse<String>> resetPassword(ResetPasswordRequest payload);
}
