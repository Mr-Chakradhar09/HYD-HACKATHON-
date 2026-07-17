package com.inventory.forecastservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.util.List;

@Component
public class JwtValidator {
    @Value("${jwt.secret:VGhpc0lzQVN1cGVyU2VjcmV0S2V5Rm9ySldUU2lnbmluZ1RoYXRNdXN0QmVBdExlYXN0MzJCeXRlc0xvbmc=}") private String secretKey;
    public Claims extractAllClaims(String token) { return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload(); }
    public String extractEmployeeCode(String token) { return extractAllClaims(token).getSubject(); }
    @SuppressWarnings("unchecked") public List<String> extractRoles(String token) { return extractAllClaims(token).get("roles", List.class); }
    public boolean isTokenValid(String token) { try { return !extractAllClaims(token).getExpiration().before(new java.util.Date()); } catch (Exception e) { return false; } }
    private SecretKey getSigningKey() { return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey)); }
}
