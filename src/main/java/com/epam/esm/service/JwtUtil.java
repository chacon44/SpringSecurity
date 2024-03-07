package com.epam.esm.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.time.ZonedDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;
@Service
public class JwtUtil {


  @Value("${jwt.secret}")
  private String secret;
   public String generateToken(UserDetails userDetails) {
     //I'm returning a String for a received user details
      return Jwts.builder()
          .setSubject(userDetails.getUsername())
          .setExpiration(Date.from(ZonedDateTime.now().plusMinutes(50).toInstant()))
          .signWith(SignatureAlgorithm.HS256, secret)
          .compact();
    }
}
