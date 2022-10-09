package com.example.demospringrest.config.security;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

public class ImprovedUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
	@Override
	protected String obtainUsername(HttpServletRequest request) {
		final String usernameParameter = getUsernameParameter();
		validateQueryParameter(request, usernameParameter);
		return super.obtainUsername(request);
	}

	@Override
	protected String obtainPassword(HttpServletRequest request) {
		final String passwordParameter = getPasswordParameter();
		validateQueryParameter(request, passwordParameter);
		return super.obtainPassword(request);
	}

	private void validateQueryParameter(HttpServletRequest request, String parameter) {
		final String queryString = request.getQueryString();
		if (!StringUtils.isEmpty(queryString)) {
			if (queryString.contains(parameter))
				throw new AuthenticationServiceException(
						"Query parameters for login are a prohibit, use message body only!");

		}
	}
}
