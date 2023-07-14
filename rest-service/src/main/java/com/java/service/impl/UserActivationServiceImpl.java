package com.java.service.impl;

import com.java.repository.AppUserRepository;
import com.java.service.UserActivationService;
import com.java.utils.CryptoTool;
import org.springframework.stereotype.Service;

@Service
public class UserActivationServiceImpl implements UserActivationService {

    private final AppUserRepository appUserRepository;
    private final CryptoTool cryptoTool;

    public UserActivationServiceImpl(AppUserRepository appUserRepository, CryptoTool cryptoTool) {
        this.appUserRepository = appUserRepository;
        this.cryptoTool = cryptoTool;
    }

    @Override
    public boolean activation(String cryptoUserId) {
        var userId = cryptoTool.idOf(cryptoUserId);
        var optional = appUserRepository.findById(userId);
        if (optional.isPresent()) {
            var user = optional.get();
            user.setIsActive(true);
            user.setIsCvUploaded(false);
            appUserRepository.save(user);
            return true;
        }
        return false;
    }
}