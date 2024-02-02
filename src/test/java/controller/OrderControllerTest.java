package controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.esm.controller.OrderController;
import com.epam.esm.dto.CertificateDTO;
import com.epam.esm.dto.OrderRequestDTO;
import com.epam.esm.dto.OrderResponseDTO;
import com.epam.esm.dto.UserDTO;
import com.epam.esm.model.Order;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

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
    this.mockMvc = MockMvcBuilders.standaloneSetup(orderController).build();
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
    mockMvc.perform(post("/order")
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
    mockMvc.perform(get("/order/1"))
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
    mockMvc.perform(get("/order"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].orderId").value(1L))
        .andExpect(jsonPath("$.content[0].user.id").value(1L))
        .andExpect(jsonPath("$.content[0].certificate.certificateId").value(1L));
  }

  @Test
  public void testGetOrderRevisions() throws Exception {
    // Given
    Long orderId = 1L;
    UserDTO userDTO = new UserDTO(1L, "User1");
    CertificateDTO certificateDTO = new CertificateDTO(1L, "Certificate1", "Description1", 100.0, 5L, List.of(1L, 2L));
    OrderResponseDTO orderResponseDTO = new OrderResponseDTO(1L, userDTO, certificateDTO, 100.0, LocalDateTime.now());

    Order order1 = new Order();
    order1.setId(orderId);

    when(auditReaderService.getReader()).thenReturn(auditReader);
    when(auditReader.createQuery()).thenReturn(auditQueryCreator);
    when(auditQueryCreator.forRevisionsOfEntity(Order.class, true, true))
        .thenReturn(auditQuery);
    when(auditReader.getRevisions(Order.class, orderId)).thenReturn(List.of(1));
    when(auditReader.find(Order.class, orderId, 1)).thenReturn(order1);

    when(orderService.getOrder(1L)).thenReturn(orderResponseDTO);

    // When & Then
    mockMvc.perform(MockMvcRequestBuilders.get("/order/" + orderId + "/revisions")
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].orderId").value(1L))
        .andExpect(jsonPath("$[0].user.id").value(1L))
        .andExpect(jsonPath("$[0].certificate.certificateId").value(1L));
  }
}
