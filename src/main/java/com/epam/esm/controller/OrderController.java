package com.epam.esm.controller;

import com.epam.esm.Dto.OrderDTO;
import com.epam.esm.Dto.OrderRequestDto;
import com.epam.esm.service.OrderService;
import com.epam.esm.service.UserService;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
public class OrderController {

  private final OrderService orderService;
  private final UserService userService;

  public OrderController(OrderService orderService, UserService userService) {
    this.orderService = orderService;
    this.userService = userService;
  }

  @GetMapping
  public ResponseEntity<List<OrderDTO>> getAllOrders() {
    return ResponseEntity.ok(orderService.getAllOrders());
  }

  @GetMapping("/{id}")
  public ResponseEntity<OrderDTO> getOrder(@PathVariable Long id) {
    try {
      OrderDTO orderDTO = orderService.getOrder(id);
      return new ResponseEntity<>(orderDTO, HttpStatus.OK);
    }
    catch (NoSuchElementException e) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  @PostMapping
  public ResponseEntity<OrderDTO> purchaseGiftCertificate(@RequestBody OrderRequestDto orderRequestDto) {
    OrderDTO savedOrder = userService.purchaseGiftCertificate(orderRequestDto.userId(), orderRequestDto.certificateId());

    return ResponseEntity.status(HttpStatus.CREATED).body(savedOrder);
  }
}
