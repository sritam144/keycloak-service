package com.asiczen.identity.management.config;

import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(jsr250Enabled = true)
public class KeycloakSecurityConfig extends KeycloakWebSecurityConfigurerAdapter {

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		super.configure(http);
		http.authorizeRequests().anyRequest().permitAll();
		http.cors().and().csrf().disable();

	}

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		KeycloakAuthenticationProvider keycloakAuthenticationProvider = keycloakAuthenticationProvider();
		keycloakAuthenticationProvider.setGrantedAuthoritiesMapper(new SimpleAuthorityMapper());
		auth.authenticationProvider(keycloakAuthenticationProvider);
	}

	@Bean
	@Override
	protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
		return new RegisterSessionAuthenticationStrategy(new SessionRegistryImpl());
	}

	@Bean
	public KeycloakSpringBootConfigResolver KeycloakSpringBootConfigResolver() {
		return new KeycloakSpringBootConfigResolver();
	}
	
//	@Bean
//	public WebMvcConfigurer corsConfigurer() {
//		
//		System.out.println("I am executed");
//		
//		return new WebMvcConfigurer() {
//			public void addCorsMappings(CorsRegistry registry) {
//						registry.addMapping("/api/**")
//								.allowedOrigins("*")
//								.allowedMethods("GET","POST","PUT","DELETE").allowedHeaders("*");
//			}
//		};
//	}
}