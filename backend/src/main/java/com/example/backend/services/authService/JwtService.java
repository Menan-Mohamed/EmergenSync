package com.example.backend.services.authService;

import com.example.backend.entities.User;
import com.example.backend.exception.BadRequestException;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {

    @Value("${jwt.auth.secret}")
    private String authSecret;

    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 24; // 24h

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(authSecret.getBytes());
    }

    public String generateAuthToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("user_id", user.getId());
        claims.put("type", user.getType());
        claims.put("role", user.getRole());

        return Jwts.builder()
                .subject(user.getUsername())
                .claims(claims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey())
                .compact();
    }

    public Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        }catch(Exception e){
            throw new BadRequestException("Invalid or expired token");
        }
    }


    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public Integer extractUserId(String token) {
        Claims claims = parseClaims(token);
        return (Integer) claims.get("user_id");
    }


    public boolean isTokenValid(String token, UserDetails userDetails) {
        Claims claims = parseClaims(token);
        boolean expired = claims.getExpiration().before(new Date());
        return userDetails.getUsername().equals(claims.getSubject()) && !expired;
    }

    public String extractTokenFromHeader(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        throw new BadRequestException("Invalid Authorization header");
    }
}