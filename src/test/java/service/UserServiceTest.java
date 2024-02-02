package service;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

import com.epam.esm.dto.UserDTO;
import com.epam.esm.exceptions.CustomizedException;
import com.epam.esm.exceptions.ErrorCode;
import com.epam.esm.model.User;
import com.epam.esm.repository.UserRepository;
import com.epam.esm.service.UserService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

  public static final String USER_NAME = "Test user";
  public static final Long USER_ID = 1L;

  @InjectMocks
  private UserService userService;

  @Mock
  private UserRepository userRepository;

  @Mock
  User user;

  @Test
  public void getAllUsers_returnsPageOfUserDTOs() {
    // Given
    given(user.getId()).willReturn(USER_ID);
    given(user.getName()).willReturn(USER_NAME);

    Page<User> userPage = new PageImpl<>(singletonList(user));
    Pageable pageable = PageRequest.of(0, 10);
    Mockito.when(userRepository.findAll(pageable)).thenReturn(userPage);

    // When
    Page<UserDTO> resultPage = userService.getAllUsers(pageable);

    // Then
    assertEquals(1, resultPage.getNumberOfElements());
    assertEquals(USER_ID, resultPage.getContent().get(0).id());
    assertEquals(USER_NAME, resultPage.getContent().get(0).name());
  }

  @Test
  public void getAllUsers_throwsExceptionWhenDataAccessErrorOccurs() {
    // Given
    Pageable pageable = PageRequest.of(0, 10);
    Mockito.when(userRepository.findAll(pageable)).thenThrow(new EmptyResultDataAccessException(1));

    // When & Then
    CustomizedException exception = assertThrows(CustomizedException.class, () -> userService.getAllUsers(pageable));
    assertEquals("Error retrieving all users", exception.getMessage());
    assertEquals(ErrorCode.USER_DATABASE_ERROR, exception.getCode());
  }

  @Test
  public void getUser_returnsUserDTO_WhenUserIdExists() {
    // Given

    given(user.getId()).willReturn(USER_ID);
    given(user.getName()).willReturn(USER_NAME);
    Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));

    // When
    UserDTO result = userService.getUser(USER_ID);

    // Then
    assertEquals(USER_ID, result.id());
    assertEquals(USER_NAME, result.name());
  }

  @Test
  public void getUser_throwsNotFoundException_WhenUserIdDoesNotExist() {
    // Given
    Mockito.when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

    // When & Then
    CustomizedException exception = assertThrows(CustomizedException.class,
        () -> userService.getUser(USER_ID));

    assertEquals("Could not find any user with id " + USER_ID, exception.getMessage());
    assertEquals(ErrorCode.USER_NOT_FOUND, exception.getCode());
  }

  @Test
  public void getUser_throwsExceptionWhenDataAccessErrorOccurs() {
    // Given
    Mockito.when(userRepository.findById(USER_ID)).thenThrow(new EmptyResultDataAccessException(1));

    // When & Then
    CustomizedException exception = assertThrows(CustomizedException.class,
        () -> userService.getUser(USER_ID));

    assertEquals("Error retrieving user with id " + USER_ID, exception.getMessage());
    assertEquals(ErrorCode.USER_DATABASE_ERROR, exception.getCode());
  }
}
