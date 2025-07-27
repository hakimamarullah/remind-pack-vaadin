package com.starline.base.api.subscriptions.dto.plan;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.aot.hint.annotation.RegisterReflection;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@RegisterReflection
public class PlanInfo {


    private Long id;


    private String name;


    private String description;


    private Integer price;

    private String priceDisplay;


    private PlanValidity validity;

    private String validityDisplay;


    private Boolean enabled;
}
