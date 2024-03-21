package com.epam.esm.service;

import static com.epam.esm.exceptions.Messages.USER_ID_NOT_FOUND;
import static java.lang.Boolean.TRUE;

import com.epam.esm.dto.UserDTO;
import com.epam.esm.dto.UserRegistering;
import com.epam.esm.exceptions.CustomizedException;
import com.epam.esm.exceptions.ErrorCode;
import com.epam.esm.model.Role;
import com.epam.esm.model.User;
import com.epam.esm.repository.RoleRepository;
import com.epam.esm.repository.UserRepository;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  public UserService(UserRepository userRepository, RoleRepository roleRepository) {
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
  }

  public UserDTO registerUser(UserRegistering newUser){

    User registeredUser = new User();

    registeredUser.setEnable(TRUE);
    registeredUser.setName(newUser.name());
    registeredUser.setEmail(newUser.email());
    registeredUser.setUsername(newUser.username());
    registeredUser.setNotCryptedPassword(newUser.password());

    String encryptedPassword = passwordEncoder.encode(newUser.password());
    registeredUser.setPassword(encryptedPassword);

    Set<Role> role = Set.of(roleRepository.findById(1L).get());
    registeredUser.setRoles(role);

    userRepository.save(registeredUser);
    Long id = userRepository.findByUsername(registeredUser.getUsername()).getId();

    return new UserDTO(id, registeredUser.getUsername());
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
  public UserDTO getUser(Long userId) {
    try {
      return userRepository.findById(userId)
          .map(user -> new UserDTO(user.getId(), user.getName()))
          .orElseThrow(() -> new CustomizedException(USER_ID_NOT_FOUND.formatted(userId), ErrorCode.USER_NOT_FOUND));
    } catch (DataAccessException ex) {
      throw new CustomizedException("Error retrieving user with id " + userId, ErrorCode.USER_DATABASE_ERROR, ex);
    }
  }
}
