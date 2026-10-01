package com.cropdeal.authentication.config;

import com.cropdeal.authentication.security.JwtAuthenticationFilter;
import com.cropdeal.authentication.security.OAuth2LoginSuccessHandler;
import com.cropdeal.authentication.service.CustomOAuth2UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpStatus;

@Configuration
public class SecurityConfig {

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public SecurityFilterChain securityFilterChain(
			HttpSecurity http,
			JwtAuthenticationFilter jwtAuthenticationFilter,
			CustomOAuth2UserService customOAuth2UserService,
			OAuth2LoginSuccessHandler oauth2LoginSuccessHandler) throws Exception {

		http
			.csrf(csrf -> csrf.disable())
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(auth -> auth
					.requestMatchers(
							"/swagger-ui/**",
							"/swagger-ui.html",
							"/v3/api-docs/**",
							"/error",
							"/oauth2/**",
							"/login/**",
							"/auth/register",
							"/auth/login",
							"/auth/oauth2-success"
					).permitAll()
					.requestMatchers("/admin/**").hasRole("ADMIN")
					.requestMatchers("/internal/admin/stats").hasRole("ADMIN")
					.requestMatchers("/internal/admin/activity").hasRole("ADMIN")
					.anyRequest().authenticated())
			.exceptionHandling(exceptions -> exceptions.defaultAuthenticationEntryPointFor(
					new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
					request -> request.getServletPath().startsWith("/admin/")))
			.oauth2Login(oauth2 -> oauth2
					.userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
					.successHandler(oauth2LoginSuccessHandler))
			.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}
}