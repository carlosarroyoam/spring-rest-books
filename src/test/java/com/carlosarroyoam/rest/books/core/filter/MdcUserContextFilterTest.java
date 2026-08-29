package com.carlosarroyoam.rest.books.core.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.servlet.FilterChain;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/** Pruebas unitarias de {@link MdcUserContextFilter}. */
class MdcUserContextFilterTest {
  private final MdcUserContextFilter filter = new MdcUserContextFilter();

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
    MDC.clear();
  }

  @Test
  @DisplayName(
      "Given a JWT with preferred_username, when doFilter, then it is exposed in the MDC"
          + " during the chain and removed afterwards")
  void givenJwtWithPreferredUsername_whenDoFilter_thenExposedInMdcAndRemovedAfterwards()
      throws Exception {
    authenticateWithJwt(jwt -> jwt.subject("b2c3-uuid").claim("preferred_username", "jdoe"));
    AtomicReference<String> usernameDuringChain = new AtomicReference<>();
    FilterChain chain =
        (req, res) -> usernameDuringChain.set(MDC.get(MdcUserContextFilter.USERNAME_KEY));

    filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), chain);

    assertThat(usernameDuringChain.get()).isEqualTo("jdoe");
    assertThat(MDC.get(MdcUserContextFilter.USERNAME_KEY)).isNull();
  }

  @Test
  @DisplayName(
      "Given a JWT without preferred_username, when doFilter, then the subject is used as"
          + " fallback")
  void givenJwtWithoutPreferredUsername_whenDoFilter_thenSubjectIsUsedAsFallback()
      throws Exception {
    authenticateWithJwt(jwt -> jwt.subject("b2c3-uuid"));
    AtomicReference<String> usernameDuringChain = new AtomicReference<>();
    FilterChain chain =
        (req, res) -> usernameDuringChain.set(MDC.get(MdcUserContextFilter.USERNAME_KEY));

    filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), chain);

    assertThat(usernameDuringChain.get()).isEqualTo("b2c3-uuid");
  }

  @Test
  @DisplayName(
      "Given a service account JWT, when doFilter, then the service-account name is exposed")
  void givenServiceAccountJwt_whenDoFilter_thenServiceAccountNameIsExposed() throws Exception {
    authenticateWithJwt(
        jwt -> jwt.subject("sa-uuid").claim("preferred_username", "service-account-orders"));
    AtomicReference<String> usernameDuringChain = new AtomicReference<>();
    FilterChain chain =
        (req, res) -> usernameDuringChain.set(MDC.get(MdcUserContextFilter.USERNAME_KEY));

    filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), chain);

    assertThat(usernameDuringChain.get()).isEqualTo("service-account-orders");
  }

  @Test
  @DisplayName("Given no authentication, when doFilter, then no username key is set in the MDC")
  void givenNoAuthentication_whenDoFilter_thenNoUsernameKeyInMdc() throws Exception {
    AtomicReference<String> usernameDuringChain = new AtomicReference<>();
    FilterChain chain =
        (req, res) -> usernameDuringChain.set(MDC.get(MdcUserContextFilter.USERNAME_KEY));

    filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), chain);

    assertThat(usernameDuringChain.get()).isNull();
  }

  @Test
  @DisplayName(
      "Given an anonymous authentication, when doFilter, then no username key is set in the"
          + " MDC")
  void givenAnonymousAuthentication_whenDoFilter_thenNoUsernameKeyInMdc() throws Exception {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key", "anonymousUser", List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));
    AtomicReference<String> usernameDuringChain = new AtomicReference<>();
    FilterChain chain =
        (req, res) -> usernameDuringChain.set(MDC.get(MdcUserContextFilter.USERNAME_KEY));

    filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), chain);

    assertThat(usernameDuringChain.get()).isNull();
  }

  @Test
  @DisplayName(
      "Given the chain throws, when doFilter, then the username is still removed from the" + " MDC")
  void givenChainThrows_whenDoFilter_thenUsernameIsRemovedFromMdc() {
    authenticateWithJwt(jwt -> jwt.subject("b2c3-uuid").claim("preferred_username", "jdoe"));
    FilterChain chain =
        (req, res) -> {
          throw new RuntimeException("boom");
        };

    assertThatThrownBy(
            () ->
                filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), chain))
        .isInstanceOf(RuntimeException.class);

    assertThat(MDC.get(MdcUserContextFilter.USERNAME_KEY)).isNull();
  }

  private void authenticateWithJwt(java.util.function.Consumer<Jwt.Builder> customizer) {
    Jwt.Builder builder = Jwt.withTokenValue("token").header("alg", "none");
    customizer.accept(builder);
    SecurityContextHolder.getContext()
        .setAuthentication(new JwtAuthenticationToken(builder.build(), List.of()));
  }
}
