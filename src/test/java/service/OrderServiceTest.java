package service;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

import com.epam.esm.dto.OrderResponseDTO;
import com.epam.esm.exceptions.CustomizedException;
import com.epam.esm.exceptions.ErrorCode;
import com.epam.esm.model.GiftCertificate;
import com.epam.esm.model.Order;
import com.epam.esm.model.Tag;
import com.epam.esm.model.User;
import com.epam.esm.repository.CertificateRepository;
import com.epam.esm.repository.OrderRepository;
import com.epam.esm.repository.UserRepository;
import com.epam.esm.service.OrderService;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

  public static final Long ORDER_ID = 1L;
  public static final Long USER_ID = 2L;
  public static final Long CERTIFICATE_ID = 3L;

  @InjectMocks
  private OrderService orderService;

  @Mock
  private OrderRepository orderRepository;

  @Mock
  private CertificateRepository certificateRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  User user;

  @Mock
  GiftCertificate certificate;

  @Mock
  List<Tag> tags;

  @Mock
  Order order;

  @Test
  public void testGetAllOrders_ReturnsOrderDTOs() {
    // Given
    given(order.getId()).willReturn(ORDER_ID);
    given(user.getId()).willReturn(USER_ID);
    given(certificate.getId()).willReturn(CERTIFICATE_ID);

    given(certificate.getTags()).willReturn(tags);
    given(order.getUser()).willReturn(user);
    given(order.getCertificate()).willReturn(certificate);

    Page<Order> ordersPage = new PageImpl<>(Collections.singletonList(order));
    Pageable pageable = PageRequest.of(0, 10);
    when(orderRepository.findAll(pageable)).thenReturn(ordersPage);

    // When
    Page<OrderResponseDTO> result = orderService.getAllOrders(pageable);

    // Then
    assertEquals(1, result.getNumberOfElements());
    assertEquals(ORDER_ID, result.getContent().get(0).orderId());
  }

  @Test
  public void testGetAllOrders_ThrowsDataAccessException() {
    // Given
    Pageable pageable = PageRequest.of(0, 10);
    when(orderRepository.findAll(pageable)).thenThrow(new DataAccessException("Test Exception") {});

    // When
    CustomizedException exception = assertThrows(CustomizedException.class, () -> orderService.getAllOrders(pageable));

    // Then
    assertTrue(exception.getMessage().contains("Database error while getting all orders"));
    assertEquals(exception.getCode(), ErrorCode.ORDER_DATABASE_ERROR);
  }

  @Test
  public void testGetAllOrders_ThrowsExceptionOnConversion() {
    // Given
    when(order.getUser()).thenReturn(null);

    Page<Order> ordersPage = new PageImpl<>(singletonList(order));
    Pageable pageable = PageRequest.of(0, 10);
    when(orderRepository.findAll(pageable)).thenReturn(ordersPage);

    // When / Then
    CustomizedException exception = assertThrows(CustomizedException.class, () -> orderService.getAllOrders(pageable));

    // Then
    assertTrue(exception.getMessage().contains("Error while converting order to OrderDTO"));
    assertEquals(exception.getCode(), ErrorCode.ORDER_CONVERSION_ERROR);
  }

  @Test
  public void testGetOrdersByUserId_ReturnsOrderDTOs() {
    // Given
    given(order.getId()).willReturn(ORDER_ID);
    given(user.getId()).willReturn(USER_ID);
    given(certificate.getId()).willReturn(CERTIFICATE_ID);

    given(certificate.getTags()).willReturn(tags);
    given(order.getUser()).willReturn(user);
    given(order.getCertificate()).willReturn(certificate);

    Page<Order> ordersPage = new PageImpl<>(Collections.singletonList(order));
    Pageable pageable = PageRequest.of(0, 10);
    when(orderRepository.findAllByUserId(USER_ID, pageable)).thenReturn(ordersPage);

    // When
    Page<OrderResponseDTO> result = orderService.getOrdersByUserId(USER_ID, pageable);

    // Then
    assertEquals(1, result.getNumberOfElements());
    assertEquals(ORDER_ID, result.getContent().get(0).orderId());
  }

  @Test
  public void testGetOrdersByUserId_ThrowsDataAccessException() {
    // Given
    Pageable pageable = PageRequest.of(0, 10);
    when(orderRepository.findAllByUserId(USER_ID, pageable)).thenThrow(new DataAccessException("Test Exception") {});

    // When
    CustomizedException exception = assertThrows(CustomizedException.class, () -> orderService.getOrdersByUserId(USER_ID, pageable));

    // Then
    assertTrue(exception.getMessage().contains("Database error while retrieving orders for user id:" + USER_ID));
    assertEquals(exception.getCode(), ErrorCode.ORDER_DATABASE_ERROR);
  }

  @Test
  public void testGetOrdersByUserId_ThrowsExceptionOnConversion() {
    // Given
    when(order.getUser()).thenReturn(null);

    Page<Order> ordersPage = new PageImpl<>(singletonList(order));
    Pageable pageable = PageRequest.of(0, 10);
    when(orderRepository.findAllByUserId(USER_ID, pageable)).thenReturn(ordersPage);

    // When / Then
    CustomizedException exception = assertThrows(CustomizedException.class, () -> orderService.getOrdersByUserId(USER_ID, pageable));

    // Then
    assertTrue(exception.getMessage().contains("Error while converting order to OrderDTO"));
    assertEquals(exception.getCode(), ErrorCode.ORDER_CONVERSION_ERROR);
  }

  @Test
  public void testGetOrder_ValidId_ReturnsOrderDTO() {
    // Given
    when(order.getId()).thenReturn(ORDER_ID);
    when(order.getUser()).thenReturn(user);
    when(order.getCertificate()).thenReturn(certificate);

    when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

    // When
    OrderResponseDTO result = orderService.getOrder(ORDER_ID);

    // Then
    assertEquals(ORDER_ID, result.orderId());
  }

  @Test
  public void testGetOrder_InvalidId_ThrowsException() {
    // Given
    when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.empty());

    // When / Then
    CustomizedException exception = assertThrows(CustomizedException.class, () -> orderService.getOrder(ORDER_ID));

    // Then
    assertTrue(exception.getMessage().contains("Order with id " + ORDER_ID + " not found"));
    assertEquals(exception.getCode(), ErrorCode.ORDER_NOT_FOUND);
  }

  @Test
  public void testGetOrder_ConversionError_ThrowsException() {
    // Given
    given(order.getUser()).willReturn(null);
    when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

    // When / Then
    CustomizedException exception = assertThrows(CustomizedException.class, () -> orderService.getOrder(ORDER_ID));

    // Then
    assertTrue(exception.getMessage().contains("Error while converting order to OrderDTO"));
    assertEquals(exception.getCode(), ErrorCode.ORDER_CONVERSION_ERROR);
  }

  @Test
  public void testGetOrder_RepositoryError_ThrowsException() {
    // Given
    when(orderRepository.findById(ORDER_ID)).thenThrow(new DataAccessException("Test Exception") {});

    // When / Then
    CustomizedException exception = assertThrows(CustomizedException.class, () -> orderService.getOrder(ORDER_ID));

    // Then
    assertTrue(exception.getMessage().contains("Problem with database access"));
    assertEquals(exception.getCode(), ErrorCode.ORDER_DATABASE_ERROR);
  }

  @Test
  public void testPurchaseGiftCertificate_ValidInputs_ReturnsOrderDTO() {
    // Given
    when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
    when(certificateRepository.findById(CERTIFICATE_ID)).thenReturn(Optional.of(certificate));
    when(orderRepository.save(any())).thenAnswer(invocation -> {
      Order order = invocation.getArgument(0);
      order.setId(ORDER_ID);
      return order;
    });

    // When
    OrderResponseDTO result = orderService.purchaseGiftCertificate(USER_ID, CERTIFICATE_ID);

    // Then
    assertEquals(ORDER_ID, result.orderId());
  }

  @Test
  public void testPurchaseGiftCertificate_InvalidUserId_ThrowsException() {
    // Given
    when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

    // When / Then
    CustomizedException exception = assertThrows(CustomizedException.class, () -> orderService.purchaseGiftCertificate(USER_ID, CERTIFICATE_ID));

    // Then
    assertTrue(exception.getMessage().contains("User with id " + USER_ID + " not found"));
    assertEquals(exception.getCode(), ErrorCode.USER_NOT_FOUND);
  }

  @Test
  public void testPurchaseGiftCertificate_InvalidCertificateId_ThrowsException() {

    // When / Then
    CustomizedException exception = assertThrows(CustomizedException.class, () -> orderService.purchaseGiftCertificate(USER_ID, CERTIFICATE_ID));

    // Then
    assertTrue(exception.getMessage().contains("User with id " + USER_ID + " not found"));
    assertEquals(exception.getCode(), ErrorCode.USER_NOT_FOUND);
  }
}
