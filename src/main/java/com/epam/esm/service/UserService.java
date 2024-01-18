package com.epam.esm.service;

import static com.epam.esm.exceptions.Messages.USER_ID_NOT_FOUND;

import com.epam.esm.dto.UserDTO;
import com.epam.esm.dto.UserReturnDTO;
import com.epam.esm.exceptions.CustomizedException;
import com.epam.esm.exceptions.ErrorCode;
import com.epam.esm.repository.UserRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

@Service
public class UserService {

  private final UserRepository userRepository;

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public List<UserDTO> getAllUsers() {
    try {
      return userRepository.findAll().stream()
          .map(user -> new UserDTO(user.getId(), user.getName()))
          .collect(Collectors.toList());
    } catch (DataAccessException ex) {
      throw new CustomizedException("Error retrieving all users", ErrorCode.USER_DATABASE_ERROR, ex);
    }
  }

  public UserReturnDTO getUser(Long userId) {
    try {
      return userRepository.findById(userId)
          .map(user -> new UserReturnDTO(user.getId(), user.getName()))
          .orElseThrow(() -> new CustomizedException(USER_ID_NOT_FOUND.formatted(userId), ErrorCode.USER_NOT_FOUND));
    } catch (DataAccessException ex) {
      throw new CustomizedException("Error retrieving user with id " + userId, ErrorCode.USER_DATABASE_ERROR, ex);
    }
  }

  //TODO create single class to handle exceptions for jpa.
  // Controller advice class data access exceptions like in mode
  //TODO make description of exceptions
  //TODO implement try catch data

}
