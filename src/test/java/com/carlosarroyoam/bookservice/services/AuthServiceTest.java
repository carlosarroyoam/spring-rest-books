package com.carlosarroyoam.bookservice.services;

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

import com.carlosarroyoam.bookservice.dtos.LoginRequest;
import com.carlosarroyoam.bookservice.dtos.LoginResponse;
import com.carlosarroyoam.bookservice.entities.Role;
import com.carlosarroyoam.bookservice.entities.User;
import com.carlosarroyoam.bookservice.repositories.UserRepository;

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
	@DisplayName("Test method throws NoSuchElementException when user not exists")
	void authThrowsExceptionWhenUserNotExists() {
		LoginRequest loginRequest = new LoginRequest("non_existing_user@gmail.com", "secret");

		Mockito.when(userRepository.findByEmail(loginRequest.getEmail())).thenThrow(NoSuchElementException.class);

		Assertions.assertThatThrownBy(() -> authService.auth(loginRequest)).isInstanceOf(NoSuchElementException.class);
	}

	@Test
	@DisplayName("Test method when user exists")
	void authWhenUserExists() {
		LoginRequest loginRequest = new LoginRequest("carlosarroyoam@gmail.com", "secret");
		Role role = new Role("App//Admin", "Role for admins users");
		Optional<User> expectedUser = Optional
				.of(new User("Carlos Alberto", "Arroyo Martínez", "carlosarroyoam@gmail.com", "", role));
		Mockito.when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(expectedUser);

		LoginResponse response = authService.auth(loginRequest);

		Assertions.assertThat(response.getEmail()).isEqualTo(expectedUser.get().getEmail());
	}

}
