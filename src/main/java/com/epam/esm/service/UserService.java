package com.epam.esm.service;

import static com.epam.esm.exceptions.Messages.USER_ID_NOT_FOUND;
import static java.util.Map.Entry.comparingByValue;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

import com.epam.esm.dto.UserDTO;
import com.epam.esm.dto.UserResponseDTO;
import com.epam.esm.exceptions.CustomizedException;
import com.epam.esm.exceptions.ErrorCode;
import com.epam.esm.model.Tag;
import com.epam.esm.model.User;
import com.epam.esm.repository.UserRepository;
import java.util.Map.Entry;
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

  public UserResponseDTO getUser(Long userId) {
    try {
      return userRepository.findById(userId)
          .map(user -> new UserResponseDTO(user.getId(), user.getName()))
          .orElseThrow(() -> new CustomizedException(USER_ID_NOT_FOUND.formatted(userId), ErrorCode.USER_NOT_FOUND));
    } catch (DataAccessException ex) {
      throw new CustomizedException("Error retrieving user with id " + userId, ErrorCode.USER_DATABASE_ERROR, ex);
    }
  }

  public Optional<Entry<String, Long>> getUserMostUsedTag() {
    try {
      User userWithHighestCost = getUserWithHighestCost();
      return getMostUsedTagFromUser(userWithHighestCost);
    } catch (DataAccessException e) {
      throw new CustomizedException("Error retrieving user with highest cost or their most used tag.", ErrorCode.USER_DATABASE_ERROR, e);
    }
  }

  private Optional<Entry<String, Long>> getMostUsedTagFromUser(User user) {
    try {
      //query
      return user.getOrders().stream() // for each order of that user
          .flatMap(order -> order
              .getCertificate().getTags().stream()) //get all tags from the purchased certificate
          .collect(groupingBy(Tag::getName, counting())) //group them by name and appearances
          .entrySet().stream().max(comparingByValue());
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
