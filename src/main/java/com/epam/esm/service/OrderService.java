package com.epam.esm.service;


import com.epam.esm.Dto.CertificateDTO;
import com.epam.esm.Dto.OrderDTO;
import com.epam.esm.Dto.UserDTO;
import com.epam.esm.model.GiftCertificate;
import com.epam.esm.model.Order;
import com.epam.esm.model.User;
import com.epam.esm.model.Tag;
import com.epam.esm.repository.OrderRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

  private final OrderRepository orderRepository;

  public OrderService(OrderRepository orderRepository) {
    this.orderRepository = orderRepository;
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

  public OrderDTO getOrder(Long id) {
    Order order = orderRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("Order with ID " + id + " not found."));
    return new OrderDTO(
        order.getId(),
        mapToUserDTO(order.getUser()),
        mapToCertificateDTO(order.getCertificate()),
        order.getPrice(),
        order.getPurchaseTime()
    );
  }

  public OrderDTO save(Order order) {
    Order savedOrder = orderRepository.save(order);
    return new OrderDTO(
        savedOrder.getId(),
        mapToUserDTO(savedOrder.getUser()),
        mapToCertificateDTO(savedOrder.getCertificate()),
        savedOrder.getPrice(),
        savedOrder.getPurchaseTime()
    );
  }

  private UserDTO mapToUserDTO(User user) {
    return new UserDTO(user.getId(), user.getName()); // depending on what fields UserDTO has
  }

  private CertificateDTO mapToCertificateDTO(GiftCertificate certificate) {
    List<Long> tagIds = certificate.getTags().stream().map(Tag::getId).collect(Collectors.toList());
    return new CertificateDTO(certificate.getId(), certificate.getName(), certificate.getDescription(), certificate.getPrice(), certificate.getDuration(), tagIds);
  }
}
