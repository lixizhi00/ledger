package com.ledger.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequest {

    @NotBlank(message = "请输入手机号")
    @Size(max = 20)
    private String phone;

    @NotBlank(message = "请输入新密码")
    @Size(min = 6, max = 32)
    private String newPassword;

    @NotBlank(message = "请确认新密码")
    private String confirmPassword;
}
