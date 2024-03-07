package com.epam.esm.security;

import com.epam.esm.model.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
  private AuthenticationManager authenticationManager;

  @Value("${jwt.secret}")
  private String secret;
  @Override
  protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
      FilterChain chain, Authentication authResult) throws IOException, ServletException {

    // User successfully authenticated, generate JWT
    String token = Jwts.builder()
        .setSubject(((User) authResult.getPrincipal()).getUsername())
        .signWith(SignatureAlgorithm.HS256, secret)
        .compact();

    // Add JWT to Authorization header
    response.addHeader("Authorization", "Bearer " + token);
  }


}
