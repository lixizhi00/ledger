package com.ledger.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public final class JwtUtil {

    private static final String ISSUER = "ledger-api";
    private static final long EXPIRE_MS = 7 * 24 * 60 * 60 * 1000L; // 7 days
    private static final int MIN_KEY_BYTES = 32; // JJWT 要求至少 256 位

    private static byte[] ensureKeyBytes(String secret) {
        if (secret == null || secret.isEmpty()) {
            secret = "ledger-default-secret-key-at-least-32-bytes-long";
        }
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < MIN_KEY_BYTES) {
            byte[] padded = new byte[MIN_KEY_BYTES];
            System.arraycopy(bytes, 0, padded, 0, bytes.length);
            for (int i = bytes.length; i < MIN_KEY_BYTES; i++) {
                padded[i] = (byte) secret.charAt(i % secret.length());
            }
            return padded;
        }
        return bytes;
    }

    public static String createToken(String userId, String secret) {
        SecretKey key = Keys.hmacShaKeyFor(ensureKeyBytes(secret));
        return Jwts.builder()
                .subject(userId)
                .issuer(ISSUER)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRE_MS))
                .signWith(key)
                .compact();
    }

    public static String parseUserId(String token, String secret) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(ensureKeyBytes(secret));
            Jws<Claims> jws = Jwts.parser().verifyWith(key).build()
                    .parseSignedClaims(token);
            return jws.getPayload().getSubject();
        } catch (JwtException e) {
            return null;
        }
    }
}
