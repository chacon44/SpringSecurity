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
   * Fetches all orders from the repository and maps them to OrderResponseDTO objects.
   *
   * @param pageable The Pageable object containing the page number, size and sort.
   * @return A page of OrderResponseDTO objects.
   */
  public Page<OrderResponseDTO> getAllOrders(Pageable pageable) {
    try {
      Page<Order> ordersPage = orderRepository.findAll(pageable);

      return ordersPage.map(order -> {
        try {
          return new OrderResponseDTO(
              order.getId(),
              mapToUserDTO(order.getUser()),
              mapToCertificateDTO(order.getCertificate()),
              order.getPrice(),
              order.getPurchaseTime()
          );
        } catch (Exception ex) {
          throw new CustomizedException("Error while converting order to OrderDTO", ErrorCode.ORDER_CONVERSION_ERROR, ex);
        }
      });
    } catch (DataAccessException ex) {
      throw new CustomizedException("Database error while getting all orders", ErrorCode.ORDER_DATABASE_ERROR, ex);
    }
  }

  /**
   * Fetches all orders by user ID from the repository and maps them to OrderResponseDTO objects.
   *
   * @param userId The ID of the user whose orders are to be retrieved.
   * @param pageable The Pageable object containing the page number, size and sort.
   * @return A page of OrderResponseDTO objects.
   */
  public Page<OrderResponseDTO> getOrdersByUserId(Long userId, Pageable pageable) {
    try {
      Page<Order> ordersPage = orderRepository.findAllByUserId(userId, pageable);
      try {
        return ordersPage.map(this::convertToOrderDTO);
      } catch (Exception ex) {
        throw new CustomizedException("Error while converting order to OrderDTO",
            ErrorCode.ORDER_CONVERSION_ERROR, ex);
      }
    } catch (DataAccessException ex) {
      throw new CustomizedException("Database error while retrieving orders for user id:" + userId, ErrorCode.ORDER_DATABASE_ERROR, ex);
    }
  }

  /**
   * Fetches an order by its ID from the repository and maps it to an OrderResponseDTO object.
   *
   * @param id The ID of the order to be retrieved.
   * @return The OrderResponseDTO object.
   */
  public OrderResponseDTO getOrder(Long id) {
    try {
      Optional<Order> optionalOrder = orderRepository.findById(id);

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
    } catch (DataAccessException ex) {
      throw new CustomizedException("Problem with database access", ErrorCode.ORDER_DATABASE_ERROR, ex);
    }
  }

  /**
   * Finds a User by the ID in the repository.
   *
   * @param userId The ID of the User to be retrieved.
   * @return The User object.
   * @throws CustomizedException if the User is not found.
   */
  @Transactional
  private User getUser(Long userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> new CustomizedException("User with id " + userId + " not found.", ErrorCode.USER_NOT_FOUND));
  }

  /**
   * Finds a certificate by the ID in the repository.
   *
   * @param certificateId The ID of the certificate to be retrieved.
   * @return The certificate object.
   * @throws CustomizedException if the certificate is not found.
   */
  private GiftCertificate getCertificate(Long certificateId) {
    return certificateRepository.findById(certificateId)
        .orElseThrow(() -> new CustomizedException("Certificate with ID " + certificateId + " not found.", ErrorCode.CERTIFICATE_NOT_FOUND));
  }

  /**
   * Purchases a certificate for a User.
   *
   * @param userId The ID of the user purchasing the certificate.
   * @param certificateId The ID of the certificate to be purchased.
   * @return The OrderResponseDTO containing the details of the purchase.
   */
  @Transactional
  public OrderResponseDTO purchaseGiftCertificate(Long userId, Long certificateId) {

    User user = getUser(userId);
    GiftCertificate certificate = getCertificate(certificateId);

    UserDTO userDTO = new UserDTO(user.getId(), user.getName());
    List<Long> tagIds = certificate.getTags().stream().map(Tag::getId).collect(Collectors.toList());
    CertificateDTO certificateDTO = new CertificateDTO(certificate.getId(), certificate.getName(),
        certificate.getDescription(), certificate.getPrice(), certificate.getDuration(), tagIds);

    Order order = new Order(user, certificate, certificate.getPrice(), LocalDateTime.now());
    Order savedOrder = orderRepository.save(order);

    return new OrderResponseDTO(savedOrder.getId(), userDTO, certificateDTO, savedOrder.getPrice(),
        savedOrder.getPurchaseTime());
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
    UserDTO userDTO = mapToUserDTO(order.getUser());
    CertificateDTO certificateDTO = mapToCertificateDTO(order.getCertificate());

    return new OrderResponseDTO(order.getId(), userDTO, certificateDTO,
        order.getPrice(), order.getPurchaseTime());
  }
}
