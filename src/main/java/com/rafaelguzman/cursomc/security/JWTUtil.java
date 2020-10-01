package com.rafaelguzman.cursomc.security;

import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JWTUtil {
	
	@Value("${jwt.secret}")
	private String secret;

	@Value("${jwt.expiration}")
	private Long expiration;
	
	/* Gera um Token */
	public String generateToken(String username) {
		
		return Jwts.builder() // método da biblioteca do jwt importada no pom.xml que gera o token
				.setSubject(username) // usuário
				.setExpiration(new Date(System.currentTimeMillis() + expiration)) // determina a validade do token
				.signWith(SignatureAlgorithm.HS512, secret.getBytes()) // Como o token assinado? algoritmo e chave 
				.compact();
	}
}
