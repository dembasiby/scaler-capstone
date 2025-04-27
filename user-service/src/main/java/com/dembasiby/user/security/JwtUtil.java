package com.dembasiby.user.security;

import com.dembasiby.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@Component
public class JwtUtil {
    Logger logger = Logger.getLogger(this.getClass().getName());

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("authorities", user.getAuthorities());
        return createToken(claims, user.getUsername());
    }

    public Claims extractAllClaims(String token) throws JwtException {
       try {
           SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
           return Jwts.parser()
                   .verifyWith(key)
                   .build()
                   .parseSignedClaims(token)
                   .getPayload();

       } catch (JwtException e) {
           throw new JwtException("Failed to extract JWT claims: " + e.getMessage(), e);
       }
    }

    public boolean validateToken(String token, User user) {
        try {
            Claims claims = extractAllClaims(token);
            if (isTokenExpired(claims)) {
                return false;
            }

            final String username = claims.getSubject();
            return username.equals(user.getUsername());
        } catch (JwtException | IllegalArgumentException e) {
            logger.info("Invalid Jwt" + e.getMessage());
            return false;
        }
    }

    private boolean isTokenExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        SecretKey key = Keys.hmacShaKeyFor(secretBytes);
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }
}