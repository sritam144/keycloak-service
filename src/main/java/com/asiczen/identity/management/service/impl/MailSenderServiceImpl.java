package com.asiczen.identity.management.service.impl;

import com.asiczen.identity.management.service.MailSenderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class MailSenderServiceImpl implements MailSenderService {

    @Autowired
    private JavaMailSender sender;


    @Override
    public void sendEmail(String password, String to) {
        MimeMessage message = sender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());
            helper.setTo(to);
            helper.setText(password, true);
            helper.setSubject("Password from AzTracker");
            helper.setFrom("noreply@asiczen.com");
            sender.send(message);

        } catch (MessagingException e) {
            log.error("Error while sending email");
            log.error(e.getLocalizedMessage());
        }
    }

}
