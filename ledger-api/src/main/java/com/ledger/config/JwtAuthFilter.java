package com.ledger.config;

import com.ledger.service.AuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(1)
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final AuthService authService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/auth/") || path.equals("/api/health");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String auth = request.getHeader("Authorization");
        String userId = null;
        if (auth != null && auth.startsWith("Bearer ")) {
            String token = auth.substring(7).trim();
            userId = authService.getUserIdFromToken(token);
        }
        if (userId == null) {
            userId = request.getHeader("X-User-Id");
            if (userId != null) userId = userId.trim();
        }
        if (userId != null && !userId.isBlank()) {
            request.setAttribute("userId", userId);
        }
        filterChain.doFilter(request, response);
    }
}
