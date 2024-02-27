package com.epam.esm.service;

import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

@Service
public class GithubUserService {

  private static final String GITHUB_USERNAME = "github_user";

  public String getDefaultGithubUsername() { return GITHUB_USERNAME; }

  public boolean isGithubUser(String username) { return GITHUB_USERNAME.equals(username);}


  public Collection<GrantedAuthority> getGithubUserAuthorities() {
    return List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
  }
}
