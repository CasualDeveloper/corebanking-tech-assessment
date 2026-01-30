package com.assessment.corebanking.service;

import com.assessment.corebanking.dto.CardRequest;
import com.assessment.corebanking.enums.CardType;
import org.springframework.stereotype.Component;

@Component
public class CardDomainValidator {

    public void validate(CardRequest request) {
        if (request == null || request.getCardType() == null) {
            return;
        }
        if (request.getCardType() != CardType.CREDIT && request.getCreditLimit() != null) {
            throw new IllegalArgumentException("creditLimit is only allowed for CREDIT cards");
        }
    }
}
