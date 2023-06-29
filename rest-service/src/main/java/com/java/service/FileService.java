package com.java.service;

import com.java.entity.AppDocument;
import com.java.entity.AppPhoto;
import com.java.entity.BinaryContent;
import org.springframework.core.io.FileSystemResource;

public interface FileService {
    AppDocument getDocument(String id);

    AppPhoto getPhoto(String id);

    FileSystemResource getFileSystemResource(BinaryContent binaryContent);
}