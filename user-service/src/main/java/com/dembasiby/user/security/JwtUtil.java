package com.dembasiby.user.security;

import com.dembasiby.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Component
public class JwtUtil {
    private static final Logger logger = Logger.getLogger(JwtUtil.class.getName());
    private static final String AUTHORITIES_CLAIM = "auth";

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();

        List<String> authorities = user.getAuthorities()
                .stream().map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        claims.put(AUTHORITIES_CLAIM, authorities);
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

    public List<SimpleGrantedAuthority> extractAuthorities(Claims claims) {
        @SuppressWarnings("unchecked")
        List<String> authorities = claims.get(AUTHORITIES_CLAIM, List.class);
        return authorities.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    public boolean validateToken(String token, User user) {
        try {
            Claims claims = extractAllClaims(token);
            if (isTokenExpired(claims)) {
                return false;
            }
            return claims.getSubject().equals(user.getUsername());
        } catch (JwtException | IllegalArgumentException e) {
            logger.info("Invalid JWT: " + e.getMessage());
            return false;
        }
    }

    private boolean isTokenExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }
}