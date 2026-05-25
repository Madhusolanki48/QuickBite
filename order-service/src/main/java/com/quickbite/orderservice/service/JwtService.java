package com.quickbite.orderservice.service;

import com.quickbite.orderservice.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JwtService {
    private final JwtProperties jwtProperties;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractRole(String token) {
        try {
            String role = extractAllClaims(token).get("role", String.class);
            return role != null ? role : "CUSTOMER";
        } catch (Exception e) {
            return "CUSTOMER";
        }
    }

    public String extractRestaurantId(String token) {
        try {
            return extractAllClaims(token).get("restaurantId", String.class);
        } catch (Exception e) {
            return null;
        }
    }

    public Long extractUserId(String token) {
        try {
            Object val = extractAllClaims(token).get("userId");
            if (val instanceof Number n) {
                return n.longValue();
            } else if (val instanceof String s) {
                return Long.parseLong(s);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        return extractUsername(token) != null
                && extractUsername(token).equalsIgnoreCase(userDetails.getUsername())
                && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> fn) {
        return fn.apply(extractAllClaims(token));
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtProperties.secret().getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32)
            throw new IllegalStateException("JWT secret must be at least 32 bytes long");
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
