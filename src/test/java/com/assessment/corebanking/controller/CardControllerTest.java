package com.assessment.corebanking.controller;

import com.assessment.corebanking.dto.CardRequest;
import com.assessment.corebanking.entity.Card;
import com.assessment.corebanking.enums.CardStatus;
import com.assessment.corebanking.enums.CardType;
import com.assessment.corebanking.repository.CardRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureMockRestServiceServer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureMockRestServiceServer
@ActiveProfiles("test")
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private MockRestServiceServer mockServer;

    @AfterEach
    void cleanup() {
        cardRepository.deleteAll();
    }

    @Test
    void createCardReturnsMaskedCardNumber() throws Exception {
        CardRequest request = buildRequest("4293127308501088", CardType.CREDIT, new BigDecimal("100000.00"));

        mockMvc.perform(post("/api/cards")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.cardNumber").value(endsWith("1088")))
            .andExpect(jsonPath("$.cardNumber").value(not(containsString("429312730850"))));
    }

    @Test
    void getCardByIdReturnsCard() throws Exception {
        Card saved = saveCard("370144404935247", CardType.CREDIT);

        mockMvc.perform(get("/api/cards/{id}", saved.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(saved.getId()))
            .andExpect(jsonPath("$.cardholderName").value("Danial Ariff"));
    }

    @Test
    void updateCardUpdatesFields() throws Exception {
        Card saved = saveCard("5400071730269186", CardType.DEBIT);
        CardRequest update = buildRequest("5400071730269186", CardType.DEBIT, null);
        update.setCardholderName("Updated Name");

        mockMvc.perform(put("/api/cards/{id}", saved.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(update)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.cardholderName").value("Updated Name"));
    }

    @Test
    void deleteCardRemovesCard() throws Exception {
        Card saved = saveCard("4485275742308327", CardType.DEBIT);

        mockMvc.perform(delete("/api/cards/{id}", saved.getId()))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/cards/{id}", saved.getId()))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("Card not found"))
            .andExpect(jsonPath("$.id").value(saved.getId()));
    }

    @Test
    void listCardsEnforcesFixedPageSize() throws Exception {
        for (int i = 0; i < 12; i++) {
            saveCard("40000000000000" + String.format("%02d", i), CardType.DEBIT);
        }

        mockMvc.perform(get("/api/cards")
                .param("page", "0")
                .param("size", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.size").value(10))
            .andExpect(jsonPath("$.content", hasSize(10)));
    }

    @Test
    void validationErrorsReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/cards")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors").isArray())
            .andExpect(jsonPath("$.errors[*].field", hasItem("cardNumber")));
    }

    @Test
    void domainValidationReturnsBadRequest() throws Exception {
        CardRequest request = buildRequest("379569568708022", CardType.DEBIT, new BigDecimal("5000.00"));

        mockMvc.perform(post("/api/cards")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("creditLimit is only allowed for CREDIT cards"));
    }

    @Test
    void getCardNotFoundReturns404() throws Exception {
        mockMvc.perform(get("/api/cards/{id}", 999L))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("Card not found"))
            .andExpect(jsonPath("$.id").value(999));
    }

    @Test
    void getNotificationsReturnsPayload() throws Exception {
        Card saved = saveCard("4485275742308327", CardType.DEBIT);
        long cardId = saved.getId();
        long mod = Math.floorMod(cardId, 10);
        int userId = Math.toIntExact(mod + 1);
        String body = "[{\"userId\":" + userId
            + ",\"id\":21,\"title\":\"Test title\",\"body\":\"Test body\"}]";

        mockServer.expect(requestTo("https://jsonplaceholder.typicode.com/posts?userId=" + userId))
            .andExpect(method(org.springframework.http.HttpMethod.GET))
            .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

        mockMvc.perform(get("/api/cards/{id}/notifications", cardId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.cardId").value(cardId))
            .andExpect(jsonPath("$.notifications", hasSize(1)))
            .andExpect(jsonPath("$.notifications[0].id").value(21))
            .andExpect(jsonPath("$.notifications[0].title").value("Test title"));

        mockServer.verify();
    }

    @Test
    void getNotificationsCardNotFoundReturns404() throws Exception {
        int userId = Math.toIntExact(Math.floorMod(999L, 10) + 1);
        mockServer.expect(ExpectedCount.never(),
                requestTo("https://jsonplaceholder.typicode.com/posts?userId=" + userId))
            .andExpect(method(org.springframework.http.HttpMethod.GET))
            .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        mockMvc.perform(get("/api/cards/{id}/notifications", 999L))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("Card not found"))
            .andExpect(jsonPath("$.id").value(999));

        mockServer.verify();
    }

    private CardRequest buildRequest(String cardNumber, CardType cardType, BigDecimal creditLimit) {
        CardRequest request = new CardRequest();
        request.setCardNumber(cardNumber);
        request.setCardholderName("Danial Ariff");
        request.setExpiryDate(LocalDate.of(2031, 6, 30));
        request.setStatus(CardStatus.ACTIVE);
        request.setCardType(cardType);
        request.setCreditLimit(creditLimit);
        request.setBalance(new BigDecimal("15420.75"));
        return request;
    }

    private Card saveCard(String cardNumber, CardType cardType) {
        Card card = new Card();
        card.setCardNumber(cardNumber);
        card.setCardholderName("Danial Ariff");
        card.setExpiryDate(LocalDate.of(2031, 6, 30));
        card.setStatus(CardStatus.ACTIVE);
        card.setCardType(cardType);
        if (cardType == CardType.CREDIT) {
            card.setCreditLimit(new BigDecimal("100000.00"));
        }
        card.setBalance(new BigDecimal("15420.75"));
        return cardRepository.save(card);
    }
}
