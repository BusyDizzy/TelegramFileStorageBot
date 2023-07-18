package com.java.controller;

import com.java.service.UrlShortenerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping("/r")
public class RedirectController {

    private final UrlShortenerService urlShortenerService;

    public RedirectController(UrlShortenerService urlShortenerService) {
        this.urlShortenerService = urlShortenerService;
    }

    @GetMapping("/{key}")
    public void redirect(@PathVariable("key") String key, HttpServletResponse response) throws IOException {
        String originalUrl = urlShortenerService.getOriginalURL(key);
        if (originalUrl != null) {
            response.sendRedirect(originalUrl);
        } else {
            // handle not found
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
