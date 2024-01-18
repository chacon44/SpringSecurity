package com.epam.esm.service;

import static com.epam.esm.exceptions.Messages.USER_ID_NOT_FOUND;

import com.epam.esm.dto.UserDTO;
import com.epam.esm.dto.UserReturnDTO;
import com.epam.esm.exceptions.CustomizedException;
import com.epam.esm.exceptions.ErrorCode;
import com.epam.esm.model.Tag;
import com.epam.esm.model.User;
import com.epam.esm.repository.UserRepository;
import java.util.Comparator;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
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

  public UserReturnDTO getUser(Long userId) {
    try {
      return userRepository.findById(userId)
          .map(user -> new UserReturnDTO(user.getId(), user.getName()))
          .orElseThrow(() -> new CustomizedException(USER_ID_NOT_FOUND.formatted(userId), ErrorCode.USER_NOT_FOUND));
    } catch (DataAccessException ex) {
      throw new CustomizedException("Error retrieving user with id " + userId, ErrorCode.USER_DATABASE_ERROR, ex);
    }
  }

  public Optional<Entry<String, Long>> getUserMostUsedTag() {
    User userWithHighestCost = userRepository.findUserWithHighestOrdersCost()
        .orElseThrow(() -> new RuntimeException("Couldn't find any user"));

    return userWithHighestCost.getOrders().stream()
        .flatMap(order -> order.getCertificate().getTags().stream())
        .collect(Collectors.groupingBy(Tag::getName, Collectors.counting()))
        .entrySet().stream().min(Entry.comparingByValue(Comparator.reverseOrder()));
  }
}
