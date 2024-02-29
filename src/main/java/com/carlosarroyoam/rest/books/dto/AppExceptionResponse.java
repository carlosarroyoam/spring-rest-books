package com.carlosarroyoam.rest.books.dto;

import java.time.ZonedDateTime;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@JsonInclude(Include.NON_NULL)
@Data
public class AppExceptionResponse {

	private String message;
	private String error;
	private Map<String, String> details;
	private int status;
	private String path;
	private ZonedDateTime timestamp;

}
