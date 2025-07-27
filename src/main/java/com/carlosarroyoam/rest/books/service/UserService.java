package com.carlosarroyoam.rest.books.service;

import com.carlosarroyoam.rest.books.constant.AppMessages;
import com.carlosarroyoam.rest.books.dto.CreateUserRequestDto;
import com.carlosarroyoam.rest.books.dto.UpdateUserRequestDto;
import com.carlosarroyoam.rest.books.dto.UserDto;
import com.carlosarroyoam.rest.books.dto.UserDto.UserDtoMapper;
import com.carlosarroyoam.rest.books.entity.User;
import com.carlosarroyoam.rest.books.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService implements UserDetailsService {
  private static final Logger log = LoggerFactory.getLogger(UserService.class);
  private final UserRepository userRepository;

  public UserService(final UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User userByUsername = userRepository.findByUsername(username).orElseThrow(() -> {
      log.warn(AppMessages.USER_NOT_FOUND_EXCEPTION);
      return new UsernameNotFoundException("Username not found: " + username);
    });

    return buildUserDetails(userByUsername);
  }

  public List<UserDto> findAll(Integer page, Integer size) {
    Pageable pageable = PageRequest.of(page, size);
    Page<User> users = userRepository.findAll(pageable);
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
    if (userRepository.existsByUsername(requestDto.getUsername())) {
      log.warn(AppMessages.USERNAME_ALREADY_EXISTS_EXCEPTION);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          AppMessages.USERNAME_ALREADY_EXISTS_EXCEPTION);
    }

    if (userRepository.existsByEmail(requestDto.getEmail())) {
      log.warn(AppMessages.EMAIL_ALREADY_EXISTS_EXCEPTION);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          AppMessages.EMAIL_ALREADY_EXISTS_EXCEPTION);
    }

    LocalDateTime now = LocalDateTime.now();
    User user = UserDtoMapper.INSTANCE.toEntity(requestDto);
    user.setIsActive(Boolean.FALSE);
    user.setCreatedAt(now);
    user.setUpdatedAt(now);

    return UserDtoMapper.INSTANCE.toDto(userRepository.save(user));
  }

  @Transactional
  public void update(Long userId, UpdateUserRequestDto requestDto) {
    User userById = userRepository.findById(userId).orElseThrow(() -> {
      log.warn(AppMessages.USER_NOT_FOUND_EXCEPTION);
      return new ResponseStatusException(HttpStatus.NOT_FOUND,
          AppMessages.USER_NOT_FOUND_EXCEPTION);
    });

    if (requestDto.getName() != null) {
      userById.setName(requestDto.getName());
    }

    if (requestDto.getAge() != null) {
      userById.setAge(requestDto.getAge());
    }

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

    userById.setIsActive(Boolean.FALSE);
    userById.setUpdatedAt(LocalDateTime.now());

    userRepository.save(userById);
  }

  private org.springframework.security.core.userdetails.User buildUserDetails(User user) {
    String defaultPassword = "";
    String username = user.getUsername();
    boolean enabled = user.getIsActive();
    boolean accountNonExpired = user.getIsActive();
    boolean credentialsNonExpired = user.getIsActive();
    boolean accountNonLocked = user.getIsActive();
    Collection<? extends GrantedAuthority> authorities = List
        .of(new SimpleGrantedAuthority(user.getRole().getTitle()));

    return new org.springframework.security.core.userdetails.User(username, defaultPassword,
        enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
  }
}
