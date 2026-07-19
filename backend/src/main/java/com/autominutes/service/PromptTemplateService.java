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
        You are an assistant that extracts structured meeting outcomes from meeting transcripts.

        Return only valid JSON.
        Do not include Markdown.
        Do not wrap the JSON in ```json.
        Do not include explanations before or after the JSON.
        Do not invent facts that are not present in the transcript.

        The JSON must exactly match this structure:
        {
          "summary": "short concise summary",
          "detailedSummary": ["bullet point"],
          "keyDiscussionPoints": ["discussion point"],
          "decisions": ["decision"],
          "followUpNotes": ["follow-up note"],
          "actionItems": [
            {
              "description": "task description",
              "proposedAssignee": "person name or null",
              "deadline": "YYYY-MM-DD or null",
              "status": "OPEN"
            }
          ]
        }

        Rules:
        - summary must be a single string.
        - detailedSummary must be an array of strings.
        - keyDiscussionPoints must be an array of strings.
        - decisions must be an array of strings.
        - followUpNotes must be an array of strings.
        - actionItems must be an array. Use an empty array if there are no action items.
        - proposedAssignee must be null if no assignee is clearly mentioned.
        - deadline must be null if no clear date is mentioned.
        - status must be one of: OPEN, IN_PROGRESS, DONE, UNKNOWN.
        - Use UNKNOWN only when the status is unclear.
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
