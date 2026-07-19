package com.autominutes.controller;

import com.autominutes.dto.TranscriptRequest;
import com.autominutes.dto.TranscriptResponse;
import com.autominutes.service.TranscriptService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/meetings/{meetingId}/transcript")
public class TranscriptController {
    private final TranscriptService transcriptService;

    public TranscriptController(TranscriptService transcriptService) {
        this.transcriptService = transcriptService;
    }

    @PutMapping
    public TranscriptResponse upsert(@PathVariable UUID meetingId, @Valid @RequestBody TranscriptRequest request) {
        return transcriptService.upsert(meetingId, request);
    }

    @GetMapping
    public TranscriptResponse get(@PathVariable UUID meetingId) {
        return transcriptService.getByMeeting(meetingId);
    }
}
