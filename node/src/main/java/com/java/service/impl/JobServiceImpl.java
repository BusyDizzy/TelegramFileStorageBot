package com.java.service.impl;

import com.java.entity.AppUser;
import com.java.entity.CurriculumVitae;
import com.java.entity.JobListing;
import com.java.repository.CurriculumVitaeRepository;
import com.java.repository.JobListingRepository;
import com.java.service.AppUserService;
import com.java.service.JobService;
import com.java.service.OpenAIService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Slf4j
@Service
public class JobServiceImpl implements JobService {


//    private final AsyncTaskExecutor taskExecutor;
    private final OpenAIService openAIService;
    private final JobListingRepository jobListingRepository;

    private final CurriculumVitaeRepository curriculumVitaeRepository;

    private static final String EMAIL_PREFIX = "На вашу почту %s будут отправлены письма на следующие вакансии: \n";
    private static final String PROMPT_MESSAGE = "Accumulate my CV first. " +
            "Than generate cover letter for the position below based on my CV. Do not pour water, " +
            "be specific, provide three bullet points at once, why I am suitable for this job, the fourth point" +
            " should be why I want to work in this company, generate something human: for example, this is " +
            "my first bank in this country or I used the services of this company for several years and etc. " +
            "If its recruiting agency skip this part. If there are signs that company is proud to work on " +
            "a local market, tell about my desire to be a part of this country (where job is applicable). " +
            "Provide at the beginning several reasons why you would like to work on this company, " +
            "and then why I am a good fit. Here goes my CV:";

    private final AppUserService appUserService;

    public JobServiceImpl(OpenAIServiceImpl openAIServiceImpl,
                          JobListingRepository jobListingRepository,
                          CurriculumVitaeRepository curriculumVitaeRepository,
                          AppUserService appUserService) {
        this.openAIService = openAIServiceImpl;
        this.jobListingRepository = jobListingRepository;
        this.curriculumVitaeRepository = curriculumVitaeRepository;
        this.appUserService = appUserService;
    }

    @Override
    public String showJobs(AppUser appUser) {
        List<String> jobs = new ArrayList<>();
        jobs.add(String.format(EMAIL_PREFIX, appUser.getEmail()));
        List<JobListing> jobList = jobListingRepository.findAll();
        for (JobListing job : jobList) {
            String messageString = String.format("Company: %s, Job Title: %s,  \n",
                    job.getCompanyName(), job.getJobTitle());
            jobs.add(messageString);
        }
        log.info("Job list prepared, generating cover letters");
        // Generate Multiple Cover Letters
        CompletableFuture<List<String>> listCompletableFuture = generateCoverLetters(appUser, jobList);

        // If generated try to send them all in one email as attachments
        try {
            appUserService.sendMultipleCoverLetters(appUser, listCompletableFuture.get());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        return String.join("", jobs);
    }

    @Override
    public CompletableFuture<List<String>> generateCoverLetters(AppUser appUser, List<JobListing> jobList) {
        CurriculumVitae curriculumVitae = curriculumVitaeRepository.getCVsWithJobExperiences(1L);
        List<CompletableFuture<String>> coverLetterFutures = new ArrayList<>();

        for (JobListing job : jobList) {
            StringBuilder promptToGenerateCoverLetter = new StringBuilder();
            buildPromptForChatGPTForCV(promptToGenerateCoverLetter, curriculumVitae);
            buildPromptForChatGPTForJob(promptToGenerateCoverLetter, job);

            CompletableFuture<String> coverLetterFuture = openAIService.
                    chatGPTRequestMemoryLess(promptToGenerateCoverLetter.toString());


            coverLetterFutures.add(coverLetterFuture);
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


    private void buildPromptForChatGPTForCV(StringBuilder promptToGenerateCoverLetter,
                                            CurriculumVitae curriculumVitae) {
        promptToGenerateCoverLetter
                .append(PROMPT_MESSAGE).append(" ")
                .append(replaceSymbolsWithSpaces(curriculumVitae.getFullName())).append(" ")
                .append(replaceSymbolsWithSpaces(curriculumVitae.getSummaryObjective())).append(" ")
                .append(replaceSymbolsWithSpaces(curriculumVitae.getSoftSkills())).append(" ")
                .append(replaceSymbolsWithSpaces(curriculumVitae.getHardSkills())).append(" ")
                .append(replaceSymbolsWithSpaces(curriculumVitae.getJobExperience().toString())).append(" ")
                .append(replaceSymbolsWithSpaces(curriculumVitae.getEducationHistory())).append(" ");
    }

    private void buildPromptForChatGPTForJob(StringBuilder jobDescription, JobListing job) {
        jobDescription
                .append("Here is the job description: ").append(" ")
                .append(replaceSymbolsWithSpaces(job.getCompanyName())).append(" ")
                .append(replaceSymbolsWithSpaces(job.getJobTitle())).append(" ")
                .append(replaceSymbolsWithSpaces(job.getCompanyDescription())).append(" ")
                .append(replaceSymbolsWithSpaces(job.getJobResponsibilities())).append(" ")
                .append(replaceSymbolsWithSpaces(job.getJobQualifications())).append(" ")
                .append(replaceSymbolsWithSpaces(job.getJobAdditionalSkills()));
    }

    public String replaceSymbolsWithSpaces(String text) {
        return text.replaceAll("[/\\\\\\-()'\"<>*]", " ");
    }
}