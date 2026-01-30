package com.assessment.corebanking.controller;

import com.assessment.corebanking.dto.CardMapper;
import com.assessment.corebanking.dto.CardRequest;
import com.assessment.corebanking.dto.CardResponse;
import com.assessment.corebanking.entity.Card;
import com.assessment.corebanking.service.CardService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cards")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @GetMapping
    public Page<CardResponse> getCards(Pageable pageable) {
        Page<Card> cards = cardService.getAllCards(pageable);
        return cards.map(CardMapper::toResponse);
    }

    @GetMapping("/{id}")
    public CardResponse getCard(@PathVariable Long id) {
        Card card = cardService.getCardById(id);
        return CardMapper.toResponse(card);
    }

    @PostMapping
    public ResponseEntity<CardResponse> createCard(@Valid @RequestBody CardRequest request) {
        Card created = cardService.createCard(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(CardMapper.toResponse(created));
    }

    @PutMapping("/{id}")
    public CardResponse updateCard(@PathVariable Long id, @Valid @RequestBody CardRequest request) {
        Card updated = cardService.updateCard(id, request);
        return CardMapper.toResponse(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        cardService.deleteCard(id);
        return ResponseEntity.noContent().build();
    }
}
