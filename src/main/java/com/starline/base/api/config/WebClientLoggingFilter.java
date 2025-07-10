package com.starline.base.api.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.starline.base.api.dto.ApiResponse;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class WebClientLoggingFilter {

    public static ExchangeFilterFunction logAndHandleErrors(ObjectMapper objectMapper) {
        return ExchangeFilterFunction.ofResponseProcessor(response -> {
            if (response.statusCode().is2xxSuccessful()) {
                return logSuccess(response);
            } else {
                return response.bodyToMono(String.class)
                        .defaultIfEmpty("No body")
                        .flatMap(body -> {
                            try {
                                log.error("❌ Error {}: {}", response.statusCode().value(), body);
                                if (response.statusCode().isSameCodeAs(HttpStatus.BAD_REQUEST)) {
                                    ApiResponse<Map<String, String>> errorResponse = objectMapper.readValue(body,
                                            new TypeReference<>() {
                                            });
                                    return Mono.error(new ApiClientException(response.statusCode().value(), errorResponse));
                                } else if (response.statusCode().isSameCodeAs(HttpStatus.CONFLICT) ||
                                        response.statusCode().isSameCodeAs(HttpStatus.NOT_FOUND)) {
                                    ApiResponse<Object> errorResponse = objectMapper.readValue(body, new TypeReference<>() {
                                    });
                                    return Mono.error(new ApiClientException(response.statusCode().value(), errorResponse));
                                }

                                ApiResponse<Object> errorResponse = objectMapper.readValue(body, new TypeReference<>() {
                                });
                                return Mono.error(new ApiClientException(response.statusCode().value(), errorResponse));
                            } catch (Exception e) {
                                log.error("❌ Failed to parse error response body: {}", body, e);
                                return Mono.error(new RuntimeException("Unexpected error occurred"));
                            }
                        });
            }
        });
    }

    @Getter
    public static class ApiClientException extends RuntimeException {
        private final int httpStatusCode;
        private final ApiResponse<?> apiResponse;

        public ApiClientException(int httpStatusCode, ApiResponse<?> apiResponse) {
            this.httpStatusCode = httpStatusCode;
            this.apiResponse = apiResponse;
        }

        public List<ApiResponse.FieldError> getFieldErrors() {
            return Optional.ofNullable(apiResponse)
                    .map(ApiResponse::getFieldErrors)
                    .orElse(new ArrayList<>());
        }
    }

    private static Mono<ClientResponse> logSuccess(ClientResponse response) {
        return response.bodyToMono(String.class)
                .defaultIfEmpty("No body")
                .flatMap(body -> {
                    log.info("✅ Success Response: HTTP {} - {}", response.statusCode(), body);
                    return Mono.just(ClientResponse
                            .create(response.statusCode())
                            .headers(h -> h.addAll(response.headers().asHttpHeaders()))
                            .body(body)
                            .build());
                });
    }

    record ApiErrorResponse(String code, String message) {}
}
