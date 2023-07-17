package com.java.service.impl;

import com.java.DTO.CurriculumVitaeDTO;
import com.java.DTO.JobListingDTO;
import com.java.client.JobClient;
import com.java.entity.AppUser;
import com.java.entity.ChatGPTPrompt;
import com.java.entity.Pair;
import com.java.entity.enums.JobMatchState;
import com.java.entity.enums.PromptType;
import com.java.repository.ChatGPTPromptsRepository;
import com.java.repository.CurriculumVitaeDTORepository;
import com.java.repository.JobListingDTORepository;
import com.java.repository.JobListingRepository;
import com.java.service.AppUserService;
import com.java.service.JobService;
import com.java.service.OpenAIService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Service
public class JobServiceImpl implements JobService {
    private final OpenAIService openAIService;
    private final JobListingRepository jobListingRepository;
    private final JobListingDTORepository jobListingDTORepository;
    private final CurriculumVitaeDTORepository curriculumVitaeRepository;
    private static final String EMAIL_PREFIX = "На вашу почту %s были отправлены письма на следующие вакансии: \n";
    private final AppUserService appUserService;
    private final ChatGPTPromptsRepository promptsRepository;
    private List<JobListingDTO> currentDownloadedJobsList;
    private final JobClient jobClient;

    public JobServiceImpl(OpenAIServiceImpl openAIServiceImpl,
                          JobListingRepository jobListingRepository,
                          JobListingDTORepository jobListingDTORepository,
                          CurriculumVitaeDTORepository curriculumVitaeRepository,
                          AppUserService appUserService, ChatGPTPromptsRepository promptsRepository, JobClient jobClient) {
        this.openAIService = openAIServiceImpl;
        this.jobListingRepository = jobListingRepository;
        this.jobListingDTORepository = jobListingDTORepository;
        this.curriculumVitaeRepository = curriculumVitaeRepository;
        this.appUserService = appUserService;
        this.promptsRepository = promptsRepository;
        this.jobClient = jobClient;
    }

    public ResponseEntity<JobListingDTO[]> collectJobs(AppUser appUser, String query, String location) {
        ResponseEntity<JobListingDTO[]> responseEntity = jobClient.fetchJobs(appUser.getId(), query, location);
        currentDownloadedJobsList = Arrays.asList(Objects.requireNonNull(responseEntity.getBody()));
        return responseEntity;
    }

    public String matchJobs(AppUser appUser, int matchPercentage) {
        String answer;
        int counter = 0;
        List<JobListingDTO> jobListingDTOS = jobListingDTORepository.findByUserId(appUser.getId());
        ChatGPTPrompt prompt = promptsRepository.findByPromptType(1L, PromptType.MATCH_CURRICULUM_VITAE);
        String PROMPT_MESSAGE_FOR_JOB_MATCHER = prompt.getPrompt();
        double userMatchValue = matchPercentage / 100.0;
        List<JobListingDTO> filteredJobs = new ArrayList<>();
        log.info("Начинаем анализ {} вакансий с заданным порогом совпадения {} %", jobListingDTOS.size(), matchPercentage);
        Optional<CurriculumVitaeDTO> curriculumVitae = curriculumVitaeRepository.findByUserId(appUser.getId());
        for (JobListingDTO job : jobListingDTOS) {
            StringBuilder promptToCalculateJobMatchingRate = new StringBuilder();
            if (curriculumVitae.isPresent()) {
                buildPromptForChatGPTForUserData(promptToCalculateJobMatchingRate,
                        PROMPT_MESSAGE_FOR_JOB_MATCHER,
                        curriculumVitae.get());
            } else {
                return "Пожалуйста загрузите резюме для начала использования функционала /upload_resume ";
            }
            buildPromptForChatGPTForJobListingDTO(promptToCalculateJobMatchingRate, job);
            if (job.getJobMatchState().equals(JobMatchState.NOT_EVALUATED)) {
                try {
                    Double jobMatchValue = openAIService.calculateMatchRateForCvAndJob(promptToCalculateJobMatchingRate.toString());
                    counter++;
                    if (jobMatchValue >= userMatchValue) {
                        job.setJobMatchState(JobMatchState.MATCH);
                        filteredJobs.add(job);
                    } else {
                        job.setJobMatchState(JobMatchState.NOT_MATCH);
                    }
                } catch (NumberFormatException e) {
                    log.error("Error occurred while calculating match rate for job: {}", job.getId());
                    // Handle the error accordingly
                } catch (Exception e) {
                    log.error("Error occurred while calculating match rate for job: {}", job.getId(), e);
                    // Handle the error accordingly
                }
                jobListingDTORepository.save(job);
            }
        }
        if (counter > 0 && filteredJobs.isEmpty()) {
            answer = String.format("К сожалению на загруженные вакансии" +
                    " с заданным порогом совпадения %d ничего не нашлось", matchPercentage);
        } else if (counter == 0) {
            answer = "В безе нет необработанных вакансий. Загрузите новые вакансии /download_jobs";
        } else {
            log.info("Подобрано {} вакансий с заданным порогом совпадения {} % " +
                    "Нажмите /show_matched для просмотра результатов", filteredJobs.size(), matchPercentage);
            answer = String.format("Подобрано %d вакансий с заданным порогом совпадения %d",
                    filteredJobs.size(), matchPercentage);
        }
        return answer;
    }

    @Override
    public String generateCoversAndSendAsAttachment(AppUser appUser) {
        List<String> jobs = new ArrayList<>();
        jobs.add(String.format(EMAIL_PREFIX, appUser.getEmail()));
        List<JobListingDTO> jobList = jobListingDTORepository.findByUserIdMatchedAndCoverNotSend(appUser.getId(),
                JobMatchState.MATCH, false);
        AtomicInteger id = new AtomicInteger(1);
        if (jobList.isEmpty()) {
            return "Похоже у вас еще нет совпадений либо вы не загрузили вакансии " +
                    "Для поиска подходящих вакансий нажмите /match " +
                    "Для загрузки /download_jobs";
        }
        for (JobListingDTO job : jobList) {
            String messageString = String.format("Company: %s, Job Title: %s,  \n\n",
                    job.getCompanyName(), job.getJobTitle());
            jobs.add(messageString);
        }
        log.info("Job list prepared, generating cover letters");
        // Generate Multiple Cover Letters
        CompletableFuture<List<Pair<String, String>>> listCompletableFuture = generateCoverLetters(appUser, jobList);

        String jobsInfo = jobList.stream()
                .map(job -> String.format("Job number: %d\n Location: %s\n Job Title: %s\n" +
                                " Company: %s\n Source: %s\n URL: %s\n Cover Letter Filename: %s.txt\n\n",
                        id.getAndIncrement(), job.getLocation(), job.getJobTitle(), job.getCompanyName(),
                        job.getSourceWebsite(), job.getUrl(), generateCoverLetterFilename(job)))
                .collect(Collectors.joining());

        // If generated try to send them all in one email as attachments
        try {
            assert listCompletableFuture != null;
            appUserService.sendMultipleCoverLetters(appUser, listCompletableFuture.get(), jobsInfo);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return String.join("", jobs);
    }

    private CompletableFuture<List<Pair<String, String>>> generateCoverLetters(AppUser appUser, List<JobListingDTO> jobList) {
        ChatGPTPrompt prompt = promptsRepository.findByPromptType(1L, PromptType.GENERATE_COVER_LETTER);
        String PROMPT_MESSAGE_FOR_COVER_GENERATION = prompt.getPrompt();
        Optional<CurriculumVitaeDTO> curriculumVitae = curriculumVitaeRepository.findByUserId(appUser.getId());
        List<CompletableFuture<Pair<String, String>>> coverLetterFutures = new ArrayList<>();

        for (JobListingDTO job : jobList) {
            StringBuilder promptToGenerateCoverLetter = new StringBuilder();
            if (curriculumVitae.isPresent()) {
                buildPromptForChatGPTForUserData(promptToGenerateCoverLetter,
                        PROMPT_MESSAGE_FOR_COVER_GENERATION,
                        curriculumVitae.get());
            } else {
                return CompletableFuture.completedFuture(Collections.emptyList());
            }
            buildPromptForChatGPTForJobListingDTO(promptToGenerateCoverLetter, job);

            String filename = generateCoverLetterFilename(job);

            if (!job.getIsCoverSend()) {
                CompletableFuture<Pair<String, String>> coverLetterFuture = openAIService
                        .chatGPTRequestMemoryLess(promptToGenerateCoverLetter.toString())
                        .thenApply(content -> new Pair<>(content, filename));

                job.setIsCoverSend(true);
                jobListingDTORepository.save(job);

                coverLetterFutures.add(coverLetterFuture);
            }
        }
        log.info("Joining all generated cover letters");
        // Join all the futures into a single CompletableFuture that completes when all cover letters are generated
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(coverLetterFutures.toArray(new CompletableFuture[0]));

        return allFutures.thenApply(v -> coverLetterFutures.stream()
                .map(CompletableFuture::join)   // join each future (blocking operation, but they should all be completed already because of the `allOf`)
                .collect(Collectors.toList()));
    }

    public String showDownloadedJobs(AppUser appUser) {
        List<String> jobs = new ArrayList<>();
        List<JobListingDTO> jobList = jobListingDTORepository.findByUserId(appUser.getId());
        if (jobList.size() > 0) {
            prepareJobListStingForTelegram(jobs, jobList);
        } else {
            return "В вашей базе пока нет скаченных вакансий";
        }
        return String.join("", jobs);
    }

    public String showMatchedJobs(AppUser appUser) {
        List<String> jobs = new ArrayList<>();
        List<JobListingDTO> jobList = jobListingDTORepository.findByUserIdMatched(appUser.getId(), JobMatchState.MATCH);
        prepareJobListStingForTelegram(jobs, jobList);
        return String.join("", jobs);
    }

    private void prepareJobListStingForTelegram(List<String> jobs, List<JobListingDTO> jobList) {
        if (jobList.size() > 0) {
            for (JobListingDTO job : jobList) {
                String answerForMatch = "No";
                String answerForCover = "No";
                if (job.getJobMatchState().equals(JobMatchState.NOT_EVALUATED)) {
                    answerForMatch = "Not evaluated yet";
                } else if (job.getJobMatchState().equals(JobMatchState.MATCH)) {
                    answerForMatch = "It's a Match! ;)";
                }
                if (job.getIsCoverSend()) {
                    answerForCover = "Yes";
                }
                String messageString = String.format(
                        """
                                Location: %s\s
                                Company: %s\s
                                Job Title: %s\s
                                Match: %s\s
                                Cover sent: %s\s
                                Website: %s\s
                                Link: %s\s
                                \s
                                """,
                        job.getLocation(),
                        job.getCompanyName(),
                        job.getJobTitle(),
                        answerForMatch,
                        answerForCover,
                        job.getSourceWebsite(),
                        job.getUrl());
                jobs.add(messageString);
            }
        }
    }

    private String generateCoverLetterFilename(JobListingDTO job) {
        return String.format("%s_%s_%s", job.getJobTitle(), job.getCompanyName(), job.getLocation());

    }

    private void buildPromptForChatGPTForUserData(StringBuilder promptToGenerateCoverLetter, String initializationPrompt,
                                                  CurriculumVitaeDTO curriculumVitae) {
        promptToGenerateCoverLetter
                .append(initializationPrompt).append("Here goes my CV: ")
                .append(curriculumVitae.getCvDescription());
    }

    private void buildPromptForChatGPTForJobListingDTO(StringBuilder jobDescription, JobListingDTO job) {
        jobDescription
                .append("Here is the job description: ").append(" ")
                .append(job.getCompanyName()).append(" ")
                .append(job.getJobTitle()).append(" ")
                .append(job.getJobDescription());
    }
}