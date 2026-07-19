package com.autominutes.repository;

import com.autominutes.entity.PromptTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PromptTemplateRepository extends JpaRepository<PromptTemplate, UUID> {
    Optional<PromptTemplate> findFirstByActiveTrueOrderByCreatedAtDesc();
}
