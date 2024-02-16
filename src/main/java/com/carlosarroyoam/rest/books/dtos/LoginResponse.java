package com.carlosarroyoam.rest.books.dtos;

import lombok.Data;

@Data
public class LoginResponse {

	private String email;
	private String accessToken;

}
