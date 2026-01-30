package com.assessment.corebanking.service;

import com.assessment.corebanking.dto.CardRequest;
import com.assessment.corebanking.entity.Card;
import com.assessment.corebanking.enums.CardStatus;
import com.assessment.corebanking.enums.CardType;
import com.assessment.corebanking.repository.CardRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class CardServiceTest {

    @Autowired
    private CardService cardService;

    @Autowired
    private CardRepository cardRepository;

    @AfterEach
    void cleanup() {
        cardRepository.deleteAll();
    }

    @Test
    void createUpdateGetDeleteFlow() {
        CardRequest request = new CardRequest();
        request.setCardNumber("370144404935247"); // Amex
        request.setCardholderName("Danial Ariff");
        request.setExpiryDate(LocalDate.of(2031, 6, 30));
        request.setStatus(CardStatus.ACTIVE);
        request.setCardType(CardType.CREDIT);
        request.setCreditLimit(new BigDecimal("100000.00"));
        request.setBalance(new BigDecimal("31572.80"));

        Card created = cardService.createCard(request);
        Card fetched = cardService.getCardById(created.getId());

        assertThat(fetched.getCardholderName()).isEqualTo("Danial Ariff");

        request.setCardholderName("Updated Name");
        Card updated = cardService.updateCard(created.getId(), request);

        assertThat(updated.getCardholderName()).isEqualTo("Updated Name");

        cardService.deleteCard(created.getId());
    }

    @Test
    void getAllCardsForcesFixedPageSize() {
        Page<Card> page = cardService.getAllCards(PageRequest.of(0, 5));
        assertThat(page.getSize()).isEqualTo(10);
    }
}
