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
import jakarta.annotation.PostConstruct;

@Component
public class JwtUtil {
    private static final Logger logger = Logger.getLogger(JwtUtil.class.getName());
    private static final String AUTHORITIES_CLAIM = "auth";
    private SecretKey secretKey;

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;
    
    @PostConstruct
    public void init() {
        // Generate a secure key once during initialization
        try {
            // If the secret is at least 32 characters (256 bits), use it
            if (secret != null && secret.length() >= 32) {
                secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
                logger.info("Using configured JWT secret key");
            } else {
                // Otherwise, generate a secure random key
                secretKey = Jwts.SIG.HS256.key().build();
                logger.info("Generated secure random JWT key");
            }
        } catch (Exception e) {
            logger.severe("Error initializing JWT secret key: " + e.getMessage());
            // Fallback to a secure random key
            secretKey = Jwts.SIG.HS256.key().build();
            logger.info("Fallback to generated secure random JWT key");
        }
    }

    // Add a method to check if the secret is valid
    private void validateSecret() {
        if (secretKey == null) {
            throw new IllegalStateException("JWT secret key is not initialized properly");
        }
    }

    public String generateToken(User user) {
        validateSecret();
        try {
            Map<String, Object> claims = new HashMap<>();

            // Debug log to check user authorities
            logger.info("User authorities: " + user.getAuthorities());
            
            List<String> authorities = user.getAuthorities()
                    .stream()
                    .map(authority -> authority.getAuthority())
                    .collect(Collectors.toList());

            // Debug log to check extracted authorities
            logger.info("Extracted authorities: " + authorities);
            
            claims.put(AUTHORITIES_CLAIM, authorities);
            
            // Debug log to check user email
            logger.info("User email for token subject: " + user.getEmail());
            
            return createToken(claims, user.getEmail());
        } catch (Exception e) {
            logger.severe("Error generating JWT token: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to generate JWT token", e);
        }
    }

    public Claims extractAllClaims(String token) throws JwtException {
        validateSecret();
        if (token == null) {
            throw new JwtException("JWT token is null");
        }
        
        // Trim the token to remove any leading/trailing whitespace
        token = token.trim();
        
        if (token.isEmpty()) {
            throw new JwtException("JWT token is empty");
        }
        
        // Check for whitespace within the token
        if (token.contains(" ")) {
            throw new JwtException("JWT token contains whitespace");
        }
        
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException e) {
            logger.warning("Failed to extract JWT claims: " + e.getMessage());
            throw new JwtException("Failed to extract JWT claims: " + e.getMessage(), e);
        }
    }

    private String createToken(Map<String, Object> claims, String subject) {
        validateSecret();
        try {
            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + expiration);
            
            // Debug log for token building
            logger.info("Building JWT token with claims: " + claims.keySet());
            logger.info("Token subject: " + subject);
            logger.info("Token expiration: " + expiryDate);
            
            // Build token with claims using the initialized secretKey
            String token = Jwts.builder()
                    .claims(claims)
                    .subject(subject)
                    .issuedAt(now)
                    .expiration(expiryDate)
                    .signWith(secretKey)
                    .compact();
            
            // Debug log for successful token generation
            logger.info("JWT token generated successfully");
            
            return token;
        } catch (Exception e) {
            logger.severe("Error creating JWT token: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to create JWT token: " + e.getMessage(), e);
        }
    }

    public boolean validateToken(String token, User user) {
        validateSecret();
        try {
            final Claims claims = extractAllClaims(token);
            final String email = claims.getSubject();
            final Date expiration = claims.getExpiration();
            
            // Debug log for token validation
            logger.info("Validating token for user: " + user.getEmail());
            logger.info("Token subject (email): " + email);
            logger.info("Token expiration: " + expiration);
            
            return (email.equals(user.getEmail()) && !isTokenExpired(expiration));
        } catch (JwtException e) {
            logger.warning("Invalid JWT token: " + e.getMessage());
            return false;
        }
    }

    private boolean isTokenExpired(Date expiration) {
        return expiration.before(new Date());
    }
}
