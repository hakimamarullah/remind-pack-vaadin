package com.starline.base.api.users.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SendOTPRequest {

    @NotBlank(message = "Please Provide Valid Mobile Number")
    private String mobilePhone;
}
