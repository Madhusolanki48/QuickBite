package com.quickbite.authservice.service;

import com.quickbite.authservice.config.JwtProperties;
import com.quickbite.authservice.model.AppUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JwtService {
    private final JwtProperties jwtProperties;
    public String generateToken(AppUser user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().name());
        if (user.getId() != null) {
            claims.put("userId", user.getId());
        }
        claims.put("name", user.getFirstName() + " " + user.getLastName());
        return buildToken(claims, user.getEmail());
    }
    public String extractUsername(String token) { return extractClaim(token, Claims::getSubject); }
    public boolean isTokenValid(String token, UserDetails userDetails) {
        return extractUsername(token) != null
                && extractUsername(token).equalsIgnoreCase(userDetails.getUsername())
                && !isTokenExpired(token)
                && userDetails.isEnabled();
    }
    public long getExpirationMs() { return jwtProperties.expirationMs(); }
    private String buildToken(Map<String, Object> extraClaims, String subject) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.expirationMs());
        return Jwts.builder().claims(extraClaims).subject(subject).issuedAt(now).expiration(expiry).signWith(getSigningKey(), Jwts.SIG.HS256).compact();
    }
    private boolean isTokenExpired(String token) { return extractExpiration(token).before(new Date()); }
    private Date extractExpiration(String token) { return extractClaim(token, Claims::getExpiration); }
    private <T> T extractClaim(String token, Function<Claims, T> fn) { return fn.apply(extractAllClaims(token)); }
    private Claims extractAllClaims(String token) { return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload(); }
    private SecretKey getSigningKey() {
        if (jwtProperties.secret() == null || jwtProperties.secret().isBlank()) {
            throw new IllegalStateException("JWT secret is not configured");
        }
        byte[] keyBytes = jwtProperties.secret().getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) throw new IllegalStateException("JWT secret must be at least 32 bytes long");
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
