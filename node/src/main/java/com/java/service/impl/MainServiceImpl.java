package com.java.service.impl;

import com.java.entity.AppDocument;
import com.java.entity.AppUser;
import com.java.entity.RawData;
import com.java.exceptions.UploadFileException;
import com.java.repository.AppUserRepository;
import com.java.repository.RawDataRepository;
import com.java.service.FileService;
import com.java.service.MainService;
import com.java.service.ProducerService;
import com.java.service.enums.ServiceCommand;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import static com.java.entity.enums.UserState.BASIC_STATE;
import static com.java.entity.enums.UserState.WAIT_FOR_EMAIL_STATE;
import static com.java.service.enums.ServiceCommand.*;

@Service
@Log4j
public class MainServiceImpl implements MainService {

    private final RawDataRepository rawDataRepository;
    private final ProducerService service;
    private final AppUserRepository appUserRepository;

    private final FileService fileService;

    public MainServiceImpl(RawDataRepository rawDataRepository,
                           ProducerService service,
                           AppUserRepository appUserRepository,
                           FileService fileService) {
        this.rawDataRepository = rawDataRepository;
        this.service = service;
        this.appUserRepository = appUserRepository;
        this.fileService = fileService;
    }

    @Override
    public void processTextMessage(Update update) {
        saveRawData(update);
        var appUser = findOrSaveAppUser(update);
        var userState = appUser.getState();
        var text = update.getMessage().getText();
        var output = "";

        var serviceCommand = ServiceCommand.fromValue(text);

        if (CANCEL.equals(serviceCommand)) {
            output = cancelProcess(appUser);
        } else if (BASIC_STATE.equals(userState)) {
            output = processServiceCommand(appUser, text);
        } else if (WAIT_FOR_EMAIL_STATE.equals(userState)) {
            // TODO добавить обработку email
        } else {
            log.error("Unknown user state: " + userState);
            output = "Неизвестная ошибка! Введите /cancel и попробуйте снова!";
        }

        var chatId = update.getMessage().getChatId();
        sendAnswer(output, chatId);
    }

    @Override
    public void processDocMessage(Update update) {
        saveRawData(update);
        var appUser = findOrSaveAppUser(update);
        var chatId = update.getMessage().getChatId();

        if (isNotAllowedToSendContent(chatId, appUser)) {
            return;
        }
        try {
            AppDocument doc = fileService.processDoc(update.getMessage());
            //TODO добавить генерацию ссылки для скачивания документа
            var answer = "Документ успешно загружен! "
                    + " Ссылка для скачивания http://test.ry/get-doc/777";
            sendAnswer(answer, chatId);
        } catch (UploadFileException exp) {
            log.error(exp);
            String error = "К сожалению, загрузка файла не удалась. Повторите попытку позже";
            sendAnswer(error, chatId);
        }
    }

    @Override
    public void processPhotoMessage(Update update) {
        saveRawData(update);
        var appUser = findOrSaveAppUser(update);
        var chatId = update.getMessage().getChatId();

        if (isNotAllowedToSendContent(chatId, appUser)) {
            return;
        }

        //TODO добавить сохранение фото
        var answer = "Фото успешно загружено! Ссылка для скачивания http://test.ry/get-photo/777";
        sendAnswer(answer, chatId);
    }

    private boolean isNotAllowedToSendContent(Long chatId, AppUser appUser) {
        var userState = appUser.getState();
        if (!appUser.getIsActive()) {
            var error = "Зарегистрируйтесь или активируйте свою учетную запись для загрузки контента.";
            sendAnswer(error, chatId);
            return true;
        } else if (!BASIC_STATE.equals(userState)) {
            var error = "Отмените текущую команду с помощью /cancel для отправки файлов";
            sendAnswer(error, chatId);
            return true;
        }
        return false;
    }

    private void sendAnswer(String output, Long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(output);
        service.produceAnswer(sendMessage);
    }

    private String processServiceCommand(AppUser appUser, String cmd) {
        if (REGISTRATION.equals(cmd)) {
            // TODO добавить регистрацию
            return "Временно недоступно";
        } else if (HELP.equals(cmd)) {
            return help();
        } else if (START.equals(cmd)) {
            return "Приветствую! Чтобы посмотреть список доступных команд введите /help";
        } else {
            return "Неизвестная команда! Чтобы посмотреть список доступных команд введите /help";
        }
    }

    private String help() {
        return "Список доступных команд:\n"
                + "/cancel - отмена выполнения текущей команды;\n"
                + "/registration - регистрация пользователя.";
    }

    private String cancelProcess(AppUser appUser) {
        appUser.setState(BASIC_STATE);
        appUserRepository.save(appUser);
        return "Команда отменена!";
    }

    private AppUser findOrSaveAppUser(Update update) {
        User telegramUser = update.getMessage().getFrom();
        AppUser persistentAppUser = appUserRepository.findAppUserByTelegramUserId(telegramUser.getId());
        if (persistentAppUser == null) {
            AppUser transientAppUser = AppUser.builder()
                    .telegramUserId(telegramUser.getId())
                    .username(telegramUser.getUserName())
                    .firstName(telegramUser.getFirstName())
                    .lastName(telegramUser.getLastName())
                    //TODO изменить значение по умолчанию после добавления регистрации
                    .isActive(true)
                    .state(BASIC_STATE)
                    .build();
            return appUserRepository.save(transientAppUser);
        }

        return persistentAppUser;
    }

    private void saveRawData(Update update) {
        RawData rawData = RawData.builder()
                .event(update)
                .build();

        rawDataRepository.save(rawData);
    }
}