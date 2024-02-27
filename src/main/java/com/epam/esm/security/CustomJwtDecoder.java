package com.epam.esm.security;

import com.epam.esm.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import java.time.Instant;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

@Component
public class CustomJwtDecoder implements JwtDecoder {

    private final JwtService jwtService;

    public CustomJwtDecoder(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Jwt decode(String token) throws JwtException {
        JwtParser parser = jwtService.getTokenParser();
        try {
            Jws<Claims> jwt = parser.parseClaimsJws(token);
            Claims claims = jwt.getBody();
            Instant issuedAt = claims.getIssuedAt().toInstant();
            Instant expiresAt = claims.getExpiration().toInstant();
            JwsHeader header = jwt.getHeader();
            // Note, that both Header and Claims extend Map<String,Object> and thus can be passed to Spring Security JWT directly
            return new Jwt(token, issuedAt, expiresAt, header, claims);
        } catch (UnsupportedJwtException e) {
            throw new JwtException("Attempt to authorize with JWT of unsupported type", e);
        } catch (MalformedJwtException e) {
            throw new JwtException("Attempt to authorize with malformed JWT", e);
        } catch (SignatureException e) {
            throw new JwtException("Attempt to authorize with JWT with invalid signature", e);
        } catch (ExpiredJwtException e) {
            throw new JwtException("Attempt to authorize with expired JWT", e);
        } catch (IllegalArgumentException e) {
            throw new JwtException("Attempt to authorize with empty/blank JWT", e);
        }
    }
}
