package com.carlosarroyoam.rest.books.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {

	private String email;
	private String accessToken;

}
