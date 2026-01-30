package com.assessment.corebanking.service;

import com.assessment.corebanking.dto.CardRequest;
import com.assessment.corebanking.enums.CardStatus;
import com.assessment.corebanking.enums.CardType;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CardDomainValidatorTest {

    @Test
    void rejectsCreditLimitForNonCreditCards() {
        CardRequest request = new CardRequest();
        request.setCardNumber("5400071730269186"); // Mastercard
        request.setCardholderName("Danial Ariff");
        request.setExpiryDate(LocalDate.of(2031, 6, 30));
        request.setStatus(CardStatus.ACTIVE);
        request.setCardType(CardType.DEBIT);
        request.setCreditLimit(new BigDecimal("100000.00"));
        request.setBalance(new BigDecimal("4215.30"));

        CardDomainValidator validator = new CardDomainValidator();

        assertThatThrownBy(() -> validator.validate(request))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void allowsCreditLimitForCreditCards() {
        CardRequest request = new CardRequest();
        request.setCardNumber("4293127308501088"); // Visa
        request.setCardholderName("Danial Ariff");
        request.setExpiryDate(LocalDate.of(2031, 10, 31));
        request.setStatus(CardStatus.ACTIVE);
        request.setCardType(CardType.CREDIT);
        request.setCreditLimit(new BigDecimal("100000.00"));
        request.setBalance(new BigDecimal("67890.25"));

        CardDomainValidator validator = new CardDomainValidator();

        assertThatNoException().isThrownBy(() -> validator.validate(request));
    }
}
