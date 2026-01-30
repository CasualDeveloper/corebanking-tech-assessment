package com.assessment.corebanking.service;

import com.assessment.corebanking.dto.CardMapper;
import com.assessment.corebanking.dto.CardRequest;
import com.assessment.corebanking.entity.Card;
import com.assessment.corebanking.repository.CardRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CardService {

    private static final int PAGE_SIZE = 10;

    private final CardRepository cardRepository;
    private final CardDomainValidator cardDomainValidator;

    public CardService(CardRepository cardRepository, CardDomainValidator cardDomainValidator) {
        this.cardRepository = cardRepository;
        this.cardDomainValidator = cardDomainValidator;
    }

    @Transactional
    public Card createCard(CardRequest request) {
        cardDomainValidator.validate(request);
        Card card = CardMapper.toEntity(request);
        return cardRepository.save(card);
    }

    @Transactional
    public Card updateCard(Long id, CardRequest request) {
        cardDomainValidator.validate(request);
        Card card = cardRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Card not found: " + id));
        CardMapper.updateEntity(request, card);
        return cardRepository.save(card);
    }

    @Transactional
    public Card getCardById(Long id) {
        return cardRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Card not found: " + id));
    }

    @Transactional
    public Page<Card> getAllCards(Pageable pageable) {
        Pageable fixed = PageRequest.of(pageable.getPageNumber(), PAGE_SIZE, pageable.getSort());
        return cardRepository.findAll(fixed);
    }

    @Transactional
    public void deleteCard(Long id) {
        cardRepository.deleteById(id);
    }
}
