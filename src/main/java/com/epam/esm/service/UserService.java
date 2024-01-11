package com.epam.esm.service;

import com.epam.esm.Dto.CertificateDTO;
import com.epam.esm.Dto.OrderDTO;
import com.epam.esm.Dto.UserDTO;
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
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class UserService {

  private final UserRepository userRepository;
  private final OrderRepository orderRepository;
  private final CertificateRepository certificateRepository;

  public UserService(UserRepository userRepository, OrderRepository orderRepository, CertificateRepository certificateRepository) {
    this.userRepository = userRepository;
    this.orderRepository = orderRepository;
    this.certificateRepository = certificateRepository;
  }

  public List<UserDTO> getAllUsers() {
    List<User> users = userRepository.findAll();

    List<UserDTO> userDTOs = new ArrayList<>();
    for (User user : users) {
      UserDTO userDTO = new UserDTO(
          user.getId(),
          user.getName() // depending on what map to username
      );

      userDTOs.add(userDTO);
    }

    return userDTOs;
  }

  public UserDTO getUser(Long id) {
    User user = userRepository.findById(id).orElseThrow(NoSuchElementException::new);

    return new UserDTO(
        user.getId(),
        user.getName() // depending on what map to username
    );
  }

  @Transactional
  public OrderDTO purchaseGiftCertificate(Long userId, Long certificateId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new NoSuchElementException("User with ID " + userId + " not found."));
    UserDTO userDTO = new UserDTO(user.getId(), user.getName());

    GiftCertificate certificate = certificateRepository.findById(certificateId)
        .orElseThrow(() -> new NoSuchElementException("Certificate with ID " + certificateId + " not found."));
    List<Long> tagIds = certificate.getTags().stream().map(Tag::getId).collect(Collectors.toList());
    CertificateDTO certificateDTO = new CertificateDTO(certificate.getId(), certificate.getName(), certificate.getDescription(), certificate.getPrice(), certificate.getDuration(), tagIds);

    Order order = new Order(user, certificate, certificate.getPrice(), LocalDateTime.now());
    Order savedOrder = orderRepository.save(order);

    return new OrderDTO(savedOrder.getId(), userDTO, certificateDTO, savedOrder.getPrice(), savedOrder.getPurchaseTime());
  }
}
