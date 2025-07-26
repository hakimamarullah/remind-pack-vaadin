package com.starline.base.api.subscriptions.dto.subscriptions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@Data
@AllArgsConstructor
@NoArgsConstructor
@RegisterReflectionForBinding(HasActiveSubscription.class)
public class HasActiveSubscription {

    private boolean isActiveSubscription;
}
