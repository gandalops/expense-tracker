package com.fullStack.expenseTracker.security.jwt;

import java.security.Key;
import java.util.Date;

import com.fullStack.expenseTracker.security.UserDetailsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${app.jwtSecret}")
    private String jwtSecret; // Should be at least 256 bits (32 characters) long

    @Value("${app.jwtExpirationMs}")
    private int jwtExpirationMs; // Typically 86400000 (24 hours)

    /**
     * Generates a JWT token from authentication object
     */
    public String generateJwtToken(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        return Jwts.builder()
                .setSubject((userPrincipal.getEmail())) // Using email as subject
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(key(), SignatureAlgorithm.HS256) // Using HMAC-SHA256
                .compact();
    }

    /**
     * Creates the signing key from the base64 encoded secret
     */
    private Key key() {
        try {
            return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
        } catch (Exception e) {
            logger.error("Error creating JWT signing key: {}", e.getMessage());
            throw new IllegalStateException("Failed to initialize JWT signing key", e);
        }
    }

    /**
     * Extracts username (email) from JWT token
     */
    public String getUserNameFromJwtToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    /**
     * Validates JWT token and logs specific error cases
     */
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(authToken);
            return true;
        } catch (SignatureException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected JWT validation error: {}", e.getMessage());
        }

        return false;
    }

    /**
     * Gets remaining time until token expiration in milliseconds
     */
    public long getRemainingTimeFromToken(String token) {
        Date expiration = Jwts.parserBuilder()
                            .setSigningKey(key())
                            .build()
                            .parseClaimsJws(token)
                            .getBody()
                            .getExpiration();
        return expiration.getTime() - System.currentTimeMillis();
    }
}