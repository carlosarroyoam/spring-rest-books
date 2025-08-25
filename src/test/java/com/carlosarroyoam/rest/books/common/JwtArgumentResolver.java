package com.carlosarroyoam.rest.books.common;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.springframework.core.MethodParameter;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class JwtArgumentResolver implements HandlerMethodArgumentResolver {
  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return Jwt.class.isAssignableFrom(parameter.getParameterType());
  }

  @Override
  public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
      NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
    Instant issuedAt = Instant.now();
    Map<String, Object> headers = Map.of("alg", "none");
    Map<String, Object> claims = new HashMap<>();
    claims.put("preferred_username", "carroyom");
    claims.put("customer_id", 1L);

    return new Jwt("token-value", issuedAt, issuedAt.plusSeconds(3600), headers, claims);
  }
}
