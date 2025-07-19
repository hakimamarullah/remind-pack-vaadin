package com.starline.base.api.subscriptions.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class OrderSummary {

    private String planPrice;
    private String taxTotal;
    private String taxRate;
    private String grandTotal;
    private String planName;
    private String planDescription;
    private Long planId;
}
