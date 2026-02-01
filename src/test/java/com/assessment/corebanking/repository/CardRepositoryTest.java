package com.assessment.corebanking.repository;

import com.assessment.corebanking.config.AuditingConfig;
import com.assessment.corebanking.entity.Card;
import com.assessment.corebanking.enums.CardStatus;
import com.assessment.corebanking.enums.CardType;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(AuditingConfig.class)
@ActiveProfiles("test")
class CardRepositoryTest {

    @Autowired
    private CardRepository cardRepository;

    @Test
    void savePopulatesIdAndAuditFields() {
        Card card = new Card();
        card.setCardNumber("4293127308501088"); // Visa
        card.setCardholderName("Danial Ariff");
        card.setExpiryDate(LocalDate.now().plusYears(2));
        card.setStatus(CardStatus.ACTIVE);
        card.setCardType(CardType.DEBIT);
        card.setBalance(new BigDecimal("2847.50"));

        Card saved = cardRepository.save(card);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();

        assertThat(cardRepository.findById(saved.getId()))
            .isPresent()
            .get()
            .extracting(Card::getCardNumber)
            .isEqualTo("4293127308501088");
    }
}
