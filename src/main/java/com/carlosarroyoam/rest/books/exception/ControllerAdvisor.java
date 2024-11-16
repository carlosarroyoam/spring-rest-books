package com.carlosarroyoam.rest.books.exception;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.carlosarroyoam.rest.books.dto.AppExceptionResponse;

@ControllerAdvice
class ControllerAdvisor {
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

	@ExceptionHandler(NoHandlerFoundException.class)
	public ResponseEntity<AppExceptionResponse> handleNoHandlerFoundException(NoHandlerFoundException ex,
			WebRequest request) {
		HttpStatus statusCode = HttpStatus.NOT_FOUND;

		AppExceptionResponse appExceptionResponse = new AppExceptionResponse();
		appExceptionResponse.setMessage("No endpoint found for: " + request.getDescription(false).replace("uri=", ""));
		appExceptionResponse.setError(statusCode.getReasonPhrase());
		appExceptionResponse.setStatus(statusCode.value());
		appExceptionResponse.setPath(request.getDescription(false).replace("uri=", ""));
		appExceptionResponse.setTimestamp(ZonedDateTime.now(ZoneId.of("UTC")));

		return new ResponseEntity<>(appExceptionResponse, statusCode);
	}

	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<AppExceptionResponse> handleNoResourceFoundException(NoResourceFoundException ex,
			WebRequest request) {
		HttpStatus statusCode = HttpStatus.NOT_FOUND;

		AppExceptionResponse appExceptionResponse = new AppExceptionResponse();
		appExceptionResponse.setMessage("No static resource: " + request.getDescription(false).replace("uri=", ""));
		appExceptionResponse.setError(statusCode.getReasonPhrase());
		appExceptionResponse.setStatus(statusCode.value());
		appExceptionResponse.setPath(request.getDescription(false).replace("uri=", ""));
		appExceptionResponse.setTimestamp(ZonedDateTime.now(ZoneId.of("UTC")));

		return new ResponseEntity<>(appExceptionResponse, statusCode);
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<AppExceptionResponse> handleHttpRequestMethodNotSupportedException(
			HttpRequestMethodNotSupportedException ex, WebRequest request) {
		HttpStatus statusCode = HttpStatus.BAD_REQUEST;

		AppExceptionResponse appExceptionResponse = new AppExceptionResponse();
		appExceptionResponse.setMessage(ex.getMessage());
		appExceptionResponse.setError(statusCode.getReasonPhrase());
		appExceptionResponse.setStatus(statusCode.value());
		appExceptionResponse.setPath(request.getDescription(false).replace("uri=", ""));
		appExceptionResponse.setTimestamp(ZonedDateTime.now(ZoneId.of("UTC")));

		return new ResponseEntity<>(appExceptionResponse, statusCode);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<AppExceptionResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			WebRequest request) {
		HttpStatus statusCode = HttpStatus.BAD_REQUEST;

		AppExceptionResponse appExceptionResponse = new AppExceptionResponse();
		appExceptionResponse.setMessage("Request data is not valid");
		appExceptionResponse.setError(statusCode.getReasonPhrase());
		appExceptionResponse.setStatus(statusCode.value());
		appExceptionResponse.setPath(request.getDescription(false).replace("uri=", ""));
		appExceptionResponse.setTimestamp(ZonedDateTime.now(ZoneId.of("UTC")));
		appExceptionResponse.setDetails(ex.getBindingResult().getFieldErrors().stream().collect(
				Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage, (first, second) -> second)));

		return new ResponseEntity<>(appExceptionResponse, statusCode);
	}

	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<AppExceptionResponse> handleAuthenticationException(AuthenticationException ex,
			WebRequest request) {
		HttpStatus statusCode = HttpStatus.UNAUTHORIZED;

		AppExceptionResponse appExceptionResponse = new AppExceptionResponse();
		appExceptionResponse.setMessage(ex.getMessage());
		appExceptionResponse.setError(statusCode.getReasonPhrase());
		appExceptionResponse.setStatus(statusCode.value());
		appExceptionResponse.setPath(request.getDescription(false).replace("uri=", ""));
		appExceptionResponse.setTimestamp(ZonedDateTime.now(ZoneId.of("UTC")));

		return new ResponseEntity<>(appExceptionResponse, statusCode);
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

		ex.printStackTrace();

		return new ResponseEntity<>(appExceptionResponse, statusCode);
	}
}