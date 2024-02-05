package com.carlosarroyoam.bookservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {

	private String email;
	private String accessToken;

}
