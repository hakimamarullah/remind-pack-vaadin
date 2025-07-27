package com.starline.base.api.resi.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.aot.hint.annotation.RegisterReflection;

@Data
@Builder(toBuilder = true)
@RegisterReflection
public class AddResiRequest {

    @NotBlank(message = "trackingNumber is required")
    private String trackingNumber;

    @NotNull(message = "userId is required")
    @Min(value = 1, message = "userId must be greater than 0")
    private Long userId;

    @NotNull(message = "courierId is required")
    @Min(value = 1, message = "courierId must be greater than 0")
    private Long courierId;

    @Length(max = 255, message = "additionalValue1 must be less than 255 characters")
    private String additionalValue1;
}
