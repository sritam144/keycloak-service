package com.asiczen.identity.management.service;

import com.asiczen.identity.management.dto.Credentials;
import org.springframework.stereotype.Service;

import java.util.Map;


@Service
public interface MailSenderService {
	public void sendEmail(String request, String to);
}
