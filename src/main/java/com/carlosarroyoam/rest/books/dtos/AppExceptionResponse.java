package com.carlosarroyoam.rest.books.dtos;

import java.time.ZonedDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@JsonInclude(Include.NON_NULL)
@Data
public class AppExceptionResponse {

	private String message;
	private String error;
	private List<String> details;
	private int status;
	private String path;
	private ZonedDateTime timestamp;

}
