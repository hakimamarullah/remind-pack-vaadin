package com.starline.base.api.resi.dto;

import lombok.Builder;
import lombok.Data;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@Data
@Builder(toBuilder = true)
@RegisterReflectionForBinding(CourierInfo.class)
public class CourierInfo {

    private Long id;
    private String code;
    private String name;
}
