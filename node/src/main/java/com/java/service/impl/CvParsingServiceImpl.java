package com.java.service.impl;

import com.java.DTO.CurriculumVitaeDTO;
import com.java.entity.AppUser;
import com.java.entity.ChatGPTPrompt;
import com.java.entity.CurriculumVitae;
import com.java.repository.AppUserRepository;
import com.java.repository.ChatGPTPromptsRepository;
import com.java.repository.CurriculumVitaeDTORepository;
import com.java.repository.CurriculumVitaeRepository;
import com.java.service.CvParsingService;
import com.java.service.OpenAIService;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.java.entity.enums.PromptType.PARSE_CURRICULUM_VITAE;

@Service
@Slf4j
public class CvParsingServiceImpl implements CvParsingService {


    private final OpenAIService openAIService;

    private final ChatGPTPromptsRepository chatGPTPromptsRepository;

    private final CurriculumVitaeDTORepository curriculumVitaeDTORepository;

    private final CurriculumVitaeRepository curriculumVitaeRepository;
    private final AppUserRepository appUserRepository;

    public CvParsingServiceImpl(OpenAIService openAIService,
                                ChatGPTPromptsRepository chatGPTPromptsRepository, CurriculumVitaeDTORepository curriculumVitaeDTORepository,
                                CurriculumVitaeRepository curriculumVitaeRepository, AppUserRepository appUserRepository) {
        this.openAIService = openAIService;
        this.chatGPTPromptsRepository = chatGPTPromptsRepository;
        this.curriculumVitaeDTORepository = curriculumVitaeDTORepository;
        this.curriculumVitaeRepository = curriculumVitaeRepository;
        this.appUserRepository = appUserRepository;
    }

    public CurriculumVitaeDTO parseAndSaveCv(byte[] cvFileBytes, AppUser appUser) {
        InputStream cvFileStream = new ByteArrayInputStream(cvFileBytes);
        log.info("Going to parse the incoming document for user {}: ", appUser.getEmail());
        String cvDescription = parseCvToPlainText(cvFileStream);

//        AppUser appUser = appUserRepository.findById(appUserId).orElseThrow(() -> new RuntimeException("User not found"));

        CurriculumVitaeDTO curriculumVitaeDTO = new CurriculumVitaeDTO();
        curriculumVitaeDTO.setCvDescription(cvDescription);
        curriculumVitaeDTO.setAppUser(appUser);
        try {
            cvFileStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        parseDetailsUsingAIAndSave(curriculumVitaeDTO, appUser);
        CurriculumVitaeDTO savedCv = curriculumVitaeDTORepository.save(curriculumVitaeDTO);
        appUser.setIsCvUploaded(true);
        appUserRepository.save(appUser);
        log.info("Document for user {} was uploaded", appUser.getEmail());
        return savedCv;
    }

    private void parseDetailsUsingAIAndSave(CurriculumVitaeDTO savedCv, AppUser appUser) {
        ChatGPTPrompt prompt = chatGPTPromptsRepository.findByPromptType(1L, PARSE_CURRICULUM_VITAE);
        String gptReply = openAIService.simpleSessionLessRequest(prompt.getPrompt() + " " + savedCv.getCvDescription());
        CurriculumVitae parsedCurriculum = parseCV(gptReply, appUser);
        curriculumVitaeRepository.save(parsedCurriculum);
    }

    private String parseCvToPlainText(InputStream cvFileStream) {
        BodyContentHandler handler = new BodyContentHandler(-1);
        AutoDetectParser parser = new AutoDetectParser();
        Metadata metadata = new Metadata();
        try {
            parser.parse(cvFileStream, handler, metadata, new ParseContext());
        } catch (IOException | SAXException | TikaException e) {
            // handle exceptions
            e.printStackTrace();
        }
        return handler.toString();
    }

    private CurriculumVitae parseCV(String message, AppUser appUser) {
        String name = extractValue(message, "Name:");
        String position = extractValue(message, "Position:");
        String yearsOfExperience = extractValue(message, "Years of experience:");
        String experienceDescription = extractExperienceDescription(message);
        String email = extractValue(message, "Email:");
        String phone = extractValue(message, "Phone:");
        String hardSkills = extractValue(message, "Hard Skills:");
        String softSkills = extractValue(message, "Soft Skills:");
        String education = extractEducation(message);

        return new CurriculumVitae(name, position, email, phone, hardSkills, softSkills, yearsOfExperience,
                experienceDescription, education, appUser);
    }

    private String extractValue(String message, String field) {
        Pattern pattern = Pattern.compile(field + "\\s+(.+)");
        Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }


    private String extractExperienceDescription(String message) {
        Pattern pattern = Pattern.compile("Experience Description:(.*?)Education:", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return "";
    }

    private String extractEducation(String message) {
        Pattern pattern = Pattern.compile("Education:(.*?)Email:", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return "";
    }
}
