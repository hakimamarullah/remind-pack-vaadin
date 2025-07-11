package com.starline.base.api.resi.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;


@Data
public class ResiInfo {

    private String trackingNumber;
    private Long courierId;
    private String courierName;
    private String additionalValue1;

    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime lastCheckpointUpdate;

}
