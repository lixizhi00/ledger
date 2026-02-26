package com.ledger.service;

import com.ledger.entity.User;
import com.ledger.repository.UserRepository;
import com.ledger.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.jwt.secret:ledger-default-secret-key-at-least-32-bytes-long}")
    private String jwtSecret;

    public boolean isPasswordValid(String password) {
        if (password == null || password.length() < 6) return false;
        int types = 0;
        if (password.matches(".*\\d.*")) types++;
        if (password.matches(".*[a-zA-Z].*")) types++;
        if (password.matches(".*[^a-zA-Z0-9].*")) types++;
        return types >= 2;
    }

    @Transactional
    public User register(String phone, String username, String password) {
        if (userRepository.existsByPhone(phone)) {
            throw new IllegalArgumentException("该手机号已注册");
        }
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("该账户名已被使用");
        }
        if (!isPasswordValid(password)) {
            throw new IllegalArgumentException("密码需6位以上，且包含数字/字母/字符中的至少两种");
        }
        String hash = passwordEncoder.encode(password);
        User user = User.builder()
                .id(UUID.randomUUID().toString())
                .phone(phone.trim())
                .username(username.trim())
                .passwordHash(hash)
                .build();
        return userRepository.save(user);
    }

    public String login(String account, String password) {
        String acc = account != null ? account.trim() : "";
        String pwd = password != null ? password : "";
        if (acc.isEmpty()) {
            throw new IllegalArgumentException("请输入账号");
        }
        User user = userRepository.findByPhone(acc)
                .or(() -> userRepository.findByUsername(acc))
                .orElseThrow(() -> new IllegalArgumentException("账号或密码错误"));
        String hash = user.getPasswordHash();
        if (hash == null || !passwordEncoder.matches(pwd, hash)) {
            throw new IllegalArgumentException("账号或密码错误");
        }
        String userId = user.getId();
        if (userId == null || userId.isBlank()) {
            userId = user.getPhone();
        }
        return JwtUtil.createToken(userId, jwtSecret);
    }

    public String getUserIdFromToken(String token) {
        return JwtUtil.parseUserId(token, jwtSecret);
    }

    @Transactional
    public void resetPassword(String phone, String newPassword) {
        String p = phone != null ? phone.trim() : "";
        if (p.isEmpty()) {
            throw new IllegalArgumentException("请输入手机号");
        }
        if (!isPasswordValid(newPassword)) {
            throw new IllegalArgumentException("密码需6位以上，且包含数字/字母/字符中的至少两种");
        }
        User user = userRepository.findByPhone(p)
                .orElseThrow(() -> new IllegalArgumentException("该手机号未注册"));
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
