package com.starline.base.api.resi;

import com.starline.base.api.dto.ApiResponse;
import com.starline.base.api.resi.dto.AddResiRequest;
import com.starline.base.api.resi.dto.ResiInfo;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ResiService {

    Mono<ApiResponse<String>> addResi(AddResiRequest payload);

    Mono<ApiResponse<List<ResiInfo>>> getResiByUserId(Long userId);

    Mono<Void> deleteResiByTrackingNumberAndUserId(String trackingNumber, Long userId);
}
