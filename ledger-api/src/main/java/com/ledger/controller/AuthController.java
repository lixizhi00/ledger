package com.ledger.controller;

import com.ledger.dto.AuthResponse;
import com.ledger.dto.LoginRequest;
import com.ledger.dto.RegisterRequest;
import com.ledger.dto.ResetPasswordRequest;
import com.ledger.dto.WechatPhoneRequest;
import com.ledger.entity.User;
import com.ledger.service.AuthService;
import com.ledger.service.WechatPhoneService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final WechatPhoneService wechatPhoneService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        if (!req.getPassword().equals(req.getConfirmPassword())) {
            throw new IllegalArgumentException("两次密码输入不一致");
        }
        User user = authService.register(req.getPhone(), req.getUsername(), req.getPassword());
        String token = authService.login(user.getPhone(), req.getPassword());
        return ResponseEntity.ok(new AuthResponse(token, user.getId()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        String token = authService.login(req.getAccount(), req.getPassword());
        String userId = authService.getUserIdFromToken(token);
        return ResponseEntity.ok(new AuthResponse(token, userId));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        if (!req.getNewPassword().equals(req.getConfirmPassword())) {
            throw new IllegalArgumentException("两次密码输入不一致");
        }
        authService.resetPassword(req.getPhone(), req.getNewPassword());
        return ResponseEntity.ok(Map.of("message", "密码已重置，请登录"));
    }

    /** 微信小程序：用 getPhoneNumber 返回的 code 兑换手机号，未配置 appid/secret 时返回 400 */
    @PostMapping("/wechat-phone")
    public ResponseEntity<Map<String, String>> wechatPhone(@Valid @RequestBody WechatPhoneRequest req) {
        if (!wechatPhoneService.isConfigured()) {
            throw new IllegalStateException("未配置微信小程序，请手动输入手机号");
        }
        String phone = wechatPhoneService.getPhoneByCode(req.getCode());
        return ResponseEntity.ok(Map.of("phone", phone));
    }
}
