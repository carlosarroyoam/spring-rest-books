package com.carlosarroyoam.rest.books.controller;

import com.carlosarroyoam.rest.books.config.OpenApiConfig;
import com.carlosarroyoam.rest.books.dto.ChangePasswordRequest;
import com.carlosarroyoam.rest.books.dto.CreateUserRequest;
import com.carlosarroyoam.rest.books.dto.UpdateUserRequest;
import com.carlosarroyoam.rest.books.dto.UserResponse;
import com.carlosarroyoam.rest.books.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/users")
@Tag(name = "Users", description = "Operations about users")
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
public class UserController {
  private final UserService userService;

  public UserController(final UserService userService) {
    this.userService = userService;
  }

  @GetMapping(produces = "application/json")
  @Operation(summary = "Gets the list of users")
  public ResponseEntity<List<UserResponse>> findAll(
      @RequestParam(required = false, defaultValue = "0") Integer page,
      @RequestParam(required = false, defaultValue = "25") Integer size) {
    List<UserResponse> users = userService.findAll(page, size);
    return ResponseEntity.ok(users);
  }

  @GetMapping(path = "/{userId}", produces = "application/json")
  @Operation(summary = "Gets a user by its id")
  public ResponseEntity<UserResponse> findById(@PathVariable Long userId) {
    UserResponse userById = userService.findById(userId);
    return ResponseEntity.ok(userById);
  }

  @PostMapping(consumes = "application/json")
  @Operation(summary = "Creates a new user")
  public ResponseEntity<Void> create(@Valid @RequestBody CreateUserRequest createUserRequest,
      UriComponentsBuilder builder) {
    UserResponse createdUser = userService.create(createUserRequest);
    UriComponents uriComponents = builder.path("/users/{userId}")
        .buildAndExpand(createdUser.getId());
    return ResponseEntity.created(uriComponents.toUri()).build();
  }

  @PutMapping(value = "/{userId}", consumes = "application/json")
  @Operation(summary = "Updates a user by its id")
  public ResponseEntity<Void> update(@PathVariable Long userId,
      @Valid @RequestBody UpdateUserRequest updateUserRequest) {
    userService.update(userId, updateUserRequest);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{userId}")
  @Operation(summary = "Deletes a user by its id")
  public ResponseEntity<Void> deleteById(@PathVariable Long userId) {
    userService.deleteById(userId);
    return ResponseEntity.noContent().build();
  }

  @PostMapping(path = "/{userId}/change-password", consumes = "application/json")
  @Operation(summary = "Changes a user password")
  public ResponseEntity<UserResponse> changePassword(@PathVariable Long userId,
      @Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
    userService.changePassword(userId, changePasswordRequest);
    return ResponseEntity.noContent().build();
  }
}
