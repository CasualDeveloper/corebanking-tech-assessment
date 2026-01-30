package com.assessment.corebanking.dto;

import java.util.List;

public class CardNotificationResponse {
    private Long cardId;
    private List<ExternalPost> notifications;

    public CardNotificationResponse() {
    }

    public Long getCardId() {
        return cardId;
    }

    public void setCardId(Long cardId) {
        this.cardId = cardId;
    }

    public List<ExternalPost> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<ExternalPost> notifications) {
        this.notifications = notifications;
    }
}
