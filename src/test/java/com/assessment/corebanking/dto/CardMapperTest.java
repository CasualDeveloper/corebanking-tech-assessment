package com.assessment.corebanking.dto;

import com.assessment.corebanking.entity.Card;
import com.assessment.corebanking.enums.CardStatus;
import com.assessment.corebanking.enums.CardType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CardMapperTest {

    @Test
    void toResponseMasksCardNumber() {
        Card card = new Card();
        card.setId(1L);
        card.setCardNumber("5400071730269186"); // Mastercard
        card.setCardholderName("Danial Ariff");
        card.setExpiryDate(LocalDate.of(2031, 6, 30));
        card.setStatus(CardStatus.ACTIVE);
        card.setCardType(CardType.CREDIT);
        card.setCreditLimit(new BigDecimal("100000.00"));
        card.setBalance(new BigDecimal("15420.75"));
        card.setCreatedAt(LocalDateTime.now());
        card.setUpdatedAt(LocalDateTime.now());

        CardResponse response = CardMapper.toResponse(card);

        assertThat(response.getCardNumber()).endsWith("9186");
        assertThat(response.getCardNumber()).doesNotContain("540007173026");
    }
}
