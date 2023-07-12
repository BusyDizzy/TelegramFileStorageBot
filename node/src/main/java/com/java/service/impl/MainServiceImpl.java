package com.java.service.impl;

import com.java.DTO.JobListingDTO;
import com.java.entity.AppDocument;
import com.java.entity.AppPhoto;
import com.java.entity.AppUser;
import com.java.entity.RawData;
import com.java.exceptions.UploadFileException;
import com.java.repository.AppUserRepository;
import com.java.repository.RawDataRepository;
import com.java.service.*;
import com.java.service.enums.LinkType;
import com.java.service.enums.ServiceCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.java.entity.enums.UserState.BASIC_STATE;
import static com.java.entity.enums.UserState.WAIT_FOR_EMAIL_STATE;
import static com.java.service.enums.ServiceCommand.*;

@Service
@Slf4j
public class MainServiceImpl implements MainService {

    private final RawDataRepository rawDataRepository;
    private final ProducerService service;
    private final AppUserRepository appUserRepository;
    private final FileService fileService;
    private final AppUserService appUserService;

    private final JobService jobService;

    private final Integer USER_JOB_MATCH_RATE = 70;

    private final OpenAIService openAIService;

    public MainServiceImpl(RawDataRepository rawDataRepository,
                           ProducerService service,
                           AppUserRepository appUserRepository,
                           FileService fileService,
                           AppUserService appUserService,
                           JobService jobService,
                           OpenAIService openAIService) {
        this.rawDataRepository = rawDataRepository;
        this.service = service;
        this.appUserRepository = appUserRepository;
        this.fileService = fileService;
        this.appUserService = appUserService;
        this.jobService = jobService;
        this.openAIService = openAIService;
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
            output = appUserService.setEmail(appUser, text);
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
            String link = fileService.generateLink(doc.getId(), LinkType.GET_DOC);
            var answer = "Документ успешно загружен! "
                    + " Ссылка для скачивания: " + link;
            sendAnswer(answer, chatId);
        } catch (UploadFileException exp) {
            log.error(String.valueOf(exp));
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
        try {
            AppPhoto photo = fileService.processPhoto(update.getMessage());
            String link = fileService.generateLink(photo.getId(), LinkType.GET_PHOTO);
            var answer = "Фото успешно загружено! Ссылка для скачивания: " + link;
            sendAnswer(answer, chatId);
        } catch (UploadFileException exp) {
            log.error(String.valueOf(exp));
            String error = "К сожалению, загрузка фото не удалась. Повторите попытку позже";
            sendAnswer(error, chatId);
        }
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
        var serviceCommand = ServiceCommand.fromValue(cmd);
        if (REGISTRATION.equals(serviceCommand)) {
            return appUserService.registerUser(appUser);
        } else if (HELP.equals(serviceCommand)) {
            return help();

        } else if (START.equals(serviceCommand)) {
            return "Приветствую! Для того чтобы начать использовать бот, необходимо набрать: /registration " +
                    "Чтобы посмотреть список доступных команд введите /help";
        } else if (DOWNLOAD_JOBS.equals(serviceCommand) && appUser.getIsActive()) {
            ResponseEntity<JobListingDTO[]> response = jobService.collectJobs(
                    appUser, "Java%20Developer", "Singapore");
            if (response.getStatusCode().name().equals("OK")) {
                return String.format("В базу загружено %d новых вакансий, " +
                        "для просмотра нажмите /show_downloaded", Objects.requireNonNull(response.getBody()).length);
            }
            return "Что-то пошло не так...";
        } else if (SHOW_DOWNLOADED.equals(serviceCommand) && appUser.getIsActive()) {
            return jobService.showDownloadedJobs(appUser);
        } else if (MATCH.equals(serviceCommand)) {
            List<JobListingDTO> filteredJobs = jobService.matchJobs(appUser, USER_JOB_MATCH_RATE);
            if (filteredJobs.size() > 0) {
                return String.format("Отфильтровано %d вакансий, подходящих вам с заданным рейтингом %d" +
                                " Нажмите /show_matched для просмотра результатов.",
                        filteredJobs.size(), USER_JOB_MATCH_RATE);
            } else {
                return "Вообще ничего не подходит, попробуйте понизить планку...";
            }
        } else if (SHOW_MATCHED.equals(serviceCommand) && appUser.getIsActive()) {
            return jobService.showMatchedJobs(appUser);
        } else if (GENERATE_AND_SEND.equals(serviceCommand) && appUser.getIsActive()) {
            return jobService.generateCoversAndSendAsAttachment(appUser);
        } else {
//            Backdoor to talk to ChatGPT
//            return openAIService.chatGPTRequestSessionBased(cmd);
            return "Вы не зарегистрированы! Чтобы начать использование бота зарегистрируйтесь: /registration " +
                    "Иначе вы ввели неизвестную команду! Чтобы посмотреть список доступных команд введите /help " +
                    "или нажмите кнопку меню!";
        }
    }


    private String help() {
        return """
                Список доступных команд:
                /cancel - отмена выполнения текущей команды;
                /registration - регистрация пользователя.""";
    }

    private String cancelProcess(AppUser appUser) {
        appUser.setState(BASIC_STATE);
        appUserRepository.save(appUser);
        return "Команда отменена!";
    }

    private AppUser findOrSaveAppUser(Update update) {
        User telegramUser = update.getMessage().getFrom();
        Optional<AppUser> persistentAppUser = appUserRepository.findByTelegramUserId(telegramUser.getId());
        if (persistentAppUser.isEmpty()) {
            AppUser transientAppUser = AppUser.builder()
                    .telegramUserId(telegramUser.getId())
                    .username(telegramUser.getUserName())
                    .firstName(telegramUser.getFirstName())
                    .lastName(telegramUser.getLastName())
                    .isActive(false)
                    .state(BASIC_STATE)
                    .build();
            return appUserRepository.save(transientAppUser);
        }

        return persistentAppUser.get();
    }

    private void saveRawData(Update update) {
        RawData rawData = RawData.builder()
                .event(update)
                .build();

        rawDataRepository.save(rawData);
    }
}