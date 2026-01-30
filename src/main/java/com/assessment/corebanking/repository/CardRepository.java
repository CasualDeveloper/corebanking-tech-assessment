package com.assessment.corebanking.repository;

import com.assessment.corebanking.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardRepository extends JpaRepository<Card, Long> {
}
