package com.carlosarroyoam.rest.books.exceptions;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.carlosarroyoam.rest.books.dtos.AppExceptionResponse;

@ControllerAdvice
public class ControllerAdvisor extends ResponseEntityExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(ControllerAdvisor.class);

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		HttpStatus statusCode = HttpStatus.valueOf(status.value());

		AppExceptionResponse appExceptionResponse = new AppExceptionResponse();
		appExceptionResponse.setMessage("The request data is not valid");
		appExceptionResponse.setError(statusCode.getReasonPhrase());
		appExceptionResponse.setStatus(statusCode.value());
		appExceptionResponse.setPath(request.getDescription(false).replace("uri=", ""));
		appExceptionResponse.setTimestamp(ZonedDateTime.now(ZoneId.of("UTC")));
		appExceptionResponse.setDetails(ex.getBindingResult().getFieldErrors().stream()
				.map(e -> e.getField() + ":" + e.getDefaultMessage()).toList());

		return ResponseEntity.unprocessableEntity().body(appExceptionResponse);
	}

	@ExceptionHandler(ResponseStatusException.class)
	public ResponseEntity<AppExceptionResponse> handleResponseStatusException(ResponseStatusException ex,
			WebRequest request) {
		HttpStatus statusCode = HttpStatus.valueOf(ex.getStatusCode().value());

		AppExceptionResponse appExceptionResponse = new AppExceptionResponse();
		appExceptionResponse.setMessage(ex.getReason());
		appExceptionResponse.setError(statusCode.getReasonPhrase());
		appExceptionResponse.setStatus(statusCode.value());
		appExceptionResponse.setPath(request.getDescription(false).replace("uri=", ""));
		appExceptionResponse.setTimestamp(ZonedDateTime.now(ZoneId.of("UTC")));

		return new ResponseEntity<>(appExceptionResponse, ex.getStatusCode());
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<AppExceptionResponse> handleAllException(Exception ex, WebRequest request) {
		HttpStatus statusCode = HttpStatus.INTERNAL_SERVER_ERROR;

		AppExceptionResponse appExceptionResponse = new AppExceptionResponse();
		appExceptionResponse.setMessage("Whoops! Something went wrong");
		appExceptionResponse.setError(statusCode.getReasonPhrase());
		appExceptionResponse.setStatus(statusCode.value());
		appExceptionResponse.setPath(request.getDescription(false).replace("uri=", ""));
		appExceptionResponse.setTimestamp(ZonedDateTime.now(ZoneId.of("UTC")));

		log.error("Exception: {}", ex.getMessage());

		return ResponseEntity.internalServerError().body(appExceptionResponse);
	}

}