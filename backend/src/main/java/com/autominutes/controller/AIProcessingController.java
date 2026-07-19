package com.autominutes.controller;

import com.autominutes.dto.AIResultResponse;
import com.autominutes.service.AIProcessingService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class AIProcessingController {
    private final AIProcessingService aiProcessingService;

    public AIProcessingController(AIProcessingService aiProcessingService) {
        this.aiProcessingService = aiProcessingService;
    }

    @PostMapping("/api/meetings/{meetingId}/process")
    public AIResultResponse process(@PathVariable UUID meetingId) {
        return aiProcessingService.process(meetingId);
    }

    @PostMapping("/api/meetings/{meetingId}/reprocess")
    public AIResultResponse reprocess(@PathVariable UUID meetingId) {
        return aiProcessingService.reprocess(meetingId);
    }

    @GetMapping("/api/meetings/{meetingId}/ai-results")
    public List<AIResultResponse> listByMeeting(@PathVariable UUID meetingId) {
        return aiProcessingService.listByMeeting(meetingId);
    }

    @GetMapping("/api/meetings/{meetingId}/ai-results/latest")
    public AIResultResponse latest(@PathVariable UUID meetingId) {
        return aiProcessingService.latest(meetingId);
    }

    @GetMapping("/api/ai-results/{aiResultId}")
    public AIResultResponse getById(@PathVariable UUID aiResultId) {
        return aiProcessingService.getById(aiResultId);
    }
}
