package com.example.demospringrest.controllers;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.example.demospringrest.dtos.ExceptionResponse;

@ControllerAdvice
public class ControllerAdvisor extends ResponseEntityExceptionHandler {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<Object> handleIllegalArgument(MethodArgumentTypeMismatchException ex, WebRequest request) {
		ExceptionResponse exceptionResponse = new ExceptionResponse();

		exceptionResponse.setMessage(ex.getMessage());
		exceptionResponse.setError(HttpStatus.BAD_REQUEST.getReasonPhrase());
		exceptionResponse.setStatus(HttpStatus.BAD_REQUEST.value());
		exceptionResponse.setPath(request.getDescription(false).replace("uri=", ""));
		exceptionResponse.setTimestamp(Instant.now().toEpochMilli());

		return ResponseEntity.badRequest().body(exceptionResponse);
	}

	@Override
	public ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers,
			HttpStatus status, WebRequest request) {
		ExceptionResponse exceptionResponse = new ExceptionResponse();

		exceptionResponse.setMessage(ex.getAllErrors().get(0).getDefaultMessage());
		exceptionResponse.setError(HttpStatus.BAD_REQUEST.getReasonPhrase());
		exceptionResponse.setStatus(HttpStatus.BAD_REQUEST.value());
		exceptionResponse.setPath(request.getDescription(false).replace("uri=", ""));
		exceptionResponse.setTimestamp(Instant.now().toEpochMilli());

		return ResponseEntity.badRequest().body(exceptionResponse);
	}

	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<ExceptionResponse> exception(RuntimeException ex, WebRequest request) {
		ExceptionResponse exceptionResponse = new ExceptionResponse();

		exceptionResponse.setMessage(ex.getMessage());
		exceptionResponse.setError(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
		exceptionResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		exceptionResponse.setPath(request.getDescription(false).replace("uri=", ""));
		exceptionResponse.setTimestamp(Instant.now().toEpochMilli());

		return ResponseEntity.internalServerError().body(exceptionResponse);
	}
}