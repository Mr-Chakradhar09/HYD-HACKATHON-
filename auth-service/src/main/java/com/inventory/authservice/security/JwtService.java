package com.inventory.authservice.security;

import com.inventory.authservice.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    public String generateToken(User user) {

        List<String> roles =  user.getEmployee()
                .getRoles()
                .stream()
                .map(role -> role.getRoleName().name())
                .toList();
        return Jwts.builder()
                .subject(user.getEmployee().getEmployeeCode())
                .id(UUID.randomUUID().toString())
                .claim("employeeId", user.getEmployee().getId())
                .claim("employeeCode", user.getEmployee().getEmployeeCode())
                .claim("roles", roles)
                .claim("username", user.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey())
                .compact();
    }
    public String extractEmployeeCode(String token) {

        return extractClaim(
                token,
                Claims::getSubject
        );
    }

    public String extractUsername(String token) {
        return extractAllClaims(token)
                .get("username", String.class);
    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        return extractAllClaims(token).get("roles", List.class);
    }

    public boolean isTokenValid(
            String token,
            UserDetails userDetails) {

        String employeeCode =
                extractEmployeeCode(token);

        return employeeCode.equals(userDetails.getUsername())
                && !isTokenExpired(token);
    }
    public boolean isTokenExpired(String token) {

        return extractExpiration(token)
                .before(new Date());
    }
    public Date extractExpiration(String token) {

        return extractClaim(token, Claims::getExpiration);
    }
    public <T> T extractClaim(
            String token,
            Function<Claims, T> claimsResolver) {

        Claims claims = extractAllClaims(token);

        return claimsResolver.apply(claims);
    }
    public Claims extractAllClaims(String token) {

        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}