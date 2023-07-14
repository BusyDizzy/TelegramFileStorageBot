package com.java.service.impl;

import com.java.DTO.JobListingDTO;
import com.java.entity.AppDocument;
import com.java.entity.AppPhoto;
import com.java.entity.AppUser;
import com.java.entity.RawData;
import com.java.entity.enums.UserState;
import com.java.exceptions.UploadFileException;
import com.java.repository.AppUserRepository;
import com.java.repository.RawDataRepository;
import com.java.service.*;
import com.java.service.enums.LinkType;
import com.java.service.enums.ServiceCommand;
import com.java.service.fetching.LinkedInLocationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.java.entity.enums.UserState.*;

@Service
@Slf4j
public class MainServiceImpl implements MainService {

    private final RawDataRepository rawDataRepository;
    private final ProducerService service;
    private final AppUserRepository appUserRepository;
    private final FileService fileService;
    private final AppUserService appUserService;

    private final JobService jobService;

    private final LinkedInLocationService linkedInLocationService;

    private static String SEARCH_QUERY = null;

    private static String LOCATION = null;

    private static final int CHUNK_SIZE = 4095;

    private final Integer USER_JOB_MATCH_RATE = 70;


    private final OpenAIService openAIService;

    public MainServiceImpl(RawDataRepository rawDataRepository,
                           ProducerService service,
                           AppUserRepository appUserRepository,
                           FileService fileService,
                           AppUserService appUserService,
                           JobService jobService,
                           LinkedInLocationService linkedInLocationService, OpenAIService openAIService) {
        this.rawDataRepository = rawDataRepository;
        this.service = service;
        this.appUserRepository = appUserRepository;
        this.fileService = fileService;
        this.appUserService = appUserService;
        this.jobService = jobService;
        this.linkedInLocationService = linkedInLocationService;
        this.openAIService = openAIService;
    }

    @Override
    public void processTextMessage(Update update) {
        saveRawData(update);
        var appUser = findOrSaveAppUser(update);
        UserState userState = appUser.getState();
        var chatId = update.getMessage().getChatId();
        var text = update.getMessage().getText();
        ServiceCommand serviceCommand = ServiceCommand.fromValue(text);

        String output;

        if (serviceCommand == ServiceCommand.CANCEL) {
            output = cancelProcess(appUser);
        } else {
            switch (userState) {
                case BASIC_STATE, SEARCH_STRING_READY -> output = processServiceCommand(appUser, text);
                case WAIT_FOR_SEARCH_INPUT_QUERY -> {
                    if (text.length() > 0) {
                        SEARCH_QUERY = text.replace(" ", "%20");
                    } else {
                        SEARCH_QUERY = text;
                    }
                    appUser.setState(WAIT_FOR_SEARCH_INPUT_LOCATION);
                    appUserRepository.save(appUser);
                    output = "Введите регион поиска";
                }
                case WAIT_FOR_SEARCH_INPUT_LOCATION -> {
                    LOCATION = text;
                    // Checking if LinkedIn geoId is supported (stored in db)
                    if (linkedInLocationService.getGeoIdByLocationName(text).isEmpty()) {
                        output = "Поиск по данному региону пока не поддерживается. " +
                                "Просьба связаться с разработчиком: anton.tk@gmail.com";
                        appUser.setState(BASIC_STATE);
                        appUserRepository.save(appUser);
                        break;
                    }
                    appUser.setState(SEARCH_STRING_READY);
                    appUserRepository.save(appUser);
                    output = "Теперь нажмите /download";
                }
                case WAIT_FOR_EMAIL_STATE -> output = appUserService.setEmail(appUser, text);
                case WAIT_FOR_CV -> output = "Загрузите ваше резюме в Телеграм Бот в формате docx, docs, txt. " +
                        "Для отмены введите /cancel ";
                default -> {
                    log.error("Unknown user state: " + userState);
                    output = "Неизвестная ошибка! Введите /cancel и попробуйте снова!";
                }
            }
        }
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
        appUser.setState(BASIC_STATE);
        appUserRepository.save(appUser);
        try {
            AppDocument doc = fileService.processDoc(update.getMessage(), appUser);
//              String link = fileService.generateLink(doc.getId(), LinkType.GET_DOC);
//            var answer = "Документ успешно загружен! "
//                    + " Ссылка для скачивания: " + link;
            var answer = "Ваше резюме успешно загружено, теперь, можете перейти к следующему шагу" +
                    " и скачать вакансии /download_jobs";
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
        } else if (appUser.getIsCvUploaded()) {
            var error = "You have already uploaded cv. To update click: /update_resume";
//            var error = "Отмените текущую команду с помощью /cancel для отправки файлов";
            sendAnswer(error, chatId);
            return true;
        }
        return false;
    }

    private void sendAnswer(String output, Long chatId) {
        int length = output.length();

        for (int i = 0; i < length; i += CHUNK_SIZE) {
            // Determine end index for substring
            int endIndex = Math.min(i + CHUNK_SIZE, length);

            // Get the chunk
            String chunk = output.substring(i, endIndex);

            // Create message with chunk and chatId
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chatId);
            sendMessage.setText(chunk);

            // Send to the service
            service.produceAnswer(sendMessage);
        }
    }

    private String processServiceCommand(AppUser appUser, String cmd) {
        var serviceCommand = ServiceCommand.fromValue(cmd);
        assert serviceCommand != null;
        return switch (serviceCommand) {
            case REGISTRATION -> appUserService.registerUser(appUser);
            case HELP -> help();
            case UPLOAD_CV -> {
                if (!appUser.getIsCvUploaded()) {
                    appUser.setState(WAIT_FOR_CV);
                    appUserRepository.save(appUser);
                    yield "Загрузите ваше резюме в формате docx, doc, txt в Телеграм бот";
                }
                yield "Вы уже загрузили одно резюме. Теперь можно только обновить: /update";
            }
            case START ->
                    "Приветствую! Это Бот на базе AI ChatGPT. Он собирает вакансии, фильтрует те, которые подходят" +
                            " под ваше резюме, пишет сопроводительные письма под вакансии, которые подошли вам, " +
                            "а также, дает рекомендации, на основе найденных вакансий, что можно улучшить в вашем резюме." +
                            " Для того чтобы начать использовать бот, необходимо регистрация: /registration " +
                            " Чтобы посмотреть список доступных команд введите /help";
            case DOWNLOAD_JOBS -> {
                if (appUser.getIsActive()) {
                    if (appUser.getState().equals(BASIC_STATE) || appUser.getState().equals(CV_UPLOADED)) {
                        appUser.setState(WAIT_FOR_SEARCH_INPUT_QUERY);
                        appUserRepository.save(appUser);
                        yield "Введите название вакансии";
                    } else if (appUser.getState().equals(SEARCH_STRING_READY)) {
                        appUser.setState(BASIC_STATE);
                        appUserRepository.save(appUser);
                        ResponseEntity<JobListingDTO[]> response = jobService.collectJobs(
                                appUser, SEARCH_QUERY, LOCATION);
                        if (response.getStatusCode().name().equals("OK")) {
                            yield String.format("В базу загружено %d новых вакансий, " +
                                    "для просмотра нажмите /show_downloaded", Objects.requireNonNull(response.getBody()).length);
                        }
                        yield "Что-то пошло не так...";
                    }
                }
                yield defaultResponse();
            }
            case SHOW_DOWNLOADED -> appUser.getIsActive() ? jobService.showDownloadedJobs(appUser) : defaultResponse();
            case MATCH -> {
                if (appUser.getIsCvUploaded()) {
                    List<JobListingDTO> filteredJobs = jobService.matchJobs(appUser, USER_JOB_MATCH_RATE);
                    if (filteredJobs.size() > 0) {
                        yield String.format("Отфильтровано %d вакансий, подходящих вам с заданным рейтингом %d" +
                                        " Нажмите /show_matched для просмотра результатов.",
                                filteredJobs.size(), USER_JOB_MATCH_RATE);
                    }
                    yield "Вообще ничего не подходит, попробуйте понизить планку...";
                } else {
                    yield "Для начала работы с функционалом сравнения загрузите резюме /upload_resume";
                }
            }
            case SHOW_MATCHED -> appUser.getIsActive() ? jobService.showMatchedJobs(appUser) : defaultResponse();
            case GENERATE_AND_SEND -> {
                if (appUser.getIsCvUploaded() && appUser.getIsActive()) {
                    jobService.generateCoversAndSendAsAttachment(appUser);
                } else {
                    yield "Для начала работы с функционалом генерации сопроводительных писем" +
                            " загрузите резюме /upload_resume";
                }
                yield "Какая-то оказия случилась...";
            }
            case UPDATE, SUGGEST_IMPROVE -> underConstruction();
            default -> defaultResponse();
        };
    }

    private String underConstruction() {
        return "Under construction";
    }

    private String defaultResponse() {
        //  Backdoor to talk to ChatGPT
//              return openAIService.chatGPTRequestSessionBased(cmd);
        return "Вы не зарегистрированы! Чтобы начать использование бота зарегистрируйтесь: /registration " +
                "Иначе вы ввели неизвестную команду! Чтобы посмотреть список доступных команд введите /help " +
                "или нажмите кнопку меню!";
    }

    private String help() {
        return """
                Для начала работы с сервисом по подбору вакансий и генерации сопроводительных писем на базе ИИ,
                нажмите: /registration и зарегистрируйтесь введя свой email.
                """;
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