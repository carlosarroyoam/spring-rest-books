package com.carlosarroyoam.rest.books.core.security;

import java.util.Collection;
import java.util.Map;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;

/** Convierte los claims de un JWT en las {@link GrantedAuthority} del usuario autenticado. */
@FunctionalInterface
public interface AuthoritiesConverter
    extends Converter<Map<String, Object>, Collection<GrantedAuthority>> {}
