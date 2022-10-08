package com.example.demospringrest.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.example.demospringrest.services.UserService;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http, UserService userService) throws Exception {
		http.csrf(csrf -> csrf.ignoringAntMatchers("/h2-console/**")).authorizeHttpRequests(requests -> requests
				.antMatchers("/").permitAll().antMatchers("/h2-console/**").permitAll().anyRequest().authenticated());

		http.formLogin(form -> form.loginPage("/login").permitAll()).logout(LogoutConfigurer::permitAll);

		http.userDetailsService(userService);

		return http.build();
	}

	@Bean
	public PasswordEncoder encoder() {
		return new BCryptPasswordEncoder();
	}
}
