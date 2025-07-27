package com.starline.base.api.resi.impl;

import com.starline.base.api.dto.ApiResponse;
import com.starline.base.api.dto.PageWrapper;
import com.starline.base.api.resi.CourierService;
import com.starline.base.api.resi.dto.CourierInfo;
import com.starline.base.api.utils.ClientRequestUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@RegisterReflectionForBinding({
        ApiResponse.class,
        Page.class,
        PageWrapper.class,
        CourierInfo.class
})
public class CourierSvc implements CourierService {

    @Qualifier("resiClient")
    private final WebClient client;

    @Override
    public Mono<ApiResponse<Page<CourierInfo>>> getCouriers(String name, Pageable pageable) {
        return client.get()
                .uri(ClientRequestUtils.withPageable("/couriers", pageable, Map.of("name", StringUtils.defaultString(name))))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<PageWrapper<CourierInfo>>>() {})
                .map(it -> mapToPage(it, pageable));
    }

    private <U> ApiResponse<Page<U>> mapToPage(ApiResponse<PageWrapper<U>> response, Pageable pageable) {
        ApiResponse<Page<U>> apiResponse = new ApiResponse<>();
        apiResponse.setCode(response.getCode());
        apiResponse.setMessage(response.getMessage());
        Optional.ofNullable(response.getData())
                .map(it -> it.toPage(pageable))
                .ifPresent(apiResponse::setData);
        return apiResponse;
    }


}
