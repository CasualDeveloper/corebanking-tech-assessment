package com.assessment.corebanking.service;

import com.assessment.corebanking.dto.ExternalPost;
import com.assessment.corebanking.exception.ExternalApiException;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class ExternalApiService {

    private static final String BASE_URL = "https://jsonplaceholder.typicode.com";

    private final RestTemplate restTemplate;

    public ExternalApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Transactional
    public List<ExternalPost> getNotificationsForCard(Long cardId) {
        if (cardId == null) {
            throw new IllegalArgumentException("cardId is required");
        }
        long mod = Math.floorMod(cardId, 10);
        int userId = Math.toIntExact(mod + 1);
        String url = BASE_URL + "/posts?userId=" + userId;
        try {
            ExternalPost[] posts = restTemplate.getForObject(url, ExternalPost[].class);
            if (posts == null) {
                return List.of();
            }
            return Arrays.asList(posts);
        } catch (RestClientException ex) {
            throw new ExternalApiException("Failed to fetch card notifications", ex);
        }
    }
}
