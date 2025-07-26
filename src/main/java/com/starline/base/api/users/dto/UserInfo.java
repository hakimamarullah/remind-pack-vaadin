package com.starline.base.api.users.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@RegisterReflectionForBinding(UserInfo.class)
public class UserInfo {

    private Long id;

    private String mobilePhone;

    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime lastLogin;

    private Boolean enabled;
}
