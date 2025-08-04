package com.carlosarroyoam.rest.books.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;

import com.carlosarroyoam.rest.books.core.constant.AppMessages;
import com.carlosarroyoam.rest.books.user.dto.CreateUserRequestDto;
import com.carlosarroyoam.rest.books.user.dto.UpdateUserRequestDto;
import com.carlosarroyoam.rest.books.user.dto.UserDto;
import com.carlosarroyoam.rest.books.user.entity.Role;
import com.carlosarroyoam.rest.books.user.entity.User;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private UserService userService;

  @Test
  @DisplayName("Should return List<UserDto> when find all users")
  void shouldReturnListOfUsers() {
    List<User> users = List.of(User.builder().build(), User.builder().build());

    Page<User> pagedUsers = new PageImpl<>(users);

    Mockito.when(userRepository.findAll(any(Pageable.class))).thenReturn(pagedUsers);

    List<UserDto> usersDto = userService.findAll(0, 25);

    assertThat(usersDto).isNotNull().isNotEmpty().size().isEqualTo(2);
  }

  @Test
  @DisplayName("Should return UserDto when find user by id with existing id")
  void shouldReturnWhenFindUserByIdWithExistingId() {
    User user = User.builder().id(1L).build();

    Mockito.when(userRepository.findById(any())).thenReturn(Optional.of(user));

    UserDto userDto = userService.findById(1L);

    assertThat(userDto).isNotNull();
    assertThat(userDto.getId()).isEqualTo(1L);
  }

  @Test
  @DisplayName("Should throw ResponseStatusException when find a user by id with non existing id")
  void shouldThrowWhenFindUserByIdWithNonExistingId() {
    Mockito.when(userRepository.findById(any())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.findById(1L)).isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining(HttpStatus.NOT_FOUND.toString())
        .hasMessageContaining(AppMessages.USER_NOT_FOUND_EXCEPTION);
  }

  @Test
  @DisplayName("Should return UserDto when create a user with valid data")
  void shouldReturnWhenCreateUserWithValidData() {
    CreateUserRequestDto requestDto = CreateUserRequestDto.builder()
        .username("carroyom")
        .email("carroyom@mail.com")
        .build();

    User user = User.builder().username("carroyom").email("carroyom@mail.com").build();

    Mockito.when(userRepository.existsByUsername(any())).thenReturn(false);
    Mockito.when(userRepository.existsByEmail(any())).thenReturn(false);
    Mockito.when(userRepository.save(any(User.class))).thenReturn(user);

    UserDto userDto = userService.create(requestDto);

    assertThat(userDto).isNotNull();
    assertThat(userDto.getUsername()).isEqualTo("carroyom");
    assertThat(userDto.getEmail()).isEqualTo("carroyom@mail.com");
  }

  @Test
  @DisplayName("Should throw ResponseStatusException when create a user with existing username")
  void shouldThrowWhenCreateUserWithExistingUsername() {
    CreateUserRequestDto requestDto = CreateUserRequestDto.builder().build();

    Mockito.when(userRepository.existsByUsername(any())).thenReturn(true);

    assertThatThrownBy(() -> userService.create(requestDto))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining(HttpStatus.BAD_REQUEST.toString())
        .hasMessageContaining(AppMessages.USERNAME_ALREADY_EXISTS_EXCEPTION);
  }

  @Test
  @DisplayName("Should throw ResponseStatusException when create a user with existing email")
  void shouldThrowWhenCreateUserWithExistingEmail() {
    CreateUserRequestDto requestDto = CreateUserRequestDto.builder().build();

    Mockito.when(userRepository.existsByEmail(any())).thenReturn(true);

    assertThatThrownBy(() -> userService.create(requestDto))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining(HttpStatus.BAD_REQUEST.toString())
        .hasMessageContaining(AppMessages.EMAIL_ALREADY_EXISTS_EXCEPTION);
  }

  @Test
  @DisplayName("Should update user with valid data")
  void shouldUpdateUserWithValidData() {
    UpdateUserRequestDto requestDto = UpdateUserRequestDto.builder()
        .name("Carlos Alberto Arroyo Martínez")
        .age(Byte.valueOf("29"))
        .build();

    User user = User.builder().id(1L).name("Carlos Arroyo").age(Byte.valueOf("28")).build();

    Mockito.when(userRepository.findById(any())).thenReturn(Optional.of(user));
    Mockito.when(userRepository.save(any(User.class))).thenReturn(user);

    userService.update(1L, requestDto);

    Mockito.verify(userRepository).save(user);
    assertThat(user.getName()).isEqualTo("Carlos Alberto Arroyo Martínez");
    assertThat(user.getAge()).isEqualTo(Byte.valueOf("29"));
  }

  @Test
  @DisplayName("Should throw ResponseStatusException when update user with non existing id")
  void shouldThrowWhenUpdateUserWithInvalidData() {
    UpdateUserRequestDto requestDto = UpdateUserRequestDto.builder().build();

    Mockito.when(userRepository.findById(any())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.update(1L, requestDto))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining(HttpStatus.NOT_FOUND.toString())
        .hasMessageContaining(AppMessages.USER_NOT_FOUND_EXCEPTION);
  }

  @Test
  @DisplayName("Should deactivate user with existing id")
  void shouldDeactivateUserWithExistingId() {
    User user = User.builder().id(1L).isActive(true).build();

    Mockito.when(userRepository.findById(any())).thenReturn(Optional.of(user));
    Mockito.when(userRepository.save(any(User.class))).thenReturn(user);

    userService.deleteById(1L);

    Mockito.verify(userRepository).save(user);
    assertThat(user.getIsActive()).isFalse();
  }

  @Test
  @DisplayName("Should throw ResponseStatusException when deactivate user with non existing id")
  void shouldThrowWhenDeactivateUserWithNonExistingId() {
    Mockito.when(userRepository.findById(any())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.deleteById(1L)).isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining(HttpStatus.NOT_FOUND.toString())
        .hasMessageContaining(AppMessages.USER_NOT_FOUND_EXCEPTION);
  }

  @Test
  @DisplayName("Should load user details by username with existing username")
  void shouldLoadUserDetailsByUsernameWithExistingUsername() {
    Role adminRole = Role.builder()
        .title("App//Admin")
        .description("Role for admins users")
        .build();

    User user = User.builder()
        .username("carroyom")
        .isActive(true)
        .role(adminRole)
        .roleId(adminRole.getId())
        .build();

    Mockito.when(userRepository.findByUsername(any())).thenReturn(Optional.of(user));

    org.springframework.security.core.userdetails.User userDetails = (org.springframework.security.core.userdetails.User) userService
        .loadUserByUsername("carroyom");

    assertThat(userDetails).isNotNull();
    assertThat(userDetails.getUsername()).isEqualTo("carroyom");
    assertThat(userDetails.isEnabled()).isTrue();
  }

  @Test
  @DisplayName("Should throw UsernameNotFoundException when load user details by username with non existing username")
  void shouldThrowWhenLoadUserDetailsByUsernameWithNonExistingUsername() {
    Mockito.when(userRepository.findByUsername(any())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.loadUserByUsername("carroyom"))
        .isInstanceOf(UsernameNotFoundException.class)
        .hasMessageContaining("Username not found: carroyom");
  }
}
