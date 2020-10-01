package com.rafaelguzman.cursomc.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rafaelguzman.cursomc.dto.CredenciaisDTO;

public class JWTAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

	private AuthenticationManager authenticationManager;
	
	private JWTUtil jwtUtil;
	
	public JWTAuthenticationFilter(AuthenticationManager authenticationManager, JWTUtil jWTUtil) {
		
		setAuthenticationFailureHandler(new JWTAuthenticationFailureHandler());
		this.authenticationManager = authenticationManager;
		this.jwtUtil = jWTUtil;
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest req, HttpServletResponse res) throws AuthenticationException {

		try {
			
			CredenciaisDTO creds = new ObjectMapper().readValue(req.getInputStream(), CredenciaisDTO.class);// [1]
			UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(creds.getEmail(), creds.getSenha(), new ArrayList<>());// [2]
			Authentication auth = authenticationManager.authenticate(authToken);// [3]
			return auth;
			
		} catch (IOException e) {
			
			throw new RuntimeException(e);
		}
		
	}

	/*
	 * Se o método 'Authentication()' acima for bem-sucedido, este método deve gerar o token */
	@Override
	protected void successfulAuthentication(HttpServletRequest req, HttpServletResponse res, FilterChain chain,	Authentication auth) throws IOException, ServletException {
		
		String username = ((UserSS) auth.getPrincipal()).getUsername();
		String token = jwtUtil.generateToken(username);
		res.addHeader("Authorization", "Bearer " + token);
	}
	
	/* TRECHO DE CÓDIGO NECESSÁRIO DEVIDO ATUALIZAÇÃO DO SPRINGBOOT */
	private class JWTAuthenticationFailureHandler implements AuthenticationFailureHandler {

		@Override
		public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
				AuthenticationException exception) throws IOException, ServletException {
			response.setStatus(401);
			response.setContentType("application/json");
			response.getWriter().append(json());
		}

		private String json() {
			long date = new Date().getTime();
			return "{\"timestamp\": " + date + ", "
					+ "\"status\": 401, " 
					+ "\"error\": \"Não autorizado\", "
					+ "\"message\": \"Email ou senha inválidos\", "
					+ "\"path\": \"/login\"}";
		}
	}

}

/*
 * [1] Instanciando um objeto credenciaisDTO a partir dos dados presentes no corpo da requisição
 * 
 * [2] Instanciando objeto padrão do Spring Security para autenticação.
 * 
 * [3] Com as informações obtidas, verifica a validade e informa se autenticação ocorreu com sucesso ou não.
 * */
