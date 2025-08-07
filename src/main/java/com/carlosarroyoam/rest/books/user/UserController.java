package com.carlosarroyoam.rest.books.user;

import com.carlosarroyoam.rest.books.user.dto.CreateUserRequestDto;
import com.carlosarroyoam.rest.books.user.dto.UpdateUserRequestDto;
import com.carlosarroyoam.rest.books.user.dto.UserDto;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/users")
public class UserController {
  private final UserService userService;

  public UserController(final UserService userService) {
    this.userService = userService;
  }

  @GetMapping(produces = "application/json")
  public ResponseEntity<List<UserDto>> findAll(
      @PageableDefault(page = 0, size = 25) Pageable pageable) {
    List<UserDto> users = userService.findAll(pageable);
    return ResponseEntity.ok(users);
  }

  @GetMapping(path = "/{userId}", produces = "application/json")
  public ResponseEntity<UserDto> findById(@PathVariable Long userId) {
    UserDto userById = userService.findById(userId);
    return ResponseEntity.ok(userById);
  }

  @PostMapping(consumes = "application/json")
  public ResponseEntity<Void> create(@Valid @RequestBody CreateUserRequestDto requestDto,
      UriComponentsBuilder builder) {
    UserDto createdUser = userService.create(requestDto);
    UriComponents uriComponents = builder.path("/users/{userId}")
        .buildAndExpand(createdUser.getId());
    return ResponseEntity.created(uriComponents.toUri()).build();
  }

  @PutMapping(value = "/{userId}", consumes = "application/json")
  public ResponseEntity<Void> update(@PathVariable Long userId,
      @Valid @RequestBody UpdateUserRequestDto requestDto) {
    userService.update(userId, requestDto);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{userId}")
  public ResponseEntity<Void> deleteById(@PathVariable Long userId) {
    userService.deleteById(userId);
    return ResponseEntity.noContent().build();
  }
}
