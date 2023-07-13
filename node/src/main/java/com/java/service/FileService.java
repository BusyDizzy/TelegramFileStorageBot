package com.java.service;

import com.java.entity.AppDocument;
import com.java.entity.AppPhoto;
import com.java.entity.AppUser;
import com.java.service.enums.LinkType;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface FileService {
    AppDocument processDoc(Message telegramMessage, AppUser appUser);

    AppPhoto processPhoto(Message telegramMessage);

    String generateLink(Long docId, LinkType linkType);
}