package com.epam.esm.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

import com.epam.esm.dto.UserDTO;
import com.epam.esm.dto.UserReturnDTO;
import com.epam.esm.service.UserService;
import java.util.Map.Entry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {
  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping
  public ResponseEntity<PagedModel<EntityModel<UserDTO>>> getAllUsers(Pageable pageable, PagedResourcesAssembler<UserDTO> assembler) {
    Page<UserDTO> userDTOPage = userService.getAllUsers(pageable);
    return ResponseEntity.ok(assembler.toModel(userDTOPage));
  }

  @GetMapping("/most-used-tag-of-user-with-highest-cost")
  public ResponseEntity<Entry<String, Long>> getMostUsedTagOfUserWithHighestCost() {
    return userService.getUserMostUsedTag()
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @GetMapping("/{id}")
  public ResponseEntity<EntityModel<UserReturnDTO>> getUser(@PathVariable Long id) {
    UserReturnDTO user = userService.getUser(id);
    EntityModel<UserReturnDTO> resource = EntityModel.of(user);
    resource.add(linkTo(UserController.class).slash(user.id()).withSelfRel());
    return ResponseEntity.ok(resource);
  }
}
