package com.autominutes.controller;

import com.autominutes.dto.PromptTemplateRequest;
import com.autominutes.dto.PromptTemplateResponse;
import com.autominutes.service.PromptTemplateService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/prompt-templates")
public class PromptTemplateController {
    private final PromptTemplateService promptTemplateService;

    public PromptTemplateController(PromptTemplateService promptTemplateService) {
        this.promptTemplateService = promptTemplateService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PromptTemplateResponse create(@Valid @RequestBody PromptTemplateRequest request) {
        return promptTemplateService.create(request);
    }

    @GetMapping
    public List<PromptTemplateResponse> findAll() {
        return promptTemplateService.findAll();
    }

    @GetMapping("/active")
    public PromptTemplateResponse active() {
        return promptTemplateService.getActiveResponse();
    }

    @PutMapping("/{promptTemplateId}")
    public PromptTemplateResponse update(@PathVariable UUID promptTemplateId, @Valid @RequestBody PromptTemplateRequest request) {
        return promptTemplateService.update(promptTemplateId, request);
    }
}
