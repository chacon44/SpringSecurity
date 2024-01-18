package com.epam.esm.controller;

import com.epam.esm.dto.UserDTO;
import com.epam.esm.dto.UserReturnDTO;
import com.epam.esm.service.UserService;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;

@RestController
@RequestMapping("/users")
public class UserController {
  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping
  public ResponseEntity<List<EntityModel<UserDTO>>> getAllUsers() {
    List<EntityModel<UserDTO>> users = userService.getAllUsers().stream()
        .map(user -> EntityModel.of(user,
            WebMvcLinkBuilder.linkTo(UserController.class).slash(user.id()).withSelfRel(),
            WebMvcLinkBuilder.linkTo(UserController.class).withRel("users")))
        .collect(Collectors.toList());
    return ResponseEntity.ok(users);
  }

  @GetMapping("/{id}")
  public ResponseEntity<EntityModel<UserReturnDTO>> getUser(@PathVariable Long id) {
    UserReturnDTO user = userService.getUser(id);
    EntityModel<UserReturnDTO> resource = EntityModel.of(user);
    resource.add(WebMvcLinkBuilder.linkTo(UserController.class).slash(user.id()).withSelfRel());
    return ResponseEntity.ok(resource);
  }
}
