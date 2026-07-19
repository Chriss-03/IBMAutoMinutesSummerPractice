package com.autominutes.controller;

import com.autominutes.dto.CreateMeetingRequest;
import com.autominutes.dto.MeetingResponse;
import com.autominutes.dto.UpdateMeetingRequest;
import com.autominutes.service.MeetingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/meetings")
public class MeetingController {
    private final MeetingService meetingService;

    public MeetingController(MeetingService meetingService) {
        this.meetingService = meetingService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MeetingResponse create(@Valid @RequestBody CreateMeetingRequest request) {
        return meetingService.create(request);
    }

    @GetMapping
    public List<MeetingResponse> findAll() {
        return meetingService.findAll();
    }

    @GetMapping("/{meetingId}")
    public MeetingResponse findById(@PathVariable UUID meetingId) {
        return meetingService.findById(meetingId);
    }

    @PutMapping("/{meetingId}")
    public MeetingResponse update(@PathVariable UUID meetingId, @Valid @RequestBody UpdateMeetingRequest request) {
        return meetingService.update(meetingId, request);
    }

    @DeleteMapping("/{meetingId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID meetingId) {
        meetingService.delete(meetingId);
    }
}
