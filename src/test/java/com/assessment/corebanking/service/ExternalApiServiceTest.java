package com.assessment.corebanking.service;

import com.assessment.corebanking.dto.ExternalPost;
import com.assessment.corebanking.exception.ExternalApiException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.test.web.client.MockRestServiceServer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ExternalApiServiceTest {

    private RestTemplate restTemplate;
    private MockRestServiceServer server;
    private ExternalApiService externalApiService;

    @BeforeEach
    void setup() {
        restTemplate = new RestTemplate();
        server = MockRestServiceServer.createServer(restTemplate);
        externalApiService = new ExternalApiService(restTemplate);
    }

    @Test
    void getNotificationsForCardReturnsPosts() {
        long cardId = 42L; // userId = (42 % 10) + 1 = 3
        String body = "[{\"userId\":3,\"id\":21,\"title\":\"Hello\",\"body\":\"World\"}]";

        server.expect(requestTo("https://jsonplaceholder.typicode.com/posts?userId=3"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

        List<ExternalPost> posts = externalApiService.getNotificationsForCard(cardId);

        assertThat(posts).hasSize(1);
        assertThat(posts.get(0).getUserId()).isEqualTo(3);
        assertThat(posts.get(0).getId()).isEqualTo(21);
        server.verify();
    }

    @Test
    void getNotificationsForCardThrowsOnTimeout() {
        long cardId = 7L; // userId = 8

        server.expect(requestTo("https://jsonplaceholder.typicode.com/posts?userId=8"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(request -> {
                throw new ResourceAccessException("timeout");
            });

        assertThatThrownBy(() -> externalApiService.getNotificationsForCard(cardId))
            .isInstanceOf(ExternalApiException.class);
    }
}
