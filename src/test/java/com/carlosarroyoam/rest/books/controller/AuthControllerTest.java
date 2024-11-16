package com.carlosarroyoam.rest.books.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.carlosarroyoam.rest.books.dto.LoginRequest;
import com.carlosarroyoam.rest.books.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(AuthController.class)
class AuthControllerTest {
	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private AuthService authService;

	@Test
	void shouldReturn_When() throws Exception {
		LoginRequest loginRequest = new LoginRequest("carroyom@mail.com", "secret");

		mockMvc.perform(
				post("/auth").accept(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(loginRequest)))
				.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON));
	}
}
