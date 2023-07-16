package com.java.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;


@Component
@Slf4j
public class TelegramBot extends TelegramWebhookBot {

    @Value("${bot.name}")
    private String botName;

    @Value("${bot.token}")
    private String botToken;

    @Value("${bot.uri}")
    private String botUri;

    private final UpdateProcessor updateController;

    public TelegramBot(UpdateProcessor updateProcessor) {
        this.updateController = updateProcessor;

    }


    @PostConstruct
    public void init() {
        updateController.registerBot(this);
        try {
            var setWebhook = SetWebhook.builder()
                    .url(botUri)
                    .build();
            this.setWebhook(setWebhook);
            List<BotCommand> menu = new ArrayList<>();
            menu.add(new BotCommand("/start", "Welcome message"));
            menu.add(new BotCommand("/registration", "Register to start"));
            menu.add(new BotCommand("/upload_resume", "Upload your cv"));
            menu.add(new BotCommand("/download_jobs", "Download LinkedIn jobs"));
            menu.add(new BotCommand("/show_downloaded_jobs", "Show all jobs "));
            menu.add(new BotCommand("/match", "Match you CV to downloaded jobs"));
            menu.add(new BotCommand("/show_matched", "Shows jobs that match"));
            menu.add(new BotCommand("/generate", "Generate covers and email them"));
            menu.add(new BotCommand("/improve", "Suggest improvements for CV"));
            menu.add(new BotCommand("/update", "Upload updated cv"));
            menu.add(new BotCommand("/help", "Info how to use this bot"));
            menu.add(new BotCommand("/cancel", "Cancel your request"));
            this.execute(new SetMyCommands(menu, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error(String.valueOf(e));
        }
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    public void sendAnswerMessage(SendMessage message) {
        if (message != null) {
            try {
                execute(message);
            } catch (TelegramApiException e) {
                log.error(String.valueOf(e));
            }
        }
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        return null;
    }

    @Override
    public String getBotPath() {
        return "/update";
    }
}
