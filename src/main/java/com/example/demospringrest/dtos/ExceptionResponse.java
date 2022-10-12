package com.example.demospringrest.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExceptionResponse {
	private long timestamp;
	private int status;
	private String error;
	private String message;
	private String path;
}
