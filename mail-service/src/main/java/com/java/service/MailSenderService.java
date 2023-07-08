package com.java.service;

import com.java.dto.MailParams;

public interface MailSenderService {
    void sendActivationEmail(MailParams mailParams);

    void sendEmail(MailParams mailParams);
}