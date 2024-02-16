package com.carlosarroyoam.rest.books.services;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;

import com.carlosarroyoam.rest.books.dtos.LoginRequest;
import com.carlosarroyoam.rest.books.dtos.LoginResponse;
import com.carlosarroyoam.rest.books.entities.Role;
import com.carlosarroyoam.rest.books.entities.User;
import com.carlosarroyoam.rest.books.repositories.UserRepository;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private AuthenticationManager authenticationManager;

	@Mock
	private TokenService tokenService;

	@InjectMocks
	private AuthService authService;

	@Test
	@DisplayName("Should return LoginResponse when attempt to auth with existing user")
	void shouldReturnLoginResponseWhenAuthWithExistingUser() {
		LoginRequest loginRequest = new LoginRequest("carlosarroyoam@gmail.com", "secret");
		Role role = new Role("App//Admin", "Role for admins users");
		Optional<User> expectedUser = Optional.of(new User("Carlos Alberto", "Arroyo Martínez",
				"carlosarroyoam@gmail.com", "", role, LocalDateTime.now(), LocalDateTime.now()));
		Mockito.when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(expectedUser);

		LoginResponse response = authService.auth(loginRequest);

		Assertions.assertThat(response.getEmail()).isEqualTo(expectedUser.get().getEmail());
	}

	@Test
	@DisplayName("Should throw NoSuchElementException when attempt to auth with non existing user")
	void shouldThrowExceptionWhenAuthWithNonExistingUser() {
		LoginRequest loginRequest = new LoginRequest("non_existing_user@gmail.com", "secret");
		Mockito.when(userRepository.findByEmail(loginRequest.getEmail())).thenThrow(NoSuchElementException.class);

		Throwable ex = assertThrows(NoSuchElementException.class, () -> authService.auth(loginRequest));

		assertThat(ex, instanceOf(NoSuchElementException.class));
	}

}
