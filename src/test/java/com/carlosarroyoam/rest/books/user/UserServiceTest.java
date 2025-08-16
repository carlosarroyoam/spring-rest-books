package com.carlosarroyoam.rest.books.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.carlosarroyoam.rest.books.core.constant.AppMessages;
import com.carlosarroyoam.rest.books.user.dto.CreateUserRequestDto;
import com.carlosarroyoam.rest.books.user.dto.UpdateUserRequestDto;
import com.carlosarroyoam.rest.books.user.dto.UserDto;
import com.carlosarroyoam.rest.books.user.dto.UserDto.UserDtoMapper;
import com.carlosarroyoam.rest.books.user.dto.UserFilterDto;
import com.carlosarroyoam.rest.books.user.entity.Role;
import com.carlosarroyoam.rest.books.user.entity.User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private UserService userService;

  private User user;

  @BeforeEach
  void setUp() {
    LocalDateTime now = LocalDateTime.now();

    Role role = Role.builder()
        .id(1)
        .title("App//Admin")
        .description("Role for admins users")
        .build();

    user = User.builder()
        .id(1L)
        .name("Carlos Alberto Arroyo Martínez")
        .age(Byte.valueOf("28"))
        .email("carroyom@mail.com")
        .username("carroyom")
        .roleId(role.getId())
        .role(role)
        .isActive(Boolean.TRUE)
        .createdAt(now)
        .updatedAt(now)
        .build();
  }

  @Test
  @DisplayName("Should return List<UserDto> when find all users")
  void shouldReturnListOfUsers() {
    List<User> users = List.of(user);

    when(userRepository.findAll(ArgumentMatchers.<Specification<User>>any(), any(Pageable.class)))
        .thenReturn(new PageImpl<>(users));

    List<UserDto> usersDto = userService.findAll(PageRequest.of(0, 25),
        UserFilterDto.builder().build());

    assertThat(usersDto).isNotNull().isNotEmpty().hasSize(1);
    assertThat(usersDto.get(0)).isNotNull();
    assertThat(usersDto.get(0).getId()).isEqualTo(1L);
    assertThat(usersDto.get(0).getName()).isEqualTo("Carlos Alberto Arroyo Martínez");
    assertThat(usersDto.get(0).getAge()).isEqualTo(Byte.valueOf("28"));
    assertThat(usersDto.get(0).getEmail()).isEqualTo("carroyom@mail.com");
    assertThat(usersDto.get(0).getUsername()).isEqualTo("carroyom");
    assertThat(usersDto.get(0).getRole()).isNotNull();
    assertThat(usersDto.get(0).getRole().getId()).isEqualTo(1);
    assertThat(usersDto.get(0).getRole().getTitle()).isEqualTo("App//Admin");
    assertThat(usersDto.get(0).getRole().getDescription()).isEqualTo("Role for admins users");
    assertThat(usersDto.get(0).getIsActive()).isEqualTo(Boolean.TRUE);
    assertThat(usersDto.get(0).getCreatedAt()).isNotNull();
    assertThat(usersDto.get(0).getUpdatedAt()).isNotNull();
  }

  @Test
  @DisplayName("Should return UserDto when find user by id with existing id")
  void shouldReturnWhenFindUserByIdWithExistingId() {
    when(userRepository.findById(any())).thenReturn(Optional.of(user));

    UserDto userDto = userService.findById(1L);

    assertThat(userDto).isNotNull();
    assertThat(userDto.getId()).isEqualTo(1L);
    assertThat(userDto.getName()).isEqualTo("Carlos Alberto Arroyo Martínez");
    assertThat(userDto.getAge()).isEqualTo(Byte.valueOf("28"));
    assertThat(userDto.getEmail()).isEqualTo("carroyom@mail.com");
    assertThat(userDto.getUsername()).isEqualTo("carroyom");
    assertThat(userDto.getRole()).isNotNull();
    assertThat(userDto.getRole().getId()).isEqualTo(1);
    assertThat(userDto.getRole().getTitle()).isEqualTo("App//Admin");
    assertThat(userDto.getRole().getDescription()).isEqualTo("Role for admins users");
    assertThat(userDto.getIsActive()).isEqualTo(Boolean.TRUE);
    assertThat(userDto.getCreatedAt()).isNotNull();
    assertThat(userDto.getUpdatedAt()).isNotNull();
  }

  @Test
  @DisplayName("Should throw ResponseStatusException when find a user by id with non existing id")
  void shouldThrowWhenFindUserByIdWithNonExistingId() {
    when(userRepository.findById(any())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.findById(1L)).isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining(HttpStatus.NOT_FOUND.toString())
        .hasMessageContaining(AppMessages.USER_NOT_FOUND_EXCEPTION);
  }

  @Test
  @DisplayName("Should return UserDto when create a user with valid data")
  void shouldReturnWhenCreateUserWithValidData() {
    CreateUserRequestDto requestDto = CreateUserRequestDto.builder()
        .name("Cathy Stefania Guido Rojas")
        .age(Byte.valueOf("28"))
        .email("cguidor@mail.com")
        .username("cguidor")
        .roleId(2)
        .build();

    when(userRepository.existsByUsername(any())).thenReturn(false);
    when(userRepository.existsByEmail(any())).thenReturn(false);
    when(userRepository.save(any(User.class)))
        .thenReturn(UserDtoMapper.INSTANCE.createRequestToEntity(requestDto));

    UserDto userDto = userService.create(requestDto);

    assertThat(userDto).isNotNull();
    assertThat(userDto.getName()).isEqualTo("Cathy Stefania Guido Rojas");
    assertThat(userDto.getAge()).isEqualTo(Byte.valueOf("28"));
    assertThat(userDto.getEmail()).isEqualTo("cguidor@mail.com");
    assertThat(userDto.getUsername()).isEqualTo("cguidor");
  }

  @Test
  @DisplayName("Should throw ResponseStatusException when create a user with existing username")
  void shouldThrowWhenCreateUserWithExistingUsername() {
    CreateUserRequestDto requestDto = CreateUserRequestDto.builder().build();

    when(userRepository.existsByUsername(any())).thenReturn(true);

    assertThatThrownBy(() -> userService.create(requestDto))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining(HttpStatus.BAD_REQUEST.toString())
        .hasMessageContaining(AppMessages.USERNAME_ALREADY_EXISTS_EXCEPTION);
  }

  @Test
  @DisplayName("Should throw ResponseStatusException when create a user with existing email")
  void shouldThrowWhenCreateUserWithExistingEmail() {
    CreateUserRequestDto requestDto = CreateUserRequestDto.builder().build();

    when(userRepository.existsByEmail(any())).thenReturn(true);

    assertThatThrownBy(() -> userService.create(requestDto))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining(HttpStatus.BAD_REQUEST.toString())
        .hasMessageContaining(AppMessages.EMAIL_ALREADY_EXISTS_EXCEPTION);
  }

  @Test
  @DisplayName("Should update user with valid data")
  void shouldUpdateUserWithValidData() {
    UpdateUserRequestDto requestDto = UpdateUserRequestDto.builder()
        .name("Carlos Alberto")
        .age(Byte.valueOf("30"))
        .build();

    when(userRepository.findById(any())).thenReturn(Optional.of(user));
    when(userRepository.save(any(User.class))).thenReturn(user);

    userService.update(1L, requestDto);

    verify(userRepository).save(user);
    assertThat(user.getId()).isEqualTo(1L);
    assertThat(user.getName()).isEqualTo("Carlos Alberto");
    assertThat(user.getAge()).isEqualTo(Byte.valueOf("30"));
    assertThat(user.getEmail()).isEqualTo("carroyom@mail.com");
    assertThat(user.getUsername()).isEqualTo("carroyom");
    assertThat(user.getRoleId()).isEqualTo(1);
    assertThat(user.getIsActive()).isEqualTo(Boolean.TRUE);
    assertThat(user.getCreatedAt()).isNotNull();
    assertThat(user.getUpdatedAt()).isNotNull();
  }

  @Test
  @DisplayName("Should throw ResponseStatusException when update user with non existing id")
  void shouldThrowWhenUpdateUserWithInvalidData() {
    UpdateUserRequestDto requestDto = UpdateUserRequestDto.builder().build();

    when(userRepository.findById(any())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.update(1L, requestDto))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining(HttpStatus.NOT_FOUND.toString())
        .hasMessageContaining(AppMessages.USER_NOT_FOUND_EXCEPTION);
  }

  @Test
  @DisplayName("Should deactivate user with existing id")
  void shouldDeactivateUserWithExistingId() {
    when(userRepository.findById(any())).thenReturn(Optional.of(user));
    when(userRepository.save(any(User.class))).thenReturn(user);

    userService.deleteById(1L);

    verify(userRepository).save(user);
    assertThat(user.getIsActive()).isFalse();
  }

  @Test
  @DisplayName("Should throw ResponseStatusException when deactivate user with non existing id")
  void shouldThrowWhenDeactivateUserWithNonExistingId() {
    when(userRepository.findById(any())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.deleteById(1L)).isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining(HttpStatus.NOT_FOUND.toString())
        .hasMessageContaining(AppMessages.USER_NOT_FOUND_EXCEPTION);
  }
}
