package com.carlosarroyoam.rest.books.core.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.servlet.FilterChain;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/** Pruebas unitarias de {@link CorrelationIdFilter}. */
class CorrelationIdFilterTest {
  private final CorrelationIdFilter filter = new CorrelationIdFilter();

  @AfterEach
  void tearDown() {
    MDC.clear();
  }

  @Test
  @DisplayName(
      "Given no X-Request-Id header, when doFilter, then a UUID is set in the MDC and echoed"
          + " in the response header")
  void givenNoRequestIdHeader_whenDoFilter_thenGeneratesUuidInMdcAndResponseHeader()
      throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/users");
    MockHttpServletResponse response = new MockHttpServletResponse();
    AtomicReference<String> mdcDuringChain = new AtomicReference<>();
    FilterChain chain =
        (req, res) -> mdcDuringChain.set(MDC.get(CorrelationIdFilter.REQUEST_ID_KEY));

    filter.doFilter(request, response, chain);

    assertThat(mdcDuringChain.get()).isNotBlank();
    assertThat(response.getHeader(CorrelationIdFilter.REQUEST_ID_HEADER))
        .isEqualTo(mdcDuringChain.get());
  }

  @Test
  @DisplayName(
      "Given an X-Request-Id header, when doFilter, then that value is reused in the MDC and"
          + " the response header")
  void givenRequestIdHeader_whenDoFilter_thenReusesItInMdcAndResponseHeader() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/users");
    request.addHeader(CorrelationIdFilter.REQUEST_ID_HEADER, "trace-123");
    MockHttpServletResponse response = new MockHttpServletResponse();
    AtomicReference<String> mdcDuringChain = new AtomicReference<>();
    FilterChain chain =
        (req, res) -> mdcDuringChain.set(MDC.get(CorrelationIdFilter.REQUEST_ID_KEY));

    filter.doFilter(request, response, chain);

    assertThat(mdcDuringChain.get()).isEqualTo("trace-123");
    assertThat(response.getHeader(CorrelationIdFilter.REQUEST_ID_HEADER)).isEqualTo("trace-123");
  }

  @Test
  @DisplayName(
      "Given the chain throws, when doFilter, then the requestId is still removed from the"
          + " MDC")
  void givenChainThrows_whenDoFilter_thenRequestIdIsRemovedFromMdc() {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/users");
    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain chain =
        (req, res) -> {
          throw new RuntimeException("boom");
        };

    assertThatThrownBy(() -> filter.doFilter(request, response, chain))
        .isInstanceOf(RuntimeException.class);

    assertThat(MDC.get(CorrelationIdFilter.REQUEST_ID_KEY)).isNull();
  }
}
