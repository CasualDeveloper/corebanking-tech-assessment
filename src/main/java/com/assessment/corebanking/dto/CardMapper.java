package com.assessment.corebanking.dto;

import com.assessment.corebanking.entity.Card;

public final class CardMapper {
    private CardMapper() {
    }

    public static Card toEntity(CardRequest request) {
        if (request == null) {
            return null;
        }
        Card card = new Card();
        card.setCardNumber(request.getCardNumber());
        card.setCardholderName(request.getCardholderName());
        card.setExpiryDate(request.getExpiryDate());
        card.setStatus(request.getStatus());
        card.setCardType(request.getCardType());
        card.setCreditLimit(request.getCreditLimit());
        card.setBalance(request.getBalance());
        return card;
    }

    public static void updateEntity(CardRequest request, Card card) {
        if (request == null || card == null) {
            return;
        }
        card.setCardNumber(request.getCardNumber());
        card.setCardholderName(request.getCardholderName());
        card.setExpiryDate(request.getExpiryDate());
        card.setStatus(request.getStatus());
        card.setCardType(request.getCardType());
        card.setCreditLimit(request.getCreditLimit());
        card.setBalance(request.getBalance());
    }

    public static CardResponse toResponse(Card card) {
        if (card == null) {
            return null;
        }
        CardResponse response = new CardResponse();
        response.setId(card.getId());
        response.setCardNumber(mask(card.getCardNumber()));
        response.setCardholderName(card.getCardholderName());
        response.setExpiryDate(card.getExpiryDate());
        response.setStatus(card.getStatus());
        response.setCardType(card.getCardType());
        response.setCreditLimit(card.getCreditLimit());
        response.setBalance(card.getBalance());
        response.setCreatedAt(card.getCreatedAt());
        response.setUpdatedAt(card.getUpdatedAt());
        return response;
    }

    private static String mask(String cardNumber) {
        if (cardNumber == null) {
            return null;
        }
        int length = cardNumber.length();
        if (length <= 4) {
            return cardNumber;
        }
        String suffix = cardNumber.substring(length - 4);
        return "*".repeat(length - 4) + suffix;
    }
}
