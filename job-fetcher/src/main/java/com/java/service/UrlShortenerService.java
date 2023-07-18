package com.java.service;

public interface UrlShortenerService {
    String shortenURL(String originalURL);
    String getOriginalURL(String shortURL);
}
