package com.epam.esm.service;

import com.epam.esm.dto.UserDTO;
import com.epam.esm.repository.UserRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class UserService {

  private final UserRepository userRepository;

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public List<UserDTO> getAllUsers() {
    return userRepository.findAll().stream()
        .map(user -> new UserDTO(user.getId(), user.getName()))
        .collect(Collectors.toList());
  }

  //TODO create single class to handle exceptions for jpa. Controller advice class
  // data access exceptions like in mode
  //TODO make description of exceptions
  //TODO implement try catch data

  //TODO implement this in service, and get purchases from each user
  public ResponseEntity<?> getUser(Long id) {
    return userRepository.findById(id)
        .map(user -> new ResponseEntity<>(new UserDTO(user.getId(), user.getName()), HttpStatus.OK ))
        .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
  }


}
