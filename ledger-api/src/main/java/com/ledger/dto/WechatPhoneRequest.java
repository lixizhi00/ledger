package com.ledger.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WechatPhoneRequest {

    @NotBlank(message = "code不能为空")
    private String code;
}
