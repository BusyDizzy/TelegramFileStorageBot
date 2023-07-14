package com.java.service.impl;

import com.java.DTO.CurriculumVitaeDTO;
import com.java.entity.AppUser;
import com.java.repository.AppUserRepository;
import com.java.repository.CurriculumVitaeDTORepository;
import com.java.service.CvParsingService;
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

@Service
@Slf4j
public class CvParsingServiceImpl implements CvParsingService {

    private final CurriculumVitaeDTORepository curriculumVitaeDTORepository;
    private final AppUserRepository appUserRepository;

    public CvParsingServiceImpl(CurriculumVitaeDTORepository curriculumVitaeDTORepository, AppUserRepository appUserRepository) {
        this.curriculumVitaeDTORepository = curriculumVitaeDTORepository;
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
        CurriculumVitaeDTO savedCv = curriculumVitaeDTORepository.save(curriculumVitaeDTO);
        appUser.setIsCvUploaded(true);
        appUserRepository.save(appUser);
        log.info("Document for user {} was uploaded", appUser.getEmail());
        return savedCv;
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
}

