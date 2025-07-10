package com.starline.base.api.users;

import com.starline.base.api.dto.ApiResponse;
import com.starline.base.api.users.dto.RegisterUserRequest;
import reactor.core.publisher.Mono;

public interface RegistrationService {

    Mono<ApiResponse<String>> registerUser(RegisterUserRequest payload);
}
