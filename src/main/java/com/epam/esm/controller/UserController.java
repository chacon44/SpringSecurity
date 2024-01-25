package com.epam.esm.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import com.epam.esm.dto.UserDTO;
import com.epam.esm.dto.UserResponseDTO;
import com.epam.esm.service.UserService;
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
  private final UserService userService;

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
}
