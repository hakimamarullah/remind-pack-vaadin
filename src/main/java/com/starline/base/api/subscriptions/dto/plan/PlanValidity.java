package com.starline.base.api.subscriptions.dto.plan;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

import java.util.Objects;

@RegisterReflectionForBinding(PlanValidity.class)
public enum PlanValidity {

    WEEKLY, MONTHLY, YEARLY;

    @JsonCreator
    public static PlanValidity getType(String type) {
        if (Objects.isNull(type)) {
            return null;
        }
        for (PlanValidity requestType : values()) {
            if (requestType.name().equalsIgnoreCase(type)) {
                return requestType;
            }
        }
        return null;
    }
}
