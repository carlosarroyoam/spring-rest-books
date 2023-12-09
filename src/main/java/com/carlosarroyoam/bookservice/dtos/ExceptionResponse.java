package com.carlosarroyoam.bookservice.dtos;

import java.time.ZonedDateTime;

import lombok.Data;

@Data
public class ExceptionResponse {
	private String message;
	private String error;
	private int status;
	private String path;
	private ZonedDateTime timestamp;
}
