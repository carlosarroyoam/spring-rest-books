package com.carlosarroyoam.rest.books.core.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.carlosarroyoam.rest.books.core.constant.MessageKeys;
import com.carlosarroyoam.rest.books.support.testutils.TestMessageSources;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.mock.web.MockHttpServletRequest;

class GlobalExceptionHandlerTest {
  private MessageSource messageSource;
  private GlobalExceptionHandler handler;
  private MockHttpServletRequest request;

  @BeforeEach
  void setUp() {
    messageSource = TestMessageSources.messages();
    handler =
        new GlobalExceptionHandler(new ProblemDetailFactory(), new ExceptionLogger(), messageSource);
    request = new MockHttpServletRequest("GET", "/books/1");
  }

  @Test
  void givenMessageKey_whenHandleAppException_thenDetailIsResolvedText() {
    ResourceNotFoundException ex =
        new ResourceNotFoundException(MessageKeys.BOOK_NOT_FOUND_EXCEPTION);

    ProblemDetail problemDetail = handler.handleAppException(ex, request);

    assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    assertThat(problemDetail.getDetail()).isEqualTo("Book not found");
  }

  @Test
  void givenUnknownKey_whenHandleAppException_thenDetailIsMessageAsIs() {
    ResourceNotFoundException ex = new ResourceNotFoundException("Some literal message");

    ProblemDetail problemDetail = handler.handleAppException(ex, request);

    assertThat(problemDetail.getDetail()).isEqualTo("Some literal message");
  }

  @Test
  void givenMessagesKeys_whenResolve_thenEveryKeyHasEntryInMessagesProperties()
      throws IllegalAccessException {
    for (Field field : MessageKeys.class.getDeclaredFields()) {
      if (!Modifier.isPublic(field.getModifiers())) {
        continue;
      }
      String key = (String) field.get(null);

      String resolved = messageSource.getMessage(key, null, null, Locale.ENGLISH);

      assertThat(resolved).as("messages.properties entry for %s", key).isNotNull();
    }
  }
}
