package com.java.service.impl;

import com.java.entity.AppUser;
import com.java.entity.CurriculumVitae;
import com.java.entity.JobListing;
import com.java.repository.CurriculumVitaeRepository;
import com.java.repository.JobListingRepository;
import com.java.service.JobService;
import com.java.service.OpenAIService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class JobServiceImpl implements JobService {

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

    private final AppUserServiceImpl appUserService;

    public JobServiceImpl(OpenAIService openAIService, JobListingRepository jobListingRepository, CurriculumVitaeRepository curriculumVitaeRepository, AppUserServiceImpl appUserService) {
        this.openAIService = openAIService;
        this.jobListingRepository = jobListingRepository;
        this.curriculumVitaeRepository = curriculumVitaeRepository;
        this.appUserService = appUserService;
    }

    public String generateCoverLetters(AppUser appUser) {
        List<String> jobs = new ArrayList<>();
        jobs.add(String.format(EMAIL_PREFIX, appUser.getEmail()));
        List<JobListing> jobList = jobListingRepository.findAll();
        CurriculumVitae curriculumVitae = curriculumVitaeRepository.getCVsWithJobExperiences(1L);
        String coverLetter;
        StringBuilder promptToGenerateCoverLetter = new StringBuilder();

        for (JobListing job : jobList) {
            promptToGenerateCoverLetter.setLength(0);
            buildPromptForChatGPTForCV(promptToGenerateCoverLetter, curriculumVitae);
            buildPromptForChatGPTForJob(promptToGenerateCoverLetter, job);

            coverLetter = generateCover(promptToGenerateCoverLetter.toString());
            appUserService.sendUserData(appUser, coverLetter);

            String messageString = String.format("Company: %s, Job Title: %s,  \n",
                    job.getCompanyName(), job.getJobTitle());
            jobs.add(messageString);
        }
        return String.join("", jobs);
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
                .append("Here is the job description").append(" ")
                .append(replaceSymbolsWithSpaces(job.getCompanyName())).append(" ")
                .append(replaceSymbolsWithSpaces(job.getJobTitle())).append(" ")
                .append(replaceSymbolsWithSpaces(job.getCompanyDescription())).append(" ")
                .append(replaceSymbolsWithSpaces(job.getJobResponsibilities())).append(" ")
                .append(replaceSymbolsWithSpaces(job.getJobQualifications())).append(" ")
                .append(replaceSymbolsWithSpaces(job.getJobAdditionalSkills()));
    }

    private String generateCover(String requiredDetails) {
        return openAIService.chatGPTRequest(requiredDetails);
    }

    public String replaceSymbolsWithSpaces(String text) {
        return text.replaceAll("[/\\\\\\-()'\"<>*]", " ");
    }
}