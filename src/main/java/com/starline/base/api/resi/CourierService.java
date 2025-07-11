package com.starline.base.api.resi;

import com.starline.base.api.dto.ApiResponse;
import com.starline.base.api.resi.dto.CourierInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Mono;

public interface CourierService {

    Mono<ApiResponse<Page<CourierInfo>>> getCouriers(String name, Pageable pageable);
}
