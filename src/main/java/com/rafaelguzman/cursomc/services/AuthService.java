package com.rafaelguzman.cursomc.services;

import java.security.SecureRandom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.rafaelguzman.cursomc.domain.Cliente;
import com.rafaelguzman.cursomc.repositories.ClienteRepository;
import com.rafaelguzman.cursomc.services.exceptions.ObjectNotFoundException;

@Service
public class AuthService {

	@Autowired
	private ClienteRepository clienteRepository;

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@Autowired
	private EmailService emailService;

	SecureRandom secRand = new SecureRandom();

	public void sendNewPassword(String email) {

		Cliente cliente = clienteRepository.findByEmail(email);

		if (cliente == null) {
			throw new ObjectNotFoundException("E-mail não encontrado.");
		}

		String newPwd = newPassword();
		cliente.setSenha(bCryptPasswordEncoder.encode(newPwd));
		clienteRepository.save(cliente);
		emailService.sendNewPasswordEmail(cliente, newPwd);
	}

	private String newPassword() {
		char[] vet = new char[10];
		for (int i = 0; i < vet.length; i++) {
			vet[i] = RandomChar();
		}
		return new String(vet);
	}

	private char RandomChar() {
		
		int opt = secRand.nextInt(3);
		
		if (opt == 0) {// gera um dígito
			return (char) (secRand.nextInt(10) + 48);
 		} else if (opt == 1) { // gera letra maiúscula
 			return (char) (secRand.nextInt(26) + 65);
		} else {// gera uma letra minúscula
			return (char) (secRand.nextInt(26) + 97);
		}
	}

}
