package com.java.service.impl;

import com.java.dto.MailParams;
import com.java.entity.AppUser;
import com.java.entity.Pair;
import com.java.repository.AppUserRepository;
import com.java.service.AppUserService;
import com.java.utils.CryptoTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.util.List;
import java.util.Objects;

import static com.java.entity.enums.UserState.BASIC_STATE;
import static com.java.entity.enums.UserState.WAIT_FOR_EMAIL_STATE;

@Service
@Slf4j
public class AppUserServiceImpl implements AppUserService {

    private final AppUserRepository appUserRepository;
    private final CryptoTool cryptoTool;

    private final RestTemplate restTemplate;
    @Value("${service.mail.uri}")
    private String mailServiceActivationUri;

    @Value("${service.mail.data-uri}")
    private String mailServiceDataUri;

    public AppUserServiceImpl(AppUserRepository appUserRepository, CryptoTool cryptoTool, RestTemplate restTemplate) {
        this.appUserRepository = appUserRepository;
        this.cryptoTool = cryptoTool;
        this.restTemplate = restTemplate;
    }


    @Override
    public String registerUser(AppUser appuser) {
        if (appuser.getIsActive()) {
            return "Вы уже зарегистрированы";
        } else if (appuser.getEmail() != null) {
            return "Вам на почту уже было отправлено письмо. "
                    + "Перейдите по ссылке в письме для подтверждения регистрации.";
        }
        appuser.setState(WAIT_FOR_EMAIL_STATE);
        appUserRepository.save(appuser);
        return "Введите, пожалуйста, ваш email:";
    }

    @Override
    public String setEmail(AppUser appUser, String email) {
        try {
            InternetAddress emailAddr = new InternetAddress(email);
            emailAddr.validate();
        } catch (AddressException e) {
            return "Введите, пожалуйста, корректный email. Для отмены команды введите /cancel";
        }
        var optional = appUserRepository.findByEmail(email);
        if (optional.isEmpty()) {
            appUser.setEmail(email);
            appUser.setState(BASIC_STATE);
            appUser = appUserRepository.save(appUser);

            var cryptoUserId = cryptoTool.hashOf(appUser.getId());
            var response = sendRequestToMailService(cryptoUserId, email);
            if (response.getStatusCode() != HttpStatus.OK) {
                var msg = String.format("Отправка email на почту %s не удалась...", email);
                log.error(msg);
                appUser.setEmail(null);

                appUserRepository.save(appUser);
                return msg;
            }
            return "Вам на почту было отправлено письмо. "
                    + "Перейдите по ссылке в письме для подтверждения регистрации.";
        } else {
            return "Этот email уже используется. Введите другой email. "
                    + "Для отмены команды введите /cancel";
        }
    }

    private ResponseEntity<String> sendRequestToMailService(String cryptoUserId, String email) {
        var restTemplate = new RestTemplate();
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var mailParams = MailParams.builder()
                .id(cryptoUserId)
                .emailTo(email)
                .build();
        var request = new HttpEntity<>(mailParams, headers);
        log.info("Формируем письмо для регистрации, на почту: {}", email);
        return restTemplate.exchange(mailServiceActivationUri,
                HttpMethod.POST,
                request,
                String.class
        );
    }

    // TODO Возможно тут не к месту публичный метод
    @Override
    public ResponseEntity<String> sendUserData(AppUser appUser, String message) {
        var restTemplate = new RestTemplate();
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var mailParams = MailParams.builder()
                .coverLetter(message)
                .emailTo(appUser.getEmail())
                .build();
        var request = new HttpEntity<>(mailParams, headers);
        log.info("Формируем письмо с данными на почту: {}", appUser.getEmail());
        return restTemplate.exchange(mailServiceDataUri,
                HttpMethod.POST,
                request,
                String.class
        );
    }

    //    public ResponseEntity<String> sendMultipleCoverLetters(AppUser appUser, List<String> coverLetterContents) {
//        var headers = new HttpHeaders();
//        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
//
//        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
//        for (int i = 0; i < coverLetterContents.size(); i++) {
//            String letterContent = coverLetterContents.get(i);
//            if (letterContent != null && !letterContent.isEmpty()) {
//                int finalI = i;
//                ByteArrayResource resource = new ByteArrayResource(letterContent.getBytes()) {
//                    @Override
//                    public String getFilename() {
//                        return "cover_letter_" + finalI + ".txt";
//                    }
//                };
//                body.add("coverLetterFiles", new HttpEntity<>(resource, getHeaderForResource(resource)));
//            } else {
//                log.warn("Cover letter " + i + " is null or empty");
//            }
//        }
//
//        body.add("emailTo", appUser.getEmail());
//
//        var request = new HttpEntity<>(body, headers);
//        log.info("Sending multiple cover letters via email to: {}", appUser.getEmail());
//
//        return restTemplate.exchange(mailServiceDataUri, HttpMethod.POST, request, String.class);
//    }
    public ResponseEntity<String> sendMultipleCoverLetters(AppUser appUser,
                                                           List<Pair<String, String>> coverLetterContentsAndFilenames,
                                                           String jobsInfo) {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        for (Pair<String, String> coverLetterContentAndFilename : coverLetterContentsAndFilenames) {
            String letterContent = coverLetterContentAndFilename.getLeft();
            String filename = coverLetterContentAndFilename.getRight();
            if (letterContent != null && !letterContent.isEmpty()) {
                ByteArrayResource resource = new ByteArrayResource(letterContent.getBytes()) {
                    @Override
                    public String getFilename() {
                        return filename + ".txt";
                    }
                };
                body.add("coverLetterFiles", new HttpEntity<>(resource, getHeaderForResource(resource)));
            } else {
                log.warn("Cover letter for " + filename + " is null or empty");
            }
        }

        body.add("emailTo", appUser.getEmail());
        body.add("emailBody", jobsInfo);

        var request = new HttpEntity<>(body, headers);
        log.info("Sending multiple cover letters via email to: {}", appUser.getEmail());

        return restTemplate.exchange(mailServiceDataUri, HttpMethod.POST, request, String.class);
    }

    private HttpHeaders getHeaderForResource(ByteArrayResource resource) {
        HttpHeaders pictureHeader = new HttpHeaders();
        pictureHeader.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        pictureHeader.setContentLength(resource.contentLength());
        pictureHeader.setContentDisposition(ContentDisposition.builder("form-data")
                .name("coverLetterFiles")
                .filename(Objects.requireNonNull(resource.getFilename()))
                .build());
        return pictureHeader;
    }
}