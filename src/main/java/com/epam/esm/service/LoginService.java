package com.epam.esm.service;

import com.epam.esm.model.User;
import com.epam.esm.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class LoginService {
  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  public boolean checkAuthentication(String username, String password) {

    User user = userRepository.findByUsername(username);
    if (user != null) {
      return passwordEncoder.matches(password, user.getPassword());
    }
    return false;
  }

  //add permission checker
  //check role and give permission to POST, GET, DELETE, PATCH
}
