package com.starline.base.api.subscriptions.dto.payment;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@RegisterReflectionForBinding(CreateOrderRequest.class)
public class CreateOrderRequest {

    @NotNull(message = "userId is required")
    @Min(value = 1, message = "userId must be greater than 0")
    private Long userId;

    @NotNull(message = "planId is required")
    @Min(value = 1, message = "planId must be greater than 0")
    private Long planId;

}
