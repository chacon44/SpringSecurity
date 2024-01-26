package com.epam.esm.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import com.epam.esm.dto.UserDTO;
import com.epam.esm.dto.UserResponseDTO;
import com.epam.esm.model.User;
import com.epam.esm.service.UserService;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

  @Autowired
  private UserService userService;

  @Autowired
  private EntityManager entityManager;
  public UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping
  public ResponseEntity<PagedModel<EntityModel<UserDTO>>> getAllUsers(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "id") String sort,
      PagedResourcesAssembler<UserDTO> assembler) {

    Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
    Page<UserDTO> userDTOPage = userService.getAllUsers(pageable);

    Link selfLink = linkTo(methodOn(UserController.class)
        .getAllUsers(page, size, sort, assembler)).withSelfRel();

    return ResponseEntity.ok(assembler.toModel(userDTOPage, selfLink));
  }

  @GetMapping("/{id}")
  public ResponseEntity<EntityModel<UserResponseDTO>> getUser(@PathVariable Long id) {
    UserResponseDTO user = userService.getUser(id);
    EntityModel<UserResponseDTO> resource = EntityModel.of(user);
    resource.add(linkTo(UserController.class).slash(user.id()).withSelfRel());
    return ResponseEntity.ok(resource);
  }

  @GetMapping("/{id}/revisions")
  public ResponseEntity getUserRevisions(@PathVariable long id) {
    AuditReader reader = AuditReaderFactory.get(entityManager);
    AuditQuery query = reader.createQuery().forRevisionsOfEntity(User.class, true, true);
    query.addOrder(AuditEntity.revisionNumber().desc());
    List<User> resultList = new ArrayList<>();

    List <Number> revisionNumbers = reader.getRevisions(User.class, id);
    for (Number rev : revisionNumbers) {
      User auditedCertificate = reader.find(User.class, id, rev);
      resultList.add(auditedCertificate);
    }

    return ResponseEntity.ok(resultList);
  }
}
