package com.epam.esm.service;


import com.epam.esm.dto.CertificateDTO;
import com.epam.esm.dto.OrderDTO;
import com.epam.esm.dto.UserDTO;
import com.epam.esm.model.GiftCertificate;
import com.epam.esm.model.Order;
import com.epam.esm.model.User;
import com.epam.esm.model.Tag;
import com.epam.esm.repository.CertificateRepository;
import com.epam.esm.repository.OrderRepository;
import com.epam.esm.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

  public List<OrderDTO> getAllOrders() {
    List<Order> orders = orderRepository.findAll();
    List<OrderDTO> orderDTOs = new ArrayList<>();
    for (Order order : orders) {
      OrderDTO orderDTO = new OrderDTO(
          order.getId(),
          mapToUserDTO(order.getUser()),
          mapToCertificateDTO(order.getCertificate()),
          order.getPrice(),
          order.getPurchaseTime()
      );
      orderDTOs.add(orderDTO);
    }
    return orderDTOs;
  }

  public ResponseEntity<OrderDTO> getOrder(Long id) {

    Optional<Order> optionalOrder = orderRepository.findById(id);

    if(optionalOrder.isPresent()) {
      Order order = optionalOrder.get();
      OrderDTO returnOrder = new OrderDTO(
          order.getId(),
          mapToUserDTO(order.getUser()),
          mapToCertificateDTO(order.getCertificate()),
          order.getPrice(),
          order.getPurchaseTime()
      );
      return new ResponseEntity<>(returnOrder, HttpStatus.OK);
    }
    else return new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  @Transactional
  public ResponseEntity<?> purchaseGiftCertificate(Long userId, Long certificateId) {
    Object[] objects = getUserAndCertificateIfExist(userId, certificateId);
    if (objects[1] == null) {
      String notFoundEntity = objects[0] != null ? "Certificate with ID " + certificateId + " not found." : "User with id " + userId + " not found";
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(notFoundEntity);
    }

    User user = (User) objects[0];
    UserDTO userDTO = new UserDTO(user.getId(), user.getName());

    GiftCertificate certificate = (GiftCertificate) objects[1];
    List<Long> tagIds = certificate.getTags().stream().map(Tag::getId).collect(Collectors.toList());
    CertificateDTO certificateDTO = new CertificateDTO(certificate.getId(), certificate.getName(), certificate.getDescription(),
        certificate.getPrice(), certificate.getDuration(), tagIds);

    Order order = new Order(user, certificate, certificate.getPrice(), LocalDateTime.now());
    Order savedOrder = orderRepository.save(order);

    OrderDTO returnOrder = new OrderDTO(savedOrder.getId(), userDTO, certificateDTO, savedOrder.getPrice(), savedOrder.getPurchaseTime());

    return ResponseEntity.status(HttpStatus.CREATED).body(returnOrder);
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

  private CertificateDTO mapToCertificateDTO(GiftCertificate certificate) {
    List<Long> tagIds = certificate.getTags().stream().map(Tag::getId).collect(Collectors.toList());
    return new CertificateDTO(certificate.getId(), certificate.getName(), certificate.getDescription(), certificate.getPrice(), certificate.getDuration(), tagIds);
  }


}
