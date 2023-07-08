package com.java.controller;

import com.java.dto.MailParams;
import com.java.service.MailSenderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mail")
public class MailController {

    private final MailSenderService mailSenderService;

    public MailController(MailSenderService mailSenderService) {
        this.mailSenderService = mailSenderService;
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendActivationMail(@RequestBody MailParams mailParams) {
        mailSenderService.sendActivationEmail(mailParams);
        // TODO Check the returned values or set return void
        return ResponseEntity.ok().build();
    }

    @PostMapping("/send-data")
    public ResponseEntity<?> sendMail(@RequestBody MailParams mailParams) {
        mailSenderService.sendEmail(mailParams);
        // TODO Check the returned values or set return void
        return ResponseEntity.ok().build();
    }
}