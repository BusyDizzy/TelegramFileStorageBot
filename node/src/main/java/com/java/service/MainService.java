package com.java.service;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface MainService {
    boolean processTextMessage(Update update);
    boolean processDocMessage(Update update);
}