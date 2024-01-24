package com.epam.esm.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import com.epam.esm.dto.OrderResponseDTO;
import com.epam.esm.dto.OrderRequestDTO;
import com.epam.esm.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/order")
public class OrderController {

  private final OrderService orderService;

  public OrderController(OrderService orderService) {
    this.orderService = orderService;
  }

  @GetMapping
  public ResponseEntity<PagedModel<EntityModel<OrderResponseDTO>>> getAllOrders(Pageable pageable, PagedResourcesAssembler<OrderResponseDTO> assembler) {
    Page<OrderResponseDTO> ordersPage = orderService.getAllOrders(pageable);
    return ResponseEntity.ok(assembler.toModel(ordersPage,
        orderResponseDTO -> EntityModel.of(orderResponseDTO,
            linkTo(methodOn(OrderController.class).getOrder(orderResponseDTO.orderId())).withSelfRel())));
  }

  @GetMapping("/{id}")
  public ResponseEntity<EntityModel<OrderResponseDTO>> getOrder(@PathVariable Long id) {
    OrderResponseDTO orderResponseDTO = orderService.getOrder(id);
    EntityModel<OrderResponseDTO> resource = EntityModel.of(orderResponseDTO);
    resource.add(linkTo(methodOn(OrderController.class).getOrder(id)).withSelfRel());
    return ResponseEntity.ok(resource);
  }

  @GetMapping(value = "/users/{userId}/orders", consumes = {"application/json"}, produces = {"application/json"})
  public ResponseEntity<PagedModel<EntityModel<OrderResponseDTO>>> getOrdersFromUser(
      @PathVariable Long userId,
      Pageable pageable,
      PagedResourcesAssembler<OrderResponseDTO> assembler) {

    Page<OrderResponseDTO> ordersPage = orderService.getOrdersByUserId(userId, pageable);
    return ResponseEntity.ok(assembler.toModel(ordersPage,
        orderResponseDTO -> EntityModel.of(orderResponseDTO,
            linkTo(methodOn(OrderController.class).getOrder(orderResponseDTO.orderId())).withSelfRel())));
  }

  @PostMapping
  public ResponseEntity<EntityModel<OrderResponseDTO>> purchaseGiftCertificate(@RequestBody OrderRequestDTO orderRequestDto) {
    OrderResponseDTO orderResponseDTO = orderService.purchaseGiftCertificate(orderRequestDto.userId(), orderRequestDto.certificateId());
    EntityModel<OrderResponseDTO> resource = EntityModel.of(orderResponseDTO);
    resource.add(linkTo(methodOn(OrderController.class).getOrder(orderResponseDTO.orderId())).withSelfRel());
    return ResponseEntity.ok(resource);
  }

}
