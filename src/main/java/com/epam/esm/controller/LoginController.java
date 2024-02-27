package com.epam.esm.controller;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.security.core.context.SecurityContextHolder.*;

import com.epam.esm.dto.JwtResponse;
import com.epam.esm.service.GithubUserService;
import com.epam.esm.service.JwtHelper;
import com.epam.esm.service.JwtService;
import com.epam.esm.service.UserDetailsServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.WebAttributes;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequestMapping("/api")
public class LoginController {

  private final JwtService jwtService;
  private final GithubUserService githubUserService;

  public LoginController(JwtService jwtService,
      GithubUserService githubUserService) {
    this.jwtService = jwtService;
    this.githubUserService = githubUserService;
  }

  @GetMapping("/login")
  public JwtResponse login() {
    User principal = (User) getContext().getAuthentication().getPrincipal();
    String jwt = jwtService.generateToken(principal.getUsername());

    return new JwtResponse(jwt);
  }

  @GetMapping("/github/login")
  public JwtResponse githubLogin() {
    String jwt = jwtService.generateToken(githubUserService.getDefaultGithubUsername());
    return new JwtResponse(jwt);
  }

}
