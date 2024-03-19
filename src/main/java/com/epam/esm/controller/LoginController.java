package com.epam.esm.controller;

import static org.springframework.security.core.context.SecurityContextHolder.getContext;

import com.epam.esm.dto.JwtResponse;
import com.epam.esm.service.GithubUserService;
import com.epam.esm.service.JwtService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController()
public class LoginController {

  private final JwtService jwtService;
  private final GithubUserService githubUserService;

  public LoginController(JwtService jwtService,
      GithubUserService githubUserService) {
    this.jwtService = jwtService;
    this.githubUserService = githubUserService;
  }

  @GetMapping("/api/login")
  public JwtResponse login() {
    String username = getContext().getAuthentication().getPrincipal().toString();
    String jwt = jwtService.generateToken(username);

    return new JwtResponse(jwt);
  }

  @GetMapping("/api/github/login")
  public JwtResponse githubLogin() {
    String jwt = jwtService.generateToken(githubUserService.getDefaultGithubUsername());
    return new JwtResponse(jwt);
  }

}
