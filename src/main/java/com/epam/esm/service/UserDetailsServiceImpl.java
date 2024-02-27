package com.epam.esm.service;

import com.epam.esm.model.User;
import com.epam.esm.repository.UserRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

  private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

  @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
      User user = userRepository.findByUsername(username);


      if (user == null) {
        throw new UsernameNotFoundException("User with username: " + username + " not found");
      }

    List<GrantedAuthority> authorities = user.getRoles().stream()
        .map(role -> {
          logger.debug("Mapping role: {}", role.getName());
          return new SimpleGrantedAuthority(role.getName().toUpperCase());
        })
        .collect(Collectors.toList());

    logger.debug("User roles loaded: {}", authorities);

      return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), authorities);    }
}
