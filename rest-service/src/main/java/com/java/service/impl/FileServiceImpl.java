package com.java.service.impl;

import com.java.entity.AppDocument;
import com.java.entity.AppPhoto;
import com.java.entity.BinaryContent;
import com.java.repository.AppDocumentRepository;
import com.java.repository.AppPhotoRepository;
import com.java.service.FileService;
import lombok.extern.log4j.Log4j;
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Log4j
@Service
public class FileServiceImpl implements FileService {

    private final AppDocumentRepository appDocumentRepository;

    private final AppPhotoRepository appPhotoRepository;

    public FileServiceImpl(AppDocumentRepository appDocumentRepository, AppPhotoRepository appPhotoRepository) {
        this.appDocumentRepository = appDocumentRepository;
        this.appPhotoRepository = appPhotoRepository;
    }

    @Override
    public AppDocument getDocument(String docId) {
        //TODO добавить дешифрование хэш-строки
        var id = Long.parseLong(docId);
        return appDocumentRepository.findById(id).orElse(null);
    }

    @Override
    public AppPhoto getPhoto(String photoId) {
        //TODO добавить дешифрование хэш-строки
        var id = Long.parseLong(photoId);
        return appPhotoRepository.findById(id).orElse(null);
    }

    @Override
    public FileSystemResource getFileSystemResource(BinaryContent binaryContent) {
        try {
            //TODO добавить генерацию имени временного файла
            File temp = File.createTempFile("tempFile", ".bin");
            temp.deleteOnExit();
            FileUtils.writeByteArrayToFile(temp, binaryContent.getFileAsArrayOfBytes());
            return new FileSystemResource(temp);
        } catch (IOException e) {
            log.error(e);
            return null;
        }
    }
}