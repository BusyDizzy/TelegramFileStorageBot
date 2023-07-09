package com.java.controller;

import com.java.dto.MailParams;
import com.java.service.MailSenderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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

    @PostMapping("/send-mail")
    public ResponseEntity<?> sendMail(@RequestBody MailParams mailParams) {
        mailSenderService.sendEmail(mailParams);
        // TODO Check the returned values or set return void
        return ResponseEntity.ok().build();
    }

//    @PostMapping("/send-data")
//    public ResponseEntity<?> sendMailWithMultipleCovers(@RequestBody MailParams mailParams) {
//        mailSenderService.sendEmailWithMultipleCovers(mailParams);
//        // TODO Check the returned values or set return void
//        return ResponseEntity.ok().build();
//    }

    @PostMapping("/send-data")
    public ResponseEntity<?> sendMailWithMultipleCovers(@RequestParam("emailTo") String emailTo,
                                                        @RequestParam("coverLetterFiles") List<MultipartFile> coverLetterFiles) {
        MailParams mailParams = MailParams.builder()
                .emailTo(emailTo)
                .coverLetterFiles(coverLetterFiles)
                .build();
        mailSenderService.sendEmailWithMultipleCovers(mailParams);
        return ResponseEntity.ok().build();
    }
}