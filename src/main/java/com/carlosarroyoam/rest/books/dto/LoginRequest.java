package com.carlosarroyoam.rest.books.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginRequest {
	@NotBlank
	@Size(min = 3, max = 128)
	@Email
	private String email;

	@NotBlank
	@Size(min = 3, max = 128)
	private String password;
}
