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

  @Mock
  private KeycloakService keycloakService;

  @InjectMocks
  private UserService userService;

  private User user;

  @BeforeEach
  void setUp() {
    LocalDateTime now = LocalDateTime.now();

    user = User.builder()
        .id(1L)
        .firstName("Carlos Alberto")
        .lastName("Arroyo Martínez")
        .email("carroyom@mail.com")
        .username("carroyom")
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
    assertThat(usersDto.get(0).getFirstName()).isEqualTo("Carlos Alberto");
    assertThat(usersDto.get(0).getLastName()).isEqualTo("Arroyo Martínez");
    assertThat(usersDto.get(0).getEmail()).isEqualTo("carroyom@mail.com");
    assertThat(usersDto.get(0).getUsername()).isEqualTo("carroyom");
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
    assertThat(userDto.getFirstName()).isEqualTo("Carlos Alberto");
    assertThat(userDto.getLastName()).isEqualTo("Arroyo Martínez");
    assertThat(userDto.getEmail()).isEqualTo("carroyom@mail.com");
    assertThat(userDto.getUsername()).isEqualTo("carroyom");
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
        .firstName("Cathy Stefania")
        .lastName("Guido Rojas")
        .email("cguidor@mail.com")
        .username("cguidor")
        .build();

    when(userRepository.existsByUsername(any())).thenReturn(false);
    when(userRepository.existsByEmail(any())).thenReturn(false);
    when(userRepository.save(any(User.class)))
        .thenReturn(UserDtoMapper.INSTANCE.createRequestToEntity(requestDto));

    UserDto userDto = userService.create(requestDto);

    assertThat(userDto).isNotNull();
    assertThat(userDto.getFirstName()).isEqualTo("Cathy Stefania");
    assertThat(userDto.getLastName()).isEqualTo("Guido Rojas");
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
        .firstName("Carlos")
        .lastName("Arroyo")
        .build();

    when(userRepository.findById(any())).thenReturn(Optional.of(user));
    when(userRepository.save(any(User.class))).thenReturn(user);

    userService.update(1L, requestDto);

    verify(userRepository).save(user);
    assertThat(user.getId()).isEqualTo(1L);
    assertThat(user.getFirstName()).isEqualTo("Carlos");
    assertThat(user.getLastName()).isEqualTo("Arroyo");
    assertThat(user.getEmail()).isEqualTo("carroyom@mail.com");
    assertThat(user.getUsername()).isEqualTo("carroyom");
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
