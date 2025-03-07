package com.picktoss.picktossserver.domain.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class SignUpRequest {

    @NotBlank(message = "Name cannot be empty")
    @Size(min = 4, max = 20, message = "Name must be 4 ~ 20 characters")
    private String name;

    @NotBlank(message = "Password cannot be empty")
    @Size(min = 4, max = 20, message = "Password must be 4 ~ 20 characters")
    private String password;
}
