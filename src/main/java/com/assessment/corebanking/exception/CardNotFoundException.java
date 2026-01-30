package com.assessment.corebanking.exception;

public class CardNotFoundException extends RuntimeException {

    private final Long cardId;

    public CardNotFoundException(Long cardId) {
        super("Card not found: " + cardId);
        this.cardId = cardId;
    }

    public Long getCardId() {
        return cardId;
    }
}
