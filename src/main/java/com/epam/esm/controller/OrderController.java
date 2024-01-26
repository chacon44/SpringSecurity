package com.epam.esm.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import com.epam.esm.dto.OrderDTO;
import com.epam.esm.dto.OrderRequestDTO;
import com.epam.esm.model.Order;
import com.epam.esm.service.OrderService;
import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
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

  @Autowired
  private OrderService orderService;

  @Autowired
  private EntityManager entityManager;

  public OrderController(OrderService orderService) {
    this.orderService = orderService;
  }

  @GetMapping
  public ResponseEntity<PagedModel<EntityModel<OrderDTO>>> getAllOrders(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "id") String sort,
      PagedResourcesAssembler<OrderDTO> assembler) {

    Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
    Page<OrderDTO> ordersPage = orderService.getAllOrders(pageable);

    Link selfLink = linkTo(methodOn(OrderController.class)
        .getAllOrders(page, size, sort, assembler)).withSelfRel();

    return ResponseEntity.ok(assembler.toModel(ordersPage,
        orderDTO -> EntityModel.of(orderDTO,
            linkTo(methodOn(OrderController.class).getOrder(orderDTO.orderId())).withSelfRel()), selfLink));
  }

  @GetMapping("/{id}")
  public ResponseEntity<EntityModel<OrderDTO>> getOrder(@PathVariable Long id) {
    OrderDTO orderDTO = orderService.getOrder(id);
    EntityModel<OrderDTO> resource = EntityModel.of(orderDTO);
    resource.add(linkTo(methodOn(OrderController.class).getOrder(id)).withSelfRel());
    return ResponseEntity.ok(resource);
  }

  @GetMapping(value = "/users/{userId}/orders", consumes = {"application/json"}, produces = {"application/json"})
  public ResponseEntity<PagedModel<EntityModel<OrderDTO>>> getOrdersFromUser(
      @PathVariable Long userId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "id") String sort,
      PagedResourcesAssembler<OrderDTO> assembler) {

    Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
    Page<OrderDTO> ordersPage = orderService.getOrdersByUserId(userId, pageable);

    Link selfLink = linkTo(methodOn(OrderController.class)
        .getOrdersFromUser(userId, page, size, sort, assembler)).withSelfRel();

    return ResponseEntity.ok(assembler.toModel(ordersPage,
        orderDTO -> EntityModel.of(orderDTO,
            linkTo(methodOn(OrderController.class).getOrder(orderDTO.orderId())).withSelfRel()), selfLink));
  }

  @PostMapping
  public ResponseEntity<EntityModel<OrderDTO>> purchaseGiftCertificate(@RequestBody OrderRequestDTO orderRequestDto) {
    OrderDTO orderDTO = orderService.purchaseGiftCertificate(orderRequestDto.userId(), orderRequestDto.certificateId());
    EntityModel<OrderDTO> resource = EntityModel.of(orderDTO);
    resource.add(linkTo(methodOn(OrderController.class).getOrder(orderDTO.orderId())).withSelfRel());
    return ResponseEntity.ok(resource);
  }

  @GetMapping("/{id}/revisions")
  public ResponseEntity getOrderRevisions(@PathVariable long id) {
    AuditReader reader = AuditReaderFactory.get(entityManager);
    AuditQuery query = reader.createQuery().forRevisionsOfEntity(Order.class, true, true);
    query.addOrder(AuditEntity.revisionNumber().desc());
    List<Order> resultList = new ArrayList<>();

    List <Number> revisionNumbers = reader.getRevisions(Order.class, id);
    for (Number rev : revisionNumbers) {
      Order auditedCertificate = reader.find(Order.class, id, rev);
      resultList.add(auditedCertificate);
    }

    return ResponseEntity.ok(resultList);
  }

}
