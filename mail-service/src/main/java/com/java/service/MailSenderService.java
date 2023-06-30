package com.java.service;

import com.java.dto.MailParams;

public interface MailSenderService {
    void send(MailParams mailParams);
}