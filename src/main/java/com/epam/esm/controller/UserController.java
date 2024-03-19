package com.epam.esm.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import com.epam.esm.dto.UserDTO;
import com.epam.esm.model.User;
import com.epam.esm.service.AuditReaderService;
import com.epam.esm.service.UserService;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class UserController {

  private final String USER = "/user";
  private final String ID = "/{id}";
  private final String REVISIONS = "/revisions";

  @Autowired
  private UserService userService;

  @Autowired
  private AuditReaderService auditReaderService;

  public UserController(UserService userService, AuditReaderService auditReaderService) {
    this.userService = userService;
    this.auditReaderService = auditReaderService;
  }

  /**
   * Retrieves a list of all Users, paged according to the provided parameters.
   *
   * @param page The number of the page to retrieve.
   * @param size The number of records in a page.
   * @param sort The property to sort the results by.
   * @param assembler Assembles the paged results into the required format.
   * @return A ResponseEntity containing the paged list of users in the response body.
   */
  @GetMapping(USER)
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

  /**
   * Retrieves the User which correlates with the provided id.
   *
   * @param id The id of the User to retrieve.
   * @return A ResponseEntity containing the UserDTO in the response body.
   */
  @GetMapping(USER + ID)
  public ResponseEntity<EntityModel<UserDTO>> getUser(@PathVariable Long id) {
    UserDTO userDTO = userService.getUser(id);
    EntityModel<UserDTO> resource = EntityModel.of(userDTO);
    resource.add(linkTo(methodOn(UserController.class).getUser(id)).withSelfRel());
    return ResponseEntity.ok(resource);
  }

  /**
   * Fetches all revisions of a User by id.
   *
   * @param id The id of the User for which to fetch revisions.
   * @return A ResponseEntity containing all revisions of the User as a List.
   */
  @GetMapping(USER + ID + REVISIONS)
  public ResponseEntity<?> getUserRevisions(@PathVariable long id) {
    AuditReader reader = auditReaderService.getReader();
    AuditQuery query = reader.createQuery().forRevisionsOfEntity(User.class, true, true);
    query.addOrder(AuditEntity.revisionNumber().desc());
    List<User> resultList = new ArrayList<>();

    List <Number> revisionNumbers = reader.getRevisions(User.class, id);
    for (Number rev : revisionNumbers) {
      User auditedUser = reader.find(User.class, id, rev);
      resultList.add(auditedUser);
    }

    return ResponseEntity.ok(resultList);
  }
}
