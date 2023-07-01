package com.java.service.impl;

import com.java.dto.MailParams;
import com.java.entity.AppUser;
import com.java.repository.AppUserRepository;
import com.java.service.AppUserService;
import com.java.utils.CryptoTool;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import static com.java.entity.enums.UserState.BASIC_STATE;
import static com.java.entity.enums.UserState.WAIT_FOR_EMAIL_STATE;

@Service
@Log4j
public class AppUserServiceImpl implements AppUserService {

    private final AppUserRepository appUserRepository;
    private final CryptoTool cryptoTool;
    @Value("${service.mail.uri}")
    private String mailServiceUri;

    public AppUserServiceImpl(AppUserRepository appUserRepository, CryptoTool cryptoTool) {
        this.appUserRepository = appUserRepository;
        this.cryptoTool = cryptoTool;
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
        return restTemplate.exchange(mailServiceUri,
                HttpMethod.POST,
                request,
                String.class
        );
    }
}