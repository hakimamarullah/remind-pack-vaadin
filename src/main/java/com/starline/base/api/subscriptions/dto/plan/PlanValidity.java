package com.starline.base.api.subscriptions.dto.plan;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Objects;

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
