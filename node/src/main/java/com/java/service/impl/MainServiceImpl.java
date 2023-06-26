package com.java.service.impl;

import com.java.entity.RawData;
import com.java.repository.RawDataRepository;
import com.java.service.MainService;
import com.java.service.ProducerService;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
public class MainServiceImpl implements MainService {

    private final RawDataRepository repository;

    private final ProducerService service;

    public MainServiceImpl(RawDataRepository repository, ProducerService service) {
        this.repository = repository;
        this.service = service;
    }

    @Override
    public void processTextMessage(Update update) {
        saveRawData(update);

        var message = update.getMessage();
        var sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setText("Hello from NODE");
        service.produceAnswer(sendMessage);
    }

    private void saveRawData(Update update) {
        RawData rawData = RawData.builder()
                .event(update)
                .build();

        repository.save(rawData);
    }
}