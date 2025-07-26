package com.starline.base.api.subscriptions.dto.subscriptions;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@RegisterReflectionForBinding(SubscriptionInfo.class)
public class SubscriptionInfo {

    private String id;
    private String planName;
    private String planDescription;
    private SubscriptionStatus status;

    @JsonFormat(pattern = "dd MMM yyyy")
    private LocalDate effectiveDate;

    @JsonFormat(pattern = "dd MMM yyyy")
    private LocalDate expiryDate;

    private String paymentUrl;

    private String planCycle;
}
