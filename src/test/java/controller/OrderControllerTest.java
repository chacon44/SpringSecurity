package controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import com.epam.esm.controller.OrderController;
import com.epam.esm.dto.CertificateDTO;
import com.epam.esm.dto.OrderRequestDTO;
import com.epam.esm.dto.OrderResponseDTO;
import com.epam.esm.dto.UserDTO;
import com.epam.esm.model.GiftCertificate;
import com.epam.esm.model.Order;
import com.epam.esm.model.User;
import com.epam.esm.service.AuditReaderService;
import com.epam.esm.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
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
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@ExtendWith(MockitoExtension.class)
public class OrderControllerTest {

  @InjectMocks
  private OrderController orderController;

  @Mock
  private OrderService orderService;

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
    this.mockMvc = standaloneSetup(orderController).build();
  }

  public static String asJsonString(final Object obj) {
    try {
      return new ObjectMapper().writeValueAsString(obj);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  @Test
  public void testPurchaseGiftCertificate() throws Exception {
    // Given
    OrderRequestDTO orderRequestDTO = new OrderRequestDTO(1L, 1L);

    UserDTO userDTO = new UserDTO(1L, "User1");
    CertificateDTO certificateDTO = new CertificateDTO(1L, "Certificate1", "Description1", 100.0, 5L, List.of(1L, 2L));
    LocalDateTime purchaseDate = LocalDateTime.now();

    OrderResponseDTO orderResponseDTO = new OrderResponseDTO(1L, userDTO, certificateDTO, 100.0, purchaseDate);

    // Mocks
    when(orderService.purchaseGiftCertificate(anyLong(), anyLong())).thenReturn(orderResponseDTO);

    // When & Then
    mockMvc.perform(post("/api/order")
            .contentType(MediaType.APPLICATION_JSON)
            .content(asJsonString(orderRequestDTO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.orderId").value(1L))
        .andExpect(jsonPath("$.user.id").value(1L))
        .andExpect(jsonPath("$.user.name").value("User1"))
        .andExpect(jsonPath("$.certificate.certificateId").value(1L))
        .andExpect(jsonPath("$.certificate.name").value("Certificate1"))
        .andExpect(jsonPath("$.purchaseCost").value(100.0));
  }

  @Test
  public void testGetOrder() throws Exception {
    // Given
    UserDTO user = new UserDTO(1L, "User1");
    CertificateDTO certificate = new CertificateDTO(1L, "Certificate1", "Description1", 100.0, 5L, List.of(1L, 2L));
    OrderResponseDTO orderResponseDTO = new OrderResponseDTO(1L, user, certificate, 100.0, LocalDateTime.now());

    // Mocks
    when(orderService.getOrder(1L)).thenReturn(orderResponseDTO);

    // When & Then
    mockMvc.perform(get("/api/order/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.orderId").value(1L))
        .andExpect(jsonPath("$.user.id").value(1L))
        .andExpect(jsonPath("$.user.name").value("User1"))
        .andExpect(jsonPath("$.certificate.certificateId").value(1L))
        .andExpect(jsonPath("$.certificate.name").value("Certificate1"))
        .andExpect(jsonPath("$.purchaseCost").value(100.0));
  }

  @Test
  public void testGetAllOrders() throws Exception {
    // Given
    UserDTO user = new UserDTO(1L, "User1");
    CertificateDTO certificate
        = new CertificateDTO(1L, "Certificate1", "Description", 100.0d, 5L, List.of(1L, 2L));
    OrderResponseDTO order = new OrderResponseDTO(
        1L, user, certificate, 100.0d, LocalDateTime.now());

    List<OrderResponseDTO> orders = List.of(order);
    Page<OrderResponseDTO> orderDTOPage
        = new PageImpl<>(orders);

    // Mocks
    when(orderService.getAllOrders(any(Pageable.class)))
        .thenReturn(orderDTOPage);

    // When & Then
    mockMvc.perform(get("/api/order"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].orderId").value(1L))
        .andExpect(jsonPath("$.content[0].user.id").value(1L))
        .andExpect(jsonPath("$.content[0].certificate.certificateId").value(1L));
  }

  @Test
  public void testGetOrdersFromUser() throws Exception {

    Long userId = 1L;
    UserDTO userDTO = new UserDTO(userId, "User");
    CertificateDTO certificateDTO
        = new CertificateDTO(1L, "Certificate", "Description", 20.50, 5L, List.of(1L, 2L));
    OrderResponseDTO orderResponseDTO
        = new OrderResponseDTO(1L, userDTO, certificateDTO, 20.50, LocalDateTime.now());

    List<OrderResponseDTO> orders = List.of(orderResponseDTO);
    Page<OrderResponseDTO> orderDTOPage = new PageImpl<>(orders);

    when(orderService.getOrdersByUserId(any(Long.class), any(Pageable.class)))
        .thenReturn(orderDTOPage);

    mockMvc.perform(MockMvcRequestBuilders.get("/api/order/users/" + userId).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("User")))
        .andExpect(content().string(containsString("Certificate")))
        .andExpect(content().string(containsString("Description")));
  }
  @Test
  public void testGetOrderRevisions() throws Exception {
    // Given
    Long orderId = 1L;

    User user = new User();
    user.setId(1L);
    user.setName("User1");


    GiftCertificate giftCertificate = new GiftCertificate();
    giftCertificate.setId(1L);
    giftCertificate.setName("Certificate1");
    giftCertificate.setDescription("Description");
    giftCertificate.setPrice(20.50);
    giftCertificate.setDuration(20L);

    Order order1 = new Order();
    order1.setId(orderId);
    order1.setUser(user);
    order1.setCertificate(giftCertificate);
    order1.setPrice(20.50);
    order1.setCreateDate(LocalDateTime.now().toString());

    when(auditReaderService.getReader()).thenReturn(auditReader);
    when(auditReader.createQuery()).thenReturn(auditQueryCreator);
    when(auditQueryCreator.forRevisionsOfEntity(Order.class, true, true))
        .thenReturn(auditQuery);
    when(auditReader.getRevisions(Order.class, orderId)).thenReturn(List.of(1));
    when(auditReader.find(Order.class, orderId, 1)).thenReturn(order1);


    // When & Then
    mockMvc.perform(get("/api/order/" + orderId + "/revisions")
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(orderId))
        .andExpect(jsonPath("$[0].price").value(order1.getPrice()))
        .andExpect(jsonPath("$[0].createDate").value(order1.getCreateDate()));
  }
}
