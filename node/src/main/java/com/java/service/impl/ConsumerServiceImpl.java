package com.java.service.impl;

import com.java.service.ConsumerService;
import com.java.service.MainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import static com.java.model.RabbitQueue.DOC_MESSAGE_UPDATE;
import static com.java.model.RabbitQueue.TEXT_MESSAGE_UPDATE;

@Service
@Slf4j
public class ConsumerServiceImpl implements ConsumerService {

    private final MainService service;

    public ConsumerServiceImpl(MainService service) {
        this.service = service;
    }

    @Override
    @RabbitListener(queues = TEXT_MESSAGE_UPDATE)
    public void consumeTextMessageUpdates(Update update) {
        log.debug("NODE: Text message is received");
        boolean isProcessingSuccessful = service.processTextMessage(update);
        if (!isProcessingSuccessful) {
            log.error("Error while processing incoming message");
            throw new AmqpRejectAndDontRequeueException("Processing of text message failed");
        }
    }

    @Override
    @RabbitListener(queues = DOC_MESSAGE_UPDATE)
    public void consumeDocMessageUpdates(Update update) {
        log.debug("NODE: Document is received");
        boolean isProcessingSuccessful = service.processDocMessage(update);
        if (!isProcessingSuccessful) {
            log.error("Error while processing incoming message");
            throw new AmqpRejectAndDontRequeueException("Processing of text message failed");
        }
    }
}