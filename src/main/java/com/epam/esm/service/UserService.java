package com.epam.esm.service;

import static com.epam.esm.exceptions.Messages.USER_ID_NOT_FOUND;

import com.epam.esm.dto.UserDTO;
import com.epam.esm.dto.UserResponseDTO;
import com.epam.esm.exceptions.CustomizedException;
import com.epam.esm.exceptions.ErrorCode;
import com.epam.esm.model.User;
import com.epam.esm.repository.UserRepository;
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

  /**
   * Retrieves a page of Users, represented as UserDTOs.
   *
   * @param pageable Paging details for the page of Users to be retrieved.
   * @return A Page of UserDTOs.
   * @throws CustomizedException If there is an error retrieving Users from the database.
   */
  public Page<UserDTO> getAllUsers(Pageable pageable) {
    try {
      Page<User> userPage = userRepository.findAll(pageable);
      return userPage.map(user -> new UserDTO(user.getId(), user.getName()));
    } catch (DataAccessException ex) {
      throw new CustomizedException("Error retrieving all users", ErrorCode.USER_DATABASE_ERROR, ex);
    }
  }

  /**
   * Retrieves a User by id, represented as a UserResponseDTO.
   *
   * @param userId The id of the User to be retrieved.
   * @return A UserResponseDTO of the User.
   * @throws CustomizedException If the User id is not found or there is an error retrieving the User from the database.
   */
  public UserResponseDTO getUser(Long userId) {
    try {
      return userRepository.findById(userId)
          .map(user -> new UserResponseDTO(user.getId(), user.getName()))
          .orElseThrow(() -> new CustomizedException(USER_ID_NOT_FOUND.formatted(userId), ErrorCode.USER_NOT_FOUND));
    } catch (DataAccessException ex) {
      throw new CustomizedException("Error retrieving user with id " + userId, ErrorCode.USER_DATABASE_ERROR, ex);
    }
  }
}
