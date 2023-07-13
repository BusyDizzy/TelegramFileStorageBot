package com.java.service.impl;

import com.java.entity.UrlMapping;
import com.java.repository.UrlMappingRepository;
import com.java.service.UrlShortener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

@Service
public class UrlShortenerImpl implements UrlShortener {
    private final UrlMappingRepository urlMappingRepository;
    private static final String domain = "http://127.0.0.1:8086/r/"; // Use your domain name
    private static final char[] charSet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();

    public UrlShortenerImpl(UrlMappingRepository urlMappingRepository) {
        this.urlMappingRepository = urlMappingRepository;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public String shortenURL(String originalURL) {
        UrlMapping mapping = urlMappingRepository.findByOriginalUrl(originalURL);
        if (mapping != null) {
            return domain + mapping.getShortUrl();
        } else {
            String key = generateKey();
            mapping = new UrlMapping();
            mapping.setOriginalUrl(originalURL);
            mapping.setShortUrl(key);
            urlMappingRepository.save(mapping);
            return domain + key;
        }
    }

    public String getOriginalURL(String shortURL) {
        String key = shortURL.replace(domain, "");
        UrlMapping mapping = urlMappingRepository.findByShortUrl(key);
        return (mapping != null) ? mapping.getOriginalUrl() : null;
    }

    private String generateKey() {
        String key = null;
        boolean flag = true;
        while (flag) {
            key = "";
            for (int i = 0; i <= 5; i++) {
                key += charSet[new Random().nextInt(62)];
            }
            if (urlMappingRepository.findByShortUrl(key) == null) {
                flag = false;
            }
        }
        return key;
    }
}