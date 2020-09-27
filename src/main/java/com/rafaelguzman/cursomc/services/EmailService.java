package com.rafaelguzman.cursomc.services;

import org.springframework.mail.SimpleMailMessage;

import com.rafaelguzman.cursomc.domain.Pedido;

public interface EmailService {

	void sendOrderConfirmationEmail(Pedido obj);
	void sendEmail(SimpleMailMessage msg);
}
