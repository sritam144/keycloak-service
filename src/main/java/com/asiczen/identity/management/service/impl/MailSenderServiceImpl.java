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
            log.info("Send mail");
            MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());
            helper.setTo(to);
            helper.setText(password, true);
            helper.setSubject("Password from AzTracker");
            helper.setFrom("noreply@asiczen.com");
            log.info("Before send mail");
            sender.send(message);
            log.info("after send mail");

        } catch (MessagingException e) {
            log.info("Error while sending email");
            log.info(e.getLocalizedMessage());
        }
    }

}
