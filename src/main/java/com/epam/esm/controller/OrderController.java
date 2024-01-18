package com.epam.esm.controller;

import com.epam.esm.dto.OrderDTO;
import com.epam.esm.dto.OrderRequestDTO;
import com.epam.esm.service.OrderService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/order")
public class OrderController {

  private final OrderService orderService;

  public OrderController(OrderService orderService) {
    this.orderService = orderService;
  }

  @GetMapping
  public ResponseEntity<List<OrderDTO>> getAllOrders() {
    return ResponseEntity.ok(orderService.getAllOrders());
  }

  @GetMapping("/{id}")
  public ResponseEntity<OrderDTO> getOrder(@PathVariable Long id) {
      return ResponseEntity.ok(orderService.getOrder(id));
  }

  @GetMapping(consumes = {"application/json"}, produces = {"application/json"})
  public ResponseEntity<List<OrderDTO>> getOrdersFromUser(
      @RequestParam(required = false) Long userId) {


    return ResponseEntity.ok(orderService.getOrdersByUserId(userId));
  }
  @PostMapping
  public ResponseEntity<?> purchaseGiftCertificate(@RequestBody OrderRequestDTO orderRequestDto) {
    OrderDTO returnDTO = orderService.purchaseGiftCertificate(orderRequestDto.userId(), orderRequestDto.certificateId());
    return ResponseEntity.ok(returnDTO);
  }

}
