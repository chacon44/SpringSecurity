package com.epam.esm.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import com.epam.esm.dto.OrderRequestDTO;
import com.epam.esm.dto.OrderResponseDTO;
import com.epam.esm.model.Order;
import com.epam.esm.service.AuditReaderService;
import com.epam.esm.service.OrderService;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.envers.AuditReader;
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
@RequestMapping("/api/order")
public class OrderController {

  @Autowired
  private OrderService orderService;

  @Autowired
  private AuditReaderService auditReaderService;

  public OrderController(OrderService orderService, AuditReaderService auditReaderService) {
    this.orderService = orderService;
    this.auditReaderService = auditReaderService;
  }

  /**
   * Processes the purchase of a Gift Certificate by a User.
   *
   * @param orderRequestDto Contains the ids of the User and the Gift Certificate.
   * @return A ResponseEntity containing the OrderResponseDTO.
   */
  @PostMapping("/admin")
  public ResponseEntity<EntityModel<OrderResponseDTO>> purchaseGiftCertificate(@RequestBody OrderRequestDTO orderRequestDto) {
    OrderResponseDTO OrderResponseDTO = orderService.purchaseGiftCertificate(orderRequestDto.userId(), orderRequestDto.certificateId());
    EntityModel<OrderResponseDTO> resource = EntityModel.of(OrderResponseDTO);
    resource.add(linkTo(methodOn(OrderController.class).getOrder(OrderResponseDTO.orderId())).withSelfRel());
    return ResponseEntity.ok(resource);
  }

  /**
   * Retrieves a pageable and sortable list of all orders.
   *
   * @param page The number of the page to retrieve.
   * @param size The number of records in a page.
   * @param sort The property to sort the results by.
   * @param assembler Helps convert the Page into a PagedModel.
   * @return A ResponseEntity containing a PagedModel of OrderResponseDTO.
   */
  @GetMapping
  public ResponseEntity<PagedModel<EntityModel<OrderResponseDTO>>> getAllOrders(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "id") String sort,
      PagedResourcesAssembler<OrderResponseDTO> assembler) {

    Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
    Page<OrderResponseDTO> ordersPage = orderService.getAllOrders(pageable);

    Link selfLink = linkTo(methodOn(OrderController.class)
        .getAllOrders(page, size, sort, assembler)).withSelfRel();

    return ResponseEntity.ok(assembler.toModel(ordersPage,
        OrderResponseDTO -> EntityModel.of(OrderResponseDTO,
            linkTo(methodOn(OrderController.class).getOrder(OrderResponseDTO.orderId())).withSelfRel()), selfLink));
  }


  /**
   * Fetches an order by its ID.
   *
   * @param id The id of the Order to be retrieved.
   * @return A ResponseEntity containing the OrderResponseDTO.
   */
  @GetMapping("/{id}")
  public ResponseEntity<EntityModel<OrderResponseDTO>> getOrder(@PathVariable Long id) {
    OrderResponseDTO OrderResponseDTO = orderService.getOrder(id);
    EntityModel<OrderResponseDTO> resource = EntityModel.of(OrderResponseDTO);
    resource.add(linkTo(methodOn(OrderController.class).getOrder(id)).withSelfRel());
    return ResponseEntity.ok(resource);
  }

  /**
   * Fetches all orders by User ID.
   *
   * @param userId The id of the User whose orders are to be retrieved.
   * @param page The number of the page to retrieve.
   * @param size The number of records in a page.
   * @param sort The property to sort the results by.
   * @param assembler Helps convert the Page into a PagedModel.
   * @return A ResponseEntity containing a PagedModel of OrderResponseDTO.
   */
  @GetMapping(value = "/users/{userId}", produces = {"application/json"})
  public ResponseEntity<PagedModel<EntityModel<OrderResponseDTO>>> getOrdersFromUser(
      @PathVariable Long userId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "id") String sort,
      PagedResourcesAssembler<OrderResponseDTO> assembler) {

    Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
    Page<OrderResponseDTO> ordersPage = orderService.getOrdersByUserId(userId, pageable);

    Link selfLink = linkTo(methodOn(OrderController.class)
        .getOrdersFromUser(userId, page, size, sort, assembler)).withSelfRel();

    PagedModel<EntityModel<OrderResponseDTO>> pagedModel = assembler.toModel(ordersPage,
        OrderResponseDTO -> EntityModel.of(OrderResponseDTO,
            linkTo(methodOn(OrderController.class).getOrder(OrderResponseDTO.orderId())).withSelfRel()), selfLink);
    return ResponseEntity.ok(pagedModel);
  }



  /**
   * Retrieves the revision history of a specific Order.
   *
   * @param id The id of the Order for which revisions are to be fetched.
   * @return A ResponseEntity containing a list of all Order revisions.
   */
  @GetMapping(value = "/{id}/revisions")
  public ResponseEntity<?> getOrderRevisions(@PathVariable long id) {
    AuditReader reader = auditReaderService.getReader();
    AuditQuery query = reader.createQuery().forRevisionsOfEntity(Order.class, true, true);
    query.addOrder(AuditEntity.revisionNumber().desc());
    List<Order> resultList = new ArrayList<>();

    //return OrderDTO list
    List <Number> revisionNumbers = reader.getRevisions(Order.class, id);
    for (Number rev : revisionNumbers) {
      Order auditedCOrder = reader.find(Order.class, id, rev);
      resultList.add(auditedCOrder);
    }

    //make the conversion here before return
    //map the list to DTO
    return ResponseEntity.ok(resultList);
  }

}
