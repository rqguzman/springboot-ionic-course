package com.rafaelguzman.cursomc.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private Environment env; 
	
	private static final String[] PUBLIC_MATCHERS = { "/h2-console/**"};

	private static final String[] PUBLIC_MATCHERS_GET = { "/produtos/**", "/categorias/**" };

	/* Configura os acessos com base em array de URLs */
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		
		/* Permite acesso ao database de testes */
		if (Arrays.asList(env.getActiveProfiles()).contains("test")) {
			http.headers().frameOptions().disable();			
		}
		
		/*
		 * Invoca o bean 'corsConfigurationSource' Por se tratar de uma aplicação
		 * 'stateless', desabilita a proteção contra ataques do tipo csrf
		 */
		http.cors().and().csrf().disable();
		/*
		 * Permite o acesso dos requests com base nas URLs dos Array 'PUBLIC_MATCHERS*'.
		 * Qualquer outra URL, exige autenticação (.anyRequest().authenticated())
		 */
		http.authorizeRequests()
			.antMatchers(HttpMethod.GET, PUBLIC_MATCHERS_GET).permitAll()
			.antMatchers(PUBLIC_MATCHERS).permitAll()
			.anyRequest().authenticated();

		/* Assegura que o backend não vai criar uma sessão de usuário */
		http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

	}

	/*
	 * Autenticação básica de CORS Permite o acesso CORS. Necessário nesta etapa de
	 * desenvolvimento e testes
	 */
	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

		/* Concede acesso básico aos endpoints por requisições de múltiplas fontes */
		source.registerCorsConfiguration("/**", new CorsConfiguration().applyPermitDefaultValues());
		return source;
	}
}
