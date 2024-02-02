package controller;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.esm.controller.UserController;
import com.epam.esm.dto.UserDTO;
import com.epam.esm.model.User;
import com.epam.esm.service.AuditReaderService;
import com.epam.esm.service.UserService;
import java.util.List;
import org.hamcrest.Matchers;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.query.AuditQuery;
import org.hibernate.envers.query.AuditQueryCreator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

  @InjectMocks
  private UserController userController;

  @Mock
  private UserService userService;

  @Mock
  private AuditReaderService auditReaderService;

  @Mock
  AuditReader auditReader;

  @Mock
  AuditQuery auditQuery;

  @Mock
  AuditQueryCreator auditQueryCreator;

  private MockMvc mockMvc;

  @BeforeEach
  public void setup(){
    this.mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
  }

  @Test
  public void testGetAllUsers() throws Exception {
    // Given
    List<UserDTO> users = List.of(
        new UserDTO(1L, "User1"),
        new UserDTO(2L, "User2")
    );
    Page<UserDTO> userDTOPage = new PageImpl<>(users);

    // Mocks
    when(userService.getAllUsers(any())).thenReturn(userDTOPage);

    // When & Then
    mockMvc.perform(get("/users"))
        .andExpect(status().isOk())
        .andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("User1")));
  }

  @Test
  public void testGetUser() throws Exception {
    // Given
    UserDTO userDTO = new UserDTO(1L, "User1");

    // Mocks
    when(userService.getUser(1L)).thenReturn(userDTO);

    // When & Then
    mockMvc.perform(get("/users/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.name").value("User1"))
        .andExpect(jsonPath("$.links[0].href", endsWith("/users/1")));
  }

  @Test
  public void testGetUserRevisions() throws Exception {
    // Given
    Long userId = 1L;
    User user1 = new User();
    user1.setName("user name");
    user1.setId(userId);

    // Mocks
    when(auditReaderService.getReader()).thenReturn(auditReader);
    when(auditReader.createQuery()).thenReturn(auditQueryCreator);
    when(auditQueryCreator.forRevisionsOfEntity(User.class, true, true))
        .thenReturn(auditQuery);
    when(auditReader.getRevisions(User.class, userId)).thenReturn(List.of(1));
    when(auditReader.find(User.class, userId, 1)).thenReturn(user1);

    // When & Then
    mockMvc.perform(get("/users/" + userId + "/revisions"))
        .andExpect(status().isOk())
        .andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("User1")));
  }
}
