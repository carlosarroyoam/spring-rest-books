package com.carlosarroyoam.rest.books.user;

import com.carlosarroyoam.rest.books.core.config.OpenApiConfig;
import com.carlosarroyoam.rest.books.user.dto.CreateUserRequestDto;
import com.carlosarroyoam.rest.books.user.dto.UpdateUserRequestDto;
import com.carlosarroyoam.rest.books.user.dto.UserDto;
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
  public ResponseEntity<List<UserDto>> findAll(
      @RequestParam(required = false, defaultValue = "0") Integer page,
      @RequestParam(required = false, defaultValue = "25") Integer size) {
    List<UserDto> users = userService.findAll(page, size);
    return ResponseEntity.ok(users);
  }

  @GetMapping(path = "/{userId}", produces = "application/json")
  @Operation(summary = "Gets a user by its id")
  public ResponseEntity<UserDto> findById(@PathVariable Long userId) {
    UserDto userById = userService.findById(userId);
    return ResponseEntity.ok(userById);
  }

  @PostMapping(consumes = "application/json")
  @Operation(summary = "Creates a new user")
  public ResponseEntity<Void> create(@Valid @RequestBody CreateUserRequestDto requestDto,
      UriComponentsBuilder builder) {
    UserDto createdUser = userService.create(requestDto);
    UriComponents uriComponents = builder.path("/users/{userId}")
        .buildAndExpand(createdUser.getId());
    return ResponseEntity.created(uriComponents.toUri()).build();
  }

  @PutMapping(value = "/{userId}", consumes = "application/json")
  @Operation(summary = "Updates a user by its id")
  public ResponseEntity<Void> update(@PathVariable Long userId,
      @Valid @RequestBody UpdateUserRequestDto requestDto) {
    userService.update(userId, requestDto);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{userId}")
  @Operation(summary = "Deletes a user by its id")
  public ResponseEntity<Void> deleteById(@PathVariable Long userId) {
    userService.deleteById(userId);
    return ResponseEntity.noContent().build();
  }
}
