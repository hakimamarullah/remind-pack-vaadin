package com.starline.base.api.subscriptions.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@RegisterReflectionForBinding(PaymentInfo.class)
public class PaymentInfo {

    private String snapUrl;
}
