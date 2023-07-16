package com.java.service.impl;

import com.java.dto.MailParams;
import com.java.service.MailSenderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Objects;

@Service
@Slf4j
public class MailSenderServiceImpl implements MailSenderService {
    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String emailFrom;
    @Value("${service.activation.uri}")
    private String activationServiceUri;

    public MailSenderServiceImpl(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    @Override
    public void sendActivationEmail(MailParams mailParams) {
        var subject = "Активация учетной записи";
        var messageBody = getActivationMailBody(mailParams.getId());
        var emailTo = mailParams.getEmailTo();

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(emailFrom);
        mailMessage.setTo(emailTo);
        mailMessage.setSubject(subject);
        mailMessage.setText(messageBody);
        log.info("Отправляем письмо для регистрации, на почту: {}", mailParams.getEmailTo());
        javaMailSender.send(mailMessage);
    }

    @Override
    public void sendEmail(MailParams mailParams) {
        var subject = "Сопроводительные письма на вакансии";
        var messageBody = mailParams.getCoverLetter();
        var emailTo = mailParams.getEmailTo();

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(emailFrom);
        mailMessage.setTo(emailTo);
        mailMessage.setSubject(subject);
        mailMessage.setText(messageBody);
        log.info("Отправляем письмо с данными на почту: {}", mailParams.getEmailTo());
        javaMailSender.send(mailMessage);
    }

    private String getActivationMailBody(String id) {
        var msg = String.format("Для завершения регистрации перейдите по ссылке:\n %s",
                activationServiceUri);
        return msg.replace("{id}", id);
    }

    @Override
    public void sendEmailWithMultipleCovers(MailParams mailParams) {
        var subject = "Сопроводительные письма на вакансии";
        var messageBody = mailParams.getEmailBody();
        var emailTo = mailParams.getEmailTo();

        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper;
        try {
            helper = new MimeMessageHelper(mimeMessage, true);
            helper.setFrom(emailFrom);
            helper.setTo(emailTo);
            helper.setSubject(subject);
            helper.setText(messageBody);
            log.info("Отправляем письмо с данными на почту: {}", mailParams.getEmailTo());

            // Adding the attachments
            for (MultipartFile file : mailParams.getCoverLetterFiles()) {
                helper.addAttachment(Objects.requireNonNull(file.getOriginalFilename()), file);
            }

            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }
}