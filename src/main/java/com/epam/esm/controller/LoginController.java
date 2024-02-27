package com.epam.esm.controller;

import com.epam.esm.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController {

  @Autowired
  private LoginService loginService;
  @PostMapping("/api/login")
  public ResponseEntity<?> loginUser(
      @RequestParam("username") String username,
      @RequestParam("password") String password) {

    boolean isAuthenticated = loginService.checkAuthentication(username, password);

    if(isAuthenticated) {
      return ResponseEntity.ok().body("User logged in successfully.");
    } else {
      return ResponseEntity.status(401).body("Authentication failed, user or password not correct.");
    }
  }
}
