package com.java.service.impl;

import com.java.DTO.JobListingDTO;
import com.java.entity.AppDocument;
import com.java.entity.AppUser;
import com.java.entity.RawData;
import com.java.entity.enums.Role;
import com.java.entity.enums.UserState;
import com.java.exception.ChatGPTException;
import com.java.exceptions.UploadFileException;
import com.java.repository.AppUserRepository;
import com.java.repository.RawDataRepository;
import com.java.service.*;
import com.java.service.enums.ServiceCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

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

    private final LinkedInLocationService linkedInLocationService;

    private final JobService jobService;

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
                           LinkedInLocationService linkedInLocationService, JobService jobService,
                           OpenAIService openAIService) {
        this.rawDataRepository = rawDataRepository;
        this.service = service;
        this.appUserRepository = appUserRepository;
        this.fileService = fileService;
        this.appUserService = appUserService;
        this.linkedInLocationService = linkedInLocationService;
        this.jobService = jobService;
        this.openAIService = openAIService;
    }

    @Override
    public boolean processTextMessage(Update update) {
        saveRawData(update);
        var appUser = findOrSaveAppUser(update);
        UserState userState = appUser.getState();
        var chatId = update.getMessage().getChatId();
        var text = update.getMessage().getText();
        ServiceCommand serviceCommand = ServiceCommand.fromValue(text);
        String output;
        try {
            if (serviceCommand == ServiceCommand.CANCEL) {
                output = cancelProcess(appUser);
            } else {
                switch (userState) {
                    case BASIC_STATE, SEARCH_STRING_READY -> output = processServiceCommand(appUser, text, chatId);
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
                                    "Просьба связаться с разработчиком: @AntonTkatch";
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
                            "Для отмены загрузки резюме введите /cancel ";
                    default -> {
                        log.error("Unknown user state: " + userState);
                        output = "Неизвестная ошибка! Введите /cancel и попробуйте снова!";
                    }
                }
            }
            sendAnswer(output, chatId);
            return true;
        } catch (ChatGPTException e) {
            log.error("ChatGPTException occurred while processing text message {}", e.getMessage());
            // Do specific handling for ChatGPTException here
            output = "Ошибка во время обработки сообщения в ChatGPT, обратитесь к разработчику TG: @AntonTkatch";
            sendAnswer(output, chatId);
            return false;
        } catch (Exception e) {
            log.error("Error processing text message {}", e.getMessage());
            output = "Ошибка во время обработки команды, обратитесь к разработчику TG: @AntonTkatch";
            sendAnswer(output, chatId);
            return false;
        }
    }

    @Override
    public boolean processDocMessage(Update update) {
        saveRawData(update);
        var appUser = findOrSaveAppUser(update);
        var chatId = update.getMessage().getChatId();
        try {
            if (isNotAllowedToSendContent(chatId, appUser)) {
                return false;
            }

            appUser.setState(BASIC_STATE);
            appUserRepository.save(appUser);
            try {
                AppDocument doc = fileService.processDoc(update.getMessage(), appUser);
//              String link = fileService.generateLink(doc.getId(), LinkType.GET_DOC);
//            var answer = "Документ успешно загружен! "
//                    + " Ссылка для скачивания: " + link;
                if (doc != null) {
                    var answer = "Ваше резюме успешно загружено, теперь, можете перейти к следующему шагу" +
                            " и скачать вакансии /download_jobs";
                    sendAnswer(answer, chatId);
                    return true;
                }
                // TODO Поправить метод
                else {
                    throw new UploadFileException("Something went wrong");
                }
            } catch (UploadFileException exp) {
                log.error(String.valueOf(exp));
                String error = "К сожалению, загрузка файла не удалась. Повторите попытку позже";
                sendAnswer(error, chatId);
                return false;
            }
        } catch (Exception e) {
            String error = "К сожалению, произошла ошибка, обратитесь к разработчику TG: @AntonTkatch";
            sendAnswer(error, chatId);
            return false;
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

    private String processServiceCommand(AppUser appUser, String cmd, Long chatId) {
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
            case START -> "Вас приветствует AI CareerBooster! Telegram Bot с встроенным ИИ для:\n" +
                    "\n" +
                    " - Сбора подходящих вакансий по вашему резюме\n" +
                    " - Написания и отправки на почту сопроводительных писем для выбранных вакансий\n" +
                    " - Рекомендаций по улучшению резюме на основе найденных вакансий\n" +
                    "\n" +
                    "Для начала работы регистрируйтесь: /registration. ";
            case DOWNLOAD_JOBS -> {
                if (appUser.getIsActive()) {
                    if (appUser.getState().equals(BASIC_STATE) || appUser.getState().equals(CV_UPLOADED)) {
                        appUser.setState(WAIT_FOR_SEARCH_INPUT_QUERY);
                        appUserRepository.save(appUser);
                        yield "Введите название вакансии";
                    } else if (appUser.getState().equals(SEARCH_STRING_READY)) {
                        sendAnswer("Загрузка началась, это может занять некоторое время...", chatId);
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
            case MATCH ->
                    appUser.getIsActive() ? jobService.matchJobs(appUser, USER_JOB_MATCH_RATE) : defaultResponse();
            case SHOW_MATCHED -> appUser.getIsActive() ? jobService.showMatchedJobs(appUser) : defaultResponse();
            case GENERATE_AND_SEND -> {
                if (appUser.getIsCvUploaded() && appUser.getIsActive()) {
                    yield jobService.generateCoversAndSendAsAttachment(appUser);
                } else {
                    yield "Для начала работы с функционалом генерации сопроводительных писем" +
                            " загрузите резюме /upload_resume";
                }
            }
            case HIDDEN -> appUser.getRoles().contains(Role.ADMIN) ? "Hi from Admin" : defaultResponse();
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