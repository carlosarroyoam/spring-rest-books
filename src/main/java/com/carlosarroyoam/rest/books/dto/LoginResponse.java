package com.carlosarroyoam.rest.books.dto;

import lombok.Data;

@Data
public class LoginResponse {

	private String email;
	private String accessToken;

}
