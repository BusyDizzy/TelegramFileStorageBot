package com.java.service;

public interface UrlShortener {
    String shortenURL(String originalURL);
    String getOriginalURL(String shortURL);
}
