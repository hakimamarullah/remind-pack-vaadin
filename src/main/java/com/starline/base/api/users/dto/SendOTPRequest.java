package com.starline.base.api.users.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@Data
@RegisterReflectionForBinding(SendOTPRequest.class)
public class SendOTPRequest {

    @NotBlank(message = "Please Provide Valid Mobile Number")
    private String mobilePhone;
}
