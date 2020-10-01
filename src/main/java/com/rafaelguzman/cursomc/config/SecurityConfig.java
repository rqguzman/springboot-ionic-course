package com.rafaelguzman.cursomc.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.rafaelguzman.cursomc.security.JWTAuthenticationFilter;
import com.rafaelguzman.cursomc.security.JWTAuthorizationFilter;
import com.rafaelguzman.cursomc.security.JWTUtil;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private Environment env; 
	
	@Autowired
	private UserDetailsService userDetailsService;
	
	@Autowired
	private JWTUtil jwtUtil;
	
	private static final String[] PUBLIC_MATCHERS = { "/h2-console/**"};

	private static final String[] PUBLIC_MATCHERS_GET = { "/produtos/**", "/categorias/**", "/clientes/**" };

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
		
		http.addFilter(new JWTAuthenticationFilter(authenticationManager(), jwtUtil));
		http.addFilter(new JWTAuthorizationFilter(authenticationManager(), jwtUtil, userDetailsService));

		/* Assegura que o backend não vai criar uma sessão de usuário */
		http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

	}

	/* Sobrecarga do método anterior.
	 * Informa quem é o UserDetail Service que está sendo usado e 
	 * qual é o algoritmo de configuração da senha 
	 */
	@Override
	public void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userDetailsService).passwordEncoder(bCryptPasswordEncoder());
	}
	
	/*
	 * Autenticação básica de CORS Permite o acesso CORS. 
	 * Necessário nesta etapa de desenvolvimento e testes
	 */
	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

		/* Concede acesso básico aos endpoints por requisições de múltiplas fontes */
		source.registerCorsConfiguration("/**", new CorsConfiguration().applyPermitDefaultValues());
		return source;
	}

	/*
	 * Disponibiliza um componente de criptografia de senhas para injeção
	 */
	@Bean
	public BCryptPasswordEncoder bCryptPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
}
