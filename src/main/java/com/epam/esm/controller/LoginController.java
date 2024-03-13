package com.epam.esm.controller;

import static org.springframework.security.core.context.SecurityContextHolder.*;

import com.epam.esm.service.JwtTokenService;
import com.epam.esm.service.LoginService;
import com.epam.esm.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController {
  @Autowired
  private UserDetailsServiceImpl userDetailsService;

  @Autowired
  private LoginService loginService;

  @Autowired
  private JwtTokenService jwtTokenService;

  @PostMapping("/login")
  public ResponseEntity<?> loginUser(
      @RequestParam("username") String username,
      @RequestParam("password") String password
  ) {

    boolean isAuthenticated = loginService.checkAuthentication(username, password);

    if(isAuthenticated) {
      return ResponseEntity.ok().body("User logged in successfully.");
    } else {
      return ResponseEntity.status(401).body("Authentication failed, user or password not correct.");
    }

//    SecurityContext context = getContext();
//    Authentication authentication = context.getAuthentication();
//
//    String token = jwtTokenService.generateToken(authentication);
//
//    return ResponseEntity.ok().body(token);

  }
}
