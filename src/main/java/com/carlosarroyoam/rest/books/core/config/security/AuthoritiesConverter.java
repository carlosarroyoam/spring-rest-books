package com.carlosarroyoam.rest.books.core.config.security;

import java.util.Collection;
import java.util.Map;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;

public interface AuthoritiesConverter
    extends Converter<Map<String, Object>, Collection<GrantedAuthority>> {
}
