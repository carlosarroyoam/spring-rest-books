package com.carlosarroyoam.rest.books.user;

import com.carlosarroyoam.rest.books.core.constant.AppMessages;
import com.carlosarroyoam.rest.books.user.dto.CreateUserRequestDto;
import com.carlosarroyoam.rest.books.user.dto.UpdateUserRequestDto;
import com.carlosarroyoam.rest.books.user.dto.UserDto;
import com.carlosarroyoam.rest.books.user.dto.UserDto.UserDtoMapper;
import com.carlosarroyoam.rest.books.user.dto.UserFilterDto;
import com.carlosarroyoam.rest.books.user.entity.User;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {
  private static final Logger log = LoggerFactory.getLogger(UserService.class);
  private final UserRepository userRepository;
  private final KeycloakService keycloakService;

  public UserService(final UserRepository userRepository, final KeycloakService keycloakService) {
    this.userRepository = userRepository;
    this.keycloakService = keycloakService;
  }

  public List<UserDto> findAll(Pageable pageable, UserFilterDto filters) {
    Specification<User> spec = Specification.unrestricted();
    spec = spec.and(UserSpecification.firstNameContains(filters.getFirstName()))
        .and(UserSpecification.lastNameContains(filters.getLastName()))
        .and(UserSpecification.emailContains(filters.getEmail()))
        .and(UserSpecification.usernameContains(filters.getUsername()));

    Page<User> users = userRepository.findAll(spec, pageable);
    return UserDtoMapper.INSTANCE.toDtos(users.getContent());
  }

  public UserDto findById(Long userId) {
    User userById = userRepository.findById(userId).orElseThrow(() -> {
      log.warn(AppMessages.USER_NOT_FOUND_EXCEPTION);
      return new ResponseStatusException(HttpStatus.NOT_FOUND,
          AppMessages.USER_NOT_FOUND_EXCEPTION);
    });

    return UserDtoMapper.INSTANCE.toDto(userById);
  }

  @Transactional
  public UserDto create(CreateUserRequestDto requestDto) {
    if (Boolean.TRUE.equals(userRepository.existsByUsername(requestDto.getUsername()))) {
      log.warn(AppMessages.USERNAME_ALREADY_EXISTS_EXCEPTION);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          AppMessages.USERNAME_ALREADY_EXISTS_EXCEPTION);
    }

    if (Boolean.TRUE.equals(userRepository.existsByEmail(requestDto.getEmail()))) {
      log.warn(AppMessages.EMAIL_ALREADY_EXISTS_EXCEPTION);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          AppMessages.EMAIL_ALREADY_EXISTS_EXCEPTION);
    }

    LocalDateTime now = LocalDateTime.now();
    User user = UserDtoMapper.INSTANCE.createRequestToEntity(requestDto);
    user.setCreatedAt(now);
    user.setUpdatedAt(now);
    User createdUser = userRepository.save(user);

    keycloakService.createUser(requestDto, createdUser.getId());

    return UserDtoMapper.INSTANCE.toDto(createdUser);
  }

  @Transactional
  public void update(Long userId, UpdateUserRequestDto requestDto) {
    User userById = userRepository.findById(userId).orElseThrow(() -> {
      log.warn(AppMessages.USER_NOT_FOUND_EXCEPTION);
      return new ResponseStatusException(HttpStatus.NOT_FOUND,
          AppMessages.USER_NOT_FOUND_EXCEPTION);
    });

    userById.setFirstName(requestDto.getFirstName());
    userById.setLastName(requestDto.getLastName());
    userById.setUpdatedAt(LocalDateTime.now());
    userRepository.save(userById);
  }

  @Transactional
  public void deleteById(Long userId) {
    User userById = userRepository.findById(userId).orElseThrow(() -> {
      log.warn(AppMessages.USER_NOT_FOUND_EXCEPTION);
      return new ResponseStatusException(HttpStatus.NOT_FOUND,
          AppMessages.USER_NOT_FOUND_EXCEPTION);
    });

    userById.setUpdatedAt(LocalDateTime.now());
    userRepository.save(userById);
  }
}
