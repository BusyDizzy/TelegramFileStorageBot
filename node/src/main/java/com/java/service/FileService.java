package com.java.service;

import com.java.entity.AppDocument;
import com.java.entity.AppUser;
import com.java.service.enums.LinkType;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface FileService {
    AppDocument processDoc(Message telegramMessage, AppUser appUser);

    String generateLink(Long docId, LinkType linkType);
}