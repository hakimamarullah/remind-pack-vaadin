package com.starline.base.api.resi.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

import java.time.LocalDateTime;


@Data
@RegisterReflectionForBinding(ResiInfo.class)
public class ResiInfo {

    private String trackingNumber;
    private Long courierId;
    private String courierName;
    private String additionalValue1;

    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime lastCheckpointUpdate;

}
