package com.ledger.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public final class UserContext {

    private static final String HEADER_USER_ID = "X-User-Id";

    public static String getUserId() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) return "default";
        HttpServletRequest request = attrs.getRequest();
        if (request == null) return "default";
        Object attr = request.getAttribute("userId");
        if (attr != null && attr.toString().length() > 0) return attr.toString().trim();
        String uid = request.getHeader(HEADER_USER_ID);
        return uid != null && !uid.isBlank() ? uid.trim() : "default";
    }
}
