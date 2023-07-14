package com.java.service.impl;

import com.java.DTO.CurriculumVitaeDTO;
import com.java.DTO.JobListingDTO;
import com.java.client.JobClient;
import com.java.entity.AppUser;
import com.java.entity.JobListing;
import com.java.entity.enums.JobMatchState;
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
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Slf4j
@Service
public class JobServiceImpl implements JobService {


    //    private final AsyncTaskExecutor taskExecutor;
    private final OpenAIService openAIService;
    private final JobListingRepository jobListingRepository;

    private final JobListingDTORepository jobListingDTORepository;

    private final CurriculumVitaeDTORepository curriculumVitaeRepository;

    private static final String EMAIL_PREFIX = "На вашу почту %s были отправлены письма на следующие вакансии: \n";
    private static final String PROMPT_MESSAGE_FOR_COVER_GENERATION = "Accumulate my CV first. " +
            "Than generate cover letter for the position below based on my CV. Do not pour water, " +
            "be specific, provide three bullet points at once, why I am suitable for this job, the fourth point" +
            " should be why I want to work in this company, generate something human: for example, this is " +
            "my first bank in this country or I used the services of this company for several years and etc. " +
            "If its recruiting agency skip this part. If there are signs that company is proud to work on " +
            "a local market, tell about my desire to be a part of this country (where job is applicable). " +
            "Provide at the beginning several reasons why you would like to work on this company, " +
            "and then why I am a good fit. Use and apply The F-Shaped pattern for reading. Here goes my CV:";

    private static final String PROMPT_MESSAGE_FOR_JOB_MATCHER = "I will provide CV and job description after that." +
            "Calculate the fit rate in double from 0 to 1 how my CV relates to the job description below and return " +
            " If in the job description years of experience are more than my years of experience set score immediately 0 " +
            "without further calculations" +
            " In response I expect only one digit from 0 to 1 without any explanations. ";
    private final AppUserService appUserService;

    private List<JobListingDTO> currentDownloadedJobsList;

    private final JobClient jobClient;

    public JobServiceImpl(OpenAIServiceImpl openAIServiceImpl,
                          JobListingRepository jobListingRepository,
                          JobListingDTORepository jobListingDTORepository,
                          CurriculumVitaeDTORepository curriculumVitaeRepository,
                          AppUserService appUserService, JobClient jobClient) {
        this.openAIService = openAIServiceImpl;
        this.jobListingRepository = jobListingRepository;
        this.jobListingDTORepository = jobListingDTORepository;
        this.curriculumVitaeRepository = curriculumVitaeRepository;
        this.appUserService = appUserService;
        this.jobClient = jobClient;
    }

    public ResponseEntity<JobListingDTO[]> collectJobs(AppUser appUser, String query, String location) {
        ResponseEntity<JobListingDTO[]> responseEntity = jobClient.fetchJobs(appUser.getId(), query, location);
        currentDownloadedJobsList = Arrays.asList(Objects.requireNonNull(responseEntity.getBody()));
        return responseEntity;
    }

    public List<JobListingDTO> matchJobs(AppUser appUser, int matchPercentage) {
        List<JobListingDTO> jobListingDTOS = jobListingDTORepository.findByUserId(appUser.getId());
        double userMatchValue = matchPercentage / 100.0;
        List<JobListingDTO> filteredJobs = new ArrayList<>(jobListingDTOS);
        log.info("Начинаем анализ {} вакансий с заданным порогом совпадения {} %", jobListingDTOS.size(), matchPercentage);
        // TODO Заменить на новый репозиторий как будет
        Optional<CurriculumVitaeDTO> curriculumVitae = curriculumVitaeRepository.findByUserId(appUser.getId());
        for (JobListingDTO job : jobListingDTOS) {
            StringBuilder promptToCalculateJobMatchingRate = new StringBuilder();
            if (curriculumVitae.isPresent()) {
                buildPromptForChatGPTForUserData(promptToCalculateJobMatchingRate,
                        PROMPT_MESSAGE_FOR_JOB_MATCHER,
                        curriculumVitae.get());
            } else {
                return Collections.emptyList();
            }
            buildPromptForChatGPTForJobListingDTO(promptToCalculateJobMatchingRate, job);
            if (job.getJobMatchState().equals(JobMatchState.NOT_EVALUATED)) {
                Double jobMatchValue = openAIService.
                        chatGPTRequestMemoryLessSingle(promptToCalculateJobMatchingRate.toString());
                if (jobMatchValue <= userMatchValue) {
                    job.setJobMatchState(JobMatchState.NOT_MATCH);
                    jobListingDTORepository.save(job);
                    filteredJobs.remove(job);
                } else {
                    job.setJobMatchState(JobMatchState.MATCH);
                    jobListingDTORepository.save(job);
                }
            }
        }
        log.info("Подобрано {} вакансий с заданным порогом совпадения {} %", filteredJobs.size(), matchPercentage);
        return filteredJobs;
    }

    @Override
    public String generateCoversAndSendAsAttachment(AppUser appUser) {
        List<String> jobs = new ArrayList<>();
        jobs.add(String.format(EMAIL_PREFIX, appUser.getEmail()));
        List<JobListingDTO> jobList = jobListingDTORepository.findByUserIdMatched(appUser.getId(), JobMatchState.MATCH);

        if (jobList.isEmpty()) {
            return "Похоже у вас еще нет совпадений либо вы не загрузили вакансии " +
                    "Для поиска подходящих вакансий нажмите /match " +
                    "Для загрузки /download_jobs";
        }

        for (JobListingDTO job : jobList) {
            String messageString = String.format("Company: %s, Job Title: %s,  \n",
                    job.getCompanyName(), job.getJobTitle());
            jobs.add(messageString);
        }
        log.info("Job list prepared, generating cover letters");
        // Generate Multiple Cover Letters
        CompletableFuture<List<String>> listCompletableFuture = generateCoverLetters(appUser, jobList);

        // If generated try to send them all in one email as attachments
        try {
            assert listCompletableFuture != null;
            appUserService.sendMultipleCoverLetters(appUser, listCompletableFuture.get());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        return String.join("", jobs);
    }

    public String showDownloadedJobs(AppUser appUser) {
        List<String> jobs = new ArrayList<>();
        List<JobListingDTO> jobList = jobListingDTORepository.findByUserId(appUser.getId());
        if (jobList.size() > 0) {
            generateJobListStingForTelegram(jobs, jobList);
        } else {
            return "В вашей базе пока нет скаченных вакансий";
        }
        return String.join("", jobs);
    }

    public String showMatchedJobs(AppUser appUser) {
        List<String> jobs = new ArrayList<>();
        List<JobListingDTO> jobList = jobListingDTORepository.findByUserIdMatched(appUser.getId(), JobMatchState.MATCH);
        generateJobListStingForTelegram(jobs, jobList);
        return String.join("", jobs);
    }

    private static void generateJobListStingForTelegram(List<String> jobs, List<JobListingDTO> jobList) {
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

    private CompletableFuture<List<String>> generateCoverLetters(AppUser appUser, List<JobListingDTO> jobList) {
        Optional<CurriculumVitaeDTO> curriculumVitae = curriculumVitaeRepository.findById(appUser.getId());
        List<CompletableFuture<String>> coverLetterFutures = new ArrayList<>();

        for (JobListingDTO job : jobList) {
            StringBuilder promptToGenerateCoverLetter = new StringBuilder();
            if (curriculumVitae.isPresent()) {
                buildPromptForChatGPTForUserData(promptToGenerateCoverLetter,
                        PROMPT_MESSAGE_FOR_COVER_GENERATION,
                        curriculumVitae.get());
            } else {
                return null;
            }
            buildPromptForChatGPTForJobListingDTO(promptToGenerateCoverLetter, job);
            if (!job.getIsCoverSend()) {
                CompletableFuture<String> coverLetterFuture = openAIService.
                        chatGPTRequestMemoryLess(promptToGenerateCoverLetter.toString());

                job.setIsCoverSend(true);
                jobListingDTORepository.save(job);

                coverLetterFutures.add(coverLetterFuture);
            }
        }

        // Join all the futures into a single CompletableFuture that completes when all cover letters are generated
        log.info("Joining all generated cover letters");
        // Join all the futures into a single CompletableFuture that completes when all cover letters are generated
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(coverLetterFutures.toArray(new CompletableFuture[0]));

        // Convert CompletableFuture<Void> to CompletableFuture<List<String>>
        return allFutures.thenApply(v -> coverLetterFutures.stream()
                .map(CompletableFuture::join) // join each future (blocking operation, but they should all be completed already because of the `allOf`)
                .collect(Collectors.toList()));
    }


    private void buildPromptForChatGPTForUserData(StringBuilder promptToGenerateCoverLetter, String initializationPrompt,
                                                  CurriculumVitaeDTO curriculumVitae) {
        promptToGenerateCoverLetter
                .append(initializationPrompt).append("Here goes my CV: ")
                .append(replaceSymbolsWithSpaces(curriculumVitae.getCvDescription()));
    }

    private void buildPromptForChatGPTForJobListing(StringBuilder jobDescription, JobListing job) {
        jobDescription
                .append(" Here is the job description: ").append(" ")
                .append(replaceSymbolsWithSpaces(job.getCompanyName())).append(" ")
                .append(replaceSymbolsWithSpaces(job.getJobTitle())).append(" ")
                .append(replaceSymbolsWithSpaces(job.getCompanyDescription())).append(" ")
                .append(replaceSymbolsWithSpaces(job.getJobResponsibilities())).append(" ")
                .append(replaceSymbolsWithSpaces(job.getJobQualifications())).append(" ")
                .append(replaceSymbolsWithSpaces(job.getJobAdditionalSkills()));
    }

    private void buildPromptForChatGPTForJobListingDTO(StringBuilder jobDescription, JobListingDTO job) {
        jobDescription
                .append("Here is the job description: ").append(" ")
                .append(replaceSymbolsWithSpaces(job.getCompanyName())).append(" ")
                .append(replaceSymbolsWithSpaces(job.getJobTitle())).append(" ")
                .append(replaceSymbolsWithSpaces(job.getJobDescription()));
    }

    public String replaceSymbolsWithSpaces(String text) {
        return text.replaceAll("[^a-zA-Z0-9 ]", "");
    }
}