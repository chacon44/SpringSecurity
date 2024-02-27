package com.epam.esm.service;

import com.epam.esm.model.User;
import com.epam.esm.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
      User userDetails = userRepository.findByUsername(username);


      if (userDetails == null) {
        throw new UsernameNotFoundException("User with username: " + username + " not found");
      }

      // get a role from the database from each user
      // Getting user from database and convert to spring security user format
      // Create and return the Spring Security UserDetails object
      return org.springframework.security.core.userdetails.User.builder()
          .username(userDetails.getUsername())
          .password(userDetails.getPassword())
          .roles("ADMIN") // user.getRole
          .build();
    }
}
