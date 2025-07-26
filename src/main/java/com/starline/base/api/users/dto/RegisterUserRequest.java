package com.starline.base.api.users.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@Data
@Builder(toBuilder = true)
@RegisterReflectionForBinding(RegisterUserRequest.class)
public class RegisterUserRequest {

    @NotBlank(message = "Please Provide Valid Phone Number")
    @Length(message = "Phone Number maximum length is 15", max = 15)
    private String phoneNumber;

    @NotBlank(message = "Please Provide Valid OTP")
    private String otp;

    @NotBlank(message = "Please Provide Valid Password")
    @Length(max = 30, message = "Password maximum length is 30")
    private String password;

    @NotBlank(message = "Please Provide Valid Confirm Password")
    private String confirmPassword;
}
