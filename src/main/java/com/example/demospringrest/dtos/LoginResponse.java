package com.example.demospringrest.dtos;

import lombok.Data;

@Data
public class LoginResponse {
	private String email;
	private String jwt;
}
