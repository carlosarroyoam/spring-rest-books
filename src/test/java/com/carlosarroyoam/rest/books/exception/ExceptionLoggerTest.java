package com.carlosarroyoam.rest.books.exception;

import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.carlosarroyoam.rest.books.core.exception.ExceptionLogger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;

/** Pruebas unitarias de {@link ExceptionLogger} y su política de severidad 4xx/5xx. */
class ExceptionLoggerTest {
  private final ExceptionLogger exceptionLogger = new ExceptionLogger();
  private final Logger logbackLogger = (Logger) LoggerFactory.getLogger(ExceptionLogger.class);
  private ListAppender<ILoggingEvent> appender;

  @BeforeEach
  void setUp() {
    appender = new ListAppender<>();
    appender.start();
    logbackLogger.addAppender(appender);
  }

  @AfterEach
  void tearDown() {
    logbackLogger.detachAppender(appender);
  }

  private static MockHttpServletRequest request(String method, String uri) {
    MockHttpServletRequest request = new MockHttpServletRequest(method, uri);
    request.setRequestURI(uri);
    return request;
  }

  @Test
  @DisplayName("Given a 4xx status, when log, then writes a WARN line without a stack trace")
  void given4xxStatus_whenLog_thenWritesWarnWithoutStackTrace() {
    exceptionLogger.log(
        HttpStatus.NOT_FOUND,
        "User not found",
        request("GET", "/users/999"),
        new RuntimeException("boom"));

    assertThat(appender.list).hasSize(1);
    ILoggingEvent event = appender.list.get(0);
    assertThat(event.getLevel()).isEqualTo(Level.WARN);
    assertThat(event.getFormattedMessage()).isEqualTo("GET /users/999 -> User not found");
    assertThat(event.getThrowableProxy()).isNull();
  }

  @Test
  @DisplayName("Given a 5xx status, when log, then writes an ERROR line with the throwable")
  void given5xxStatus_whenLog_thenWritesErrorWithThrowable() {
    RuntimeException cause = new RuntimeException("boom");

    exceptionLogger.log(
        HttpStatus.INTERNAL_SERVER_ERROR, "Unhandled exception", request("POST", "/orders"), cause);

    assertThat(appender.list).hasSize(1);
    ILoggingEvent event = appender.list.get(0);
    assertThat(event.getLevel()).isEqualTo(Level.ERROR);
    assertThat(event.getFormattedMessage()).isEqualTo("POST /orders -> Unhandled exception");
    assertThat(event.getThrowableProxy()).isNotNull();
    assertThat(event.getThrowableProxy().getMessage()).isEqualTo("boom");
  }
}
