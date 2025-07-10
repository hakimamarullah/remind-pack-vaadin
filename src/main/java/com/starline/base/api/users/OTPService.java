package com.starline.base.api.users;

public interface OTPService {

    void sendOTPAsync(String phoneNumber);
}
