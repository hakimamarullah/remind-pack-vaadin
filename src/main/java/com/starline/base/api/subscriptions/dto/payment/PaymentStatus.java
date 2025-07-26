package com.starline.base.api.subscriptions.dto.payment;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.springframework.aot.hint.annotation.RegisterReflection;

import java.util.Objects;

@RegisterReflection
public enum PaymentStatus {

    PENDING, SUCCESS, FAILED;

    @JsonCreator
    public static PaymentStatus getType(String type) {
        if (Objects.isNull(type)) {
            return null;
        }
        for (PaymentStatus requestType : values()) {
            if (requestType.name().equalsIgnoreCase(type)) {
                return requestType;
            }
        }
        return null;
    }
}
