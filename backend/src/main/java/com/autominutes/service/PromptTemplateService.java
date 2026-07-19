package com.autominutes.service;

import com.autominutes.dto.PromptTemplateRequest;
import com.autominutes.dto.PromptTemplateResponse;
import com.autominutes.entity.PromptTemplate;
import com.autominutes.exception.ResourceNotFoundException;
import com.autominutes.repository.PromptTemplateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class PromptTemplateService {
    private static final String DEFAULT_PROMPT = """
            You are an assistant that extracts structured meeting outcomes from transcripts.
            Return only valid JSON with this shape:
            {
              "summary": "short concise summary",
              "detailedSummary": ["bullet point"],
              "keyDiscussionPoints": ["discussion point"],
              "decisions": ["decision"],
              "followUpNotes": ["follow-up note"],
              "actionItems": [
                {
                  "description": "task description",
                  "proposedAssignee": "person or null",
                  "deadline": "YYYY-MM-DD or null",
                  "status": "OPEN, IN_PROGRESS, DONE, or UNKNOWN"
                }
              ]
            }
            Do not invent facts. Use UNKNOWN when the transcript does not clearly specify a status.
            """;

    private final PromptTemplateRepository promptTemplateRepository;

    public PromptTemplateService(PromptTemplateRepository promptTemplateRepository) {
        this.promptTemplateRepository = promptTemplateRepository;
    }

    @Transactional
    public PromptTemplateResponse create(PromptTemplateRequest request) {
        if (Boolean.TRUE.equals(request.active())) {
            deactivateAll();
        }
        PromptTemplate promptTemplate = new PromptTemplate();
        promptTemplate.setName(request.name());
        promptTemplate.setPromptText(request.promptText());
        promptTemplate.setActive(request.active() == null || request.active());
        return DtoMapper.toPromptTemplateResponse(promptTemplateRepository.save(promptTemplate));
    }

    @Transactional(readOnly = true)
    public List<PromptTemplateResponse> findAll() {
        return promptTemplateRepository.findAll().stream().map(DtoMapper::toPromptTemplateResponse).toList();
    }

    @Transactional(readOnly = true)
    public PromptTemplateResponse getActiveResponse() {
        return DtoMapper.toPromptTemplateResponse(getOrCreateActivePrompt());
    }

    @Transactional
    public PromptTemplateResponse update(UUID id, PromptTemplateRequest request) {
        PromptTemplate promptTemplate = getPromptTemplate(id);
        if (Boolean.TRUE.equals(request.active())) {
            deactivateAll();
        }
        promptTemplate.setName(request.name());
        promptTemplate.setPromptText(request.promptText());
        promptTemplate.setActive(request.active() == null || request.active());
        return DtoMapper.toPromptTemplateResponse(promptTemplateRepository.save(promptTemplate));
    }

    @Transactional
    public PromptTemplate getOrCreateActivePrompt() {
        return promptTemplateRepository.findFirstByActiveTrueOrderByCreatedAtDesc()
                .orElseGet(() -> {
                    PromptTemplate promptTemplate = new PromptTemplate();
                    promptTemplate.setName("Default Meeting Extraction Prompt");
                    promptTemplate.setPromptText(DEFAULT_PROMPT);
                    promptTemplate.setActive(true);
                    return promptTemplateRepository.save(promptTemplate);
                });
    }

    private PromptTemplate getPromptTemplate(UUID id) {
        return promptTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prompt template not found: " + id));
    }

    private void deactivateAll() {
        List<PromptTemplate> templates = promptTemplateRepository.findAll();
        for (PromptTemplate template : templates) {
            template.setActive(false);
        }
        promptTemplateRepository.saveAll(templates);
    }
}
