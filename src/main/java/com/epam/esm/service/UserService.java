package com.epam.esm.service;

import static com.epam.esm.exceptions.Messages.USER_ID_NOT_FOUND;

import com.epam.esm.dto.UserDTO;
import com.epam.esm.exceptions.CustomizedException;
import com.epam.esm.exceptions.ErrorCode;
import com.epam.esm.model.User;
import com.epam.esm.repository.UserRepository;
import java.util.Optional;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class UserService {

  private final UserRepository userRepository;

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public Page<UserDTO> getAllUsers(Pageable pageable) {
    try {
      Page<User> userPage = userRepository.findAll(pageable);
      return userPage.map(user -> new UserDTO(user.getId(), user.getName()));
    } catch (DataAccessException ex) {
      throw new CustomizedException("Error retrieving all users", ErrorCode.USER_DATABASE_ERROR, ex);
    }
  }

  public UserDTO getUser(Long userId) {
    try {
      return userRepository.findById(userId)
          .map(user -> new UserDTO(user.getId(), user.getName()))
          .orElseThrow(() -> new CustomizedException(USER_ID_NOT_FOUND.formatted(userId), ErrorCode.USER_NOT_FOUND));
    } catch (DataAccessException ex) {
      throw new CustomizedException("Error retrieving user with id " + userId, ErrorCode.USER_DATABASE_ERROR, ex);
    }
  }

  public Optional<String> getUserMostUsedTag() {
      User userWithHighestCost = getUserWithHighestCost();
      return getMostUsedTagFromUser(userWithHighestCost);
  }

  private Optional<String> getMostUsedTagFromUser(User user) {
    try {
      return userRepository.getMostUsedTag();
    } catch (DataAccessException e) {
      throw new CustomizedException("Error retrieving most used tag of user with ID: " + user.getId(), ErrorCode.USER_DATABASE_ERROR, e);
    }
  }

  private User getUserWithHighestCost() {
    try {
      return userRepository.findUserWithHighestOrdersCost().orElseThrow(
          () -> new CustomizedException("No users found", ErrorCode.USER_NOT_FOUND)
      );
    } catch (DataAccessException e) {
      throw new CustomizedException("Error retrieving user with highest purchase cost.", ErrorCode.USER_DATABASE_ERROR, e);
    }
  }
}
