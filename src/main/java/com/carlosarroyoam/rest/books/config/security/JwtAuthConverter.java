package com.carlosarroyoam.rest.books.config.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {
  @Override
  public AbstractAuthenticationToken convert(Jwt jwt) {
    Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
    return new JwtAuthenticationToken(jwt, authorities);
  }

  @SuppressWarnings("unchecked")
  private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
    var authorities = new ArrayList<GrantedAuthority>();
    var realmAccess = jwt.getClaimAsMap("realm_access");

    if (realmAccess != null && realmAccess.get("roles") != null) {
      var roles = realmAccess.get("roles");
      if (roles instanceof List l) {
        l.forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role)));
      }
    }

    return authorities;
  }
}
