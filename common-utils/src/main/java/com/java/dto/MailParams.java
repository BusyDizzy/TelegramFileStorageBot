package com.java.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MailParams {

    private String id;
    private String emailTo;
    private String coverLetter;
    private String emailBody;
    private List<MultipartFile> coverLetterFiles;
}