package com.starline.base.api.subscriptions.dto.subscriptions;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.springframework.aot.hint.annotation.RegisterReflection;

import java.util.Objects;

@RegisterReflection
public enum SubscriptionStatus {

    PENDING, ACTIVE, CANCELLED, EXPIRED;

    @JsonCreator
    public static SubscriptionStatus getType(String type) {
        if (Objects.isNull(type)) {
            return null;
        }
        for (SubscriptionStatus requestType : values()) {
            if (requestType.name().equalsIgnoreCase(type)) {
                return requestType;
            }
        }
        return null;
    }
}
