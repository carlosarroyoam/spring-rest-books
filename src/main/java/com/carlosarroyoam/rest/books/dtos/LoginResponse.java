package com.carlosarroyoam.rest.books.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {

	private String email;
	private String accessToken;

}
