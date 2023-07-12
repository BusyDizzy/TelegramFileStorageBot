package com.java.strategy.impl;

import com.java.DTO.JobListingDTO;
import com.java.entity.enums.JobMatchState;
import com.java.strategy.Strategy;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@Slf4j
public class LinkedInStrategyImpl implements Strategy {

    private static final Random RANDOM = new Random();

    private final static String URL_FORMAT = "https://www.linkedin.com/jobs/search/?currentJobId=3656884339&geoId=102454443&keywords=%s&location=%s&refresh=true&start=%d";

    private static final String[] USER_AGENTS = {
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3",
            "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:54.0) Gecko/20100101 Firefox/54.0",
            "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.90 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_5) AppleWebKit/603.2.4 (KHTML, like Gecko) Version/10.1.1 Safari/603.2.4"
    };

    private static String getRandomUserAgent() {
        int randomIndex = RANDOM.nextInt(USER_AGENTS.length);
        return USER_AGENTS[randomIndex];
    }

    @Override
    public List<JobListingDTO> getVacancies(String query, String location, Long appUserId) {
        List<JobListingDTO> jobListings = new ArrayList<>();
        final int MAX_RETRY = 3; // Maximum number of retries
        final int DELAY_BETWEEN_REQUESTS = 5000; // Delay between requests in milliseconds
        log.info("Starting LinkedIn strategy job fetching");
        int start = 0;
        do {
            Document document;
            int retryCount = 0; // Reset retry count for each document
            while (true) {
                try {
                    document = getDocument(query, location, start);
                    break; // If the document is fetched successfully, break the loop
                } catch (HttpStatusException e) {
                    if (e.getStatusCode() == 502 || e.getStatusCode() == 429) {
                        if (++retryCount > MAX_RETRY) {
                            throw new RuntimeException("Exceeded maximum retries", e);
                        }
                        // Sleep before retry
                        try {
                            log.info("Encountered {} Jsoup error while fetching jobs. Sleeping...for {} ",
                                    e.getStatusCode(), DELAY_BETWEEN_REQUESTS);
                            Thread.sleep(DELAY_BETWEEN_REQUESTS);
                        } catch (InterruptedException ie) {
                        }
                    } else {
                        throw new RuntimeException(e); // For other exceptions, rethrow them immediately
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            Elements jobElements = document.select("div.job-search-card");

            for (Element jobElement : jobElements) {
                String jobId = jobElement.attr("data-entity-urn").split(":")[3];
                String jobTitle = jobElement.select("h3.base-search-card__title").first().text();
                String companyName = jobElement.select("h4.base-search-card__subtitle a").first().text();
                String jobUrl = jobElement.select("a.base-card__full-link").first().attr("href");
                Document jobDocument = null;

                retryCount = 0; // Reset retry count for each job document
                while (true) {
                    try {
                        jobDocument = Jsoup.connect(jobUrl).get();
                        break; // If the document is fetched successfully, break the loop
                    } catch (HttpStatusException e) {
                        if (e.getStatusCode() == 502 || e.getStatusCode() == 429) {
                            if (++retryCount > MAX_RETRY) {
                                throw new RuntimeException("Exceeded maximum retries", e);
                            }
                            // Sleep before retry
                            try {
                                log.info("Encountered {} Jsoup error while collecting description. Sleeping for {}",
                                        e.getStatusCode(), DELAY_BETWEEN_REQUESTS);
                                Thread.sleep(DELAY_BETWEEN_REQUESTS);
                            } catch (InterruptedException ie) {
                            }
                        } else {
                            throw new RuntimeException(e); // For other exceptions, rethrow them immediately
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                // Assuming the job description is contained in a div with class 'description__text'
                Element jobDescriptionElement = jobDocument.select("div.description__text").first();
                String jobDescription = jobDescriptionElement != null ? jobDescriptionElement.text() : "Job description not available";

                JobListingDTO jobListing = new JobListingDTO(null, jobId, jobTitle, companyName, jobDescription,
                        jobUrl, JobMatchState.NOT_EVALUATED, false, appUserId);
                log.info("New job listing is added with job id {} for company: {}", jobId, companyName);
                jobListings.add(jobListing);

                // Sleep after fetching each job detail
                try {
                    Thread.sleep(DELAY_BETWEEN_REQUESTS);
                } catch (InterruptedException e) {
                }
            }
            start += 25;
            log.info("Collected {} of LinkedIn records", start);
            if (start == 25) {
                break;
            }
        } while (true);
        log.info("Finished collection of {} LinkedIn job listings", jobListings.size());
        return jobListings;
    }


    protected Document getDocument(String searchString, String location, int start) throws IOException {
        String url = String.format(URL_FORMAT, searchString, location, start);
        return Jsoup.connect(url)
                .userAgent(getRandomUserAgent())
                .referrer("http://www.google.com")
                .get();
    }
}


