package com.epam.esm.service;


import com.epam.esm.dto.CertificateDTO;
import com.epam.esm.dto.OrderDTO;
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

  public Page<OrderDTO> getAllOrders(Pageable pageable) {
    try {
      Page<Order> ordersPage = orderRepository.findAll(pageable);

      return ordersPage.map(order -> {
        try {
          return new OrderDTO(
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
  public Page<OrderDTO> getOrdersByUserId(Long userId, Pageable pageable) {
    try {
      Page<Order> ordersPage = orderRepository.findAllByUserId(userId, pageable);
      return ordersPage.map(this::convertToOrderDTO);
    } catch (DataAccessException ex) {
      throw new CustomizedException("Database error while retrieving orders for user id:" + userId, ErrorCode.ORDER_DATABASE_ERROR, ex);
    }
  }
  public OrderDTO getOrder(Long id) {
    try {
      Optional<Order> optionalOrder = orderRepository.findById(id);

      if (optionalOrder.isPresent()) {
        Order order = optionalOrder.get();
        return new OrderDTO(
            order.getId(),
            mapToUserDTO(order.getUser()),
            mapToCertificateDTO(order.getCertificate()),
            order.getPrice(),
            order.getPurchaseTime()
        );
      } else {
        throw new CustomizedException("Order with id " + id + " not found", ErrorCode.ORDER_NOT_FOUND);
      }
    } catch (DataAccessException ex) {
      throw new CustomizedException("Problem with database access", ErrorCode.ORDER_DATABASE_ERROR, ex);
    }
  }

  @Transactional
  public OrderDTO purchaseGiftCertificate(Long userId, Long certificateId) {
    Object[] objects = getUserAndCertificateIfExist(userId, certificateId);
    if (objects[1] == null) {
      String notFoundEntity = objects[0] != null ? "Certificate with ID " + certificateId + " not found." : "User with id " + userId + " not found";
      throw new CustomizedException(notFoundEntity, ErrorCode.ORDER_BAD_REQUEST);
    }

    User user = (User) objects[0];
    UserDTO userDTO = new UserDTO(user.getId(), user.getName());

    GiftCertificate certificate = (GiftCertificate) objects[1];
    List<Long> tagIds = certificate.getTags().stream().map(Tag::getId).collect(Collectors.toList());
    CertificateDTO certificateDTO = new CertificateDTO(certificate.getId(), certificate.getName(), certificate.getDescription(),
        certificate.getPrice(), certificate.getDuration(), tagIds);

    Order order = new Order(user, certificate, certificate.getPrice(), LocalDateTime.now());
    Order savedOrder = orderRepository.save(order);

    return new OrderDTO(savedOrder.getId(), userDTO, certificateDTO, savedOrder.getPrice(), savedOrder.getPurchaseTime());
  }

  private Object[] getUserAndCertificateIfExist(Long userId, Long certificateId){
    User user = userRepository.findById(userId).orElse(null);
    if(user == null) return new Object[] {null};

    GiftCertificate certificate = certificateRepository.findById(certificateId).orElse(null);
    if(certificate == null) return new Object[] {null, null};

    return new Object[]{user, certificate};
  }
  private UserDTO mapToUserDTO(User user) {
    return new UserDTO(user.getId(), user.getName());
  }
  private OrderDTO convertToOrderDTO(Order order) {
    User user = order.getUser();
    UserDTO userDTO = new UserDTO(user.getId(), user.getName());

    GiftCertificate certificate = order.getCertificate();

    List<Long> tagIds = certificate.getTags().stream().map(Tag::getId).collect(Collectors.toList());
    CertificateDTO certificateDTO = new CertificateDTO(certificate.getId(),
        certificate.getName(), certificate.getDescription(), certificate.getPrice(),
        certificate.getDuration(), tagIds);

    return new OrderDTO(order.getId(), userDTO, certificateDTO, order.getPrice(), order.getPurchaseTime());
  }
  private CertificateDTO mapToCertificateDTO(GiftCertificate certificate) {
    List<Long> tagIds = certificate.getTags().stream().map(Tag::getId).collect(Collectors.toList());
    return new CertificateDTO(certificate.getId(), certificate.getName(), certificate.getDescription(), certificate.getPrice(), certificate.getDuration(), tagIds);
  }
}
