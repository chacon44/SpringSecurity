package com.epam.esm.service;


import com.epam.esm.dto.CertificateDTO;
import com.epam.esm.dto.OrderResponseDTO;
import com.epam.esm.dto.UserDTO;
import com.epam.esm.exceptions.CustomizedException;
import com.epam.esm.exceptions.ErrorCode;
import com.epam.esm.model.GiftCertificate;
import com.epam.esm.model.Order;
import com.epam.esm.model.Tag;
import com.epam.esm.model.User;
import com.epam.esm.repository.CertificateRepository;
import com.epam.esm.repository.OrderRepository;
import com.epam.esm.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

  private final OrderRepository orderRepository;
  private final UserRepository userRepository;
  private final CertificateRepository certificateRepository;

  public OrderService(OrderRepository orderRepository, UserRepository userRepository, CertificateRepository certificateRepository) {
    this.orderRepository = orderRepository;
    this.userRepository = userRepository;
    this.certificateRepository = certificateRepository;
  }

  /**
   * Retrieves a page with all orders available in the system.
   * Each order is represented as an OrderResponseDTO.
   * @param pageable Specifies the details of the page of orders to be retrieved such as the page number and size.
   * @return A page of OrderResponseDTO objects ordered as per the sort criteria specified in pageable.
   * @throws CustomizedException if there is a database access error.
   */
  public Page<OrderResponseDTO> getAllOrders(Pageable pageable) {
    Page<Order> ordersPage = handleDBError(() -> orderRepository.findAll(pageable),
        "Database error while getting all orders");
    return ordersPage.map(this::convertToOrderDTO);
  }

  /**
   * Retrieves a page of orders specific to a user in the system.
   * Each order is represented as an OrderResponseDTO.
   * @param userId Specifies the ID of the user whose orders are to be retrieved.
   * @param pageable Specifies the details of the page of orders to be retrieved such as the page number and size.
   * @return A page of OrderResponseDTO objects ordered as per the sort criteria specified in pageable.
   * @throws CustomizedException if there is a database access error or error in converting order to OrderDTO.
   */
  public Page<OrderResponseDTO> getOrdersByUserId(Long userId, Pageable pageable) {
    Page<Order> ordersPage = handleDBError(() -> orderRepository.findAllByUserId(userId, pageable),
        "Database error while retrieving orders for user id:" + userId);

    try {
      return ordersPage.map(this::convertToOrderDTO);
    } catch (Exception ex) {
      throw new CustomizedException("Error while converting order to OrderDTO",
          ErrorCode.ORDER_CONVERSION_ERROR, ex);
    }
  }

  /**
   * Retrieves a specific order in the system represented as an OrderResponseDTO.
   * @param id Specifies the ID of the order to be retrieved.
   * @return An OrderResponseDTO object representing the order.
   * @throws CustomizedException if there is a database access error,
   * order with id is not found or error in converting order to OrderDTO.
   */
  public OrderResponseDTO getOrder(Long id) {
    Optional<Order> optionalOrder = handleDBError(() -> orderRepository.findById(id),
        "Problem with database access");

    if (optionalOrder.isPresent()) {
      Order order = optionalOrder.get();
      try {
        return convertToOrderDTO(order);
      } catch (Exception ex) {
        throw new CustomizedException("Error while converting order to OrderDTO",
            ErrorCode.ORDER_CONVERSION_ERROR, ex);
      }

    } else {
      throw new CustomizedException("Order with id " + id + " not found", ErrorCode.ORDER_NOT_FOUND);
    }
  }
  /**
   * Facilitates the purchase of a gift certificate for a user.
   * @param userId Specifies the ID of the user purchasing the certificate.
   * @param certificateId Specifies the ID of the certificate to be purchased.
   * @return An OrderResponseDTO object containing the details of the purchase.
   * @throws CustomizedException if a user with specified userId or certificate with specified certificateId is not found.
   */
  @Transactional
  public OrderResponseDTO purchaseGiftCertificate(Long userId, Long certificateId) {
    User user = getUser(userId);
    GiftCertificate certificate = getCertificate(certificateId);

    Order order = new Order(user, certificate, certificate.getPrice(), LocalDateTime.now());
    Order savedOrder = orderRepository.save(order);

    return convertToOrderDTO(savedOrder);
  }


  private User getUser(Long userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> new CustomizedException("User with id " + userId + " not found.", ErrorCode.USER_NOT_FOUND));
  }

  private GiftCertificate getCertificate(Long certificateId) {
    return certificateRepository.findById(certificateId)
        .orElseThrow(() -> new CustomizedException("Certificate with ID " + certificateId + " not found.", ErrorCode.CERTIFICATE_NOT_FOUND));
  }


  private UserDTO mapToUserDTO(User user) {
    return new UserDTO(user.getId(), user.getName());
  }

  private CertificateDTO mapToCertificateDTO(GiftCertificate certificate) {
    List<Long> tagIds = certificate.getTags().stream().map(Tag::getId).collect(Collectors.toList());
    return new CertificateDTO(certificate.getId(), certificate.getName(),
        certificate.getDescription(), certificate.getPrice(),
        certificate.getDuration(), tagIds);
  }

  private OrderResponseDTO convertToOrderDTO(Order order) {
    try {
      UserDTO userDTO = mapToUserDTO(order.getUser());

    CertificateDTO certificateDTO = mapToCertificateDTO(order.getCertificate());

    return new OrderResponseDTO(order.getId(), userDTO, certificateDTO,
        order.getPrice(), order.getPurchaseTime());
  }catch (NullPointerException ex) {
    throw new CustomizedException("Error while converting order to OrderDTO", ErrorCode.ORDER_CONVERSION_ERROR, ex);
  }
  }

  private <T> T handleDBError(Callable<T> dbOperation, String errorMessage) {
    try {
      return dbOperation.call();
    } catch (DataAccessException ex) {
      throw new CustomizedException(errorMessage, ErrorCode.ORDER_DATABASE_ERROR, ex);
    } catch (Exception ex) {
      throw new CustomizedException("Unexpected error occurred", ErrorCode.ORDER_INTERNAL_SERVER_ERROR, ex);
    }
  }
}
