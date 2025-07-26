package com.starline.base.api.resi.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.aot.hint.annotation.RegisterReflection;

import java.time.LocalDateTime;


@Data
@AllArgsConstructor
@NoArgsConstructor
@RegisterReflection
public class ResiInfo {

    private String trackingNumber;
    private Long courierId;
    private String courierName;
    private String additionalValue1;

    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime lastCheckpointUpdate;

}
