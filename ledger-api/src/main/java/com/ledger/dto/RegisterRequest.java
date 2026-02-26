package com.ledger.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "请输入手机号")
    @Size(max = 20)
    private String phone;

    @NotBlank(message = "请输入账户名")
    @Size(min = 1, max = 64)
    private String username;

    @NotBlank(message = "请输入密码")
    @Size(min = 6, max = 32)
    private String password;

    @NotBlank(message = "请确认密码")
    private String confirmPassword;
}
