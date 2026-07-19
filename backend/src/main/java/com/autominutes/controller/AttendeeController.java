package com.autominutes.controller;

import com.autominutes.dto.AttendeeResponse;
import com.autominutes.dto.CreateAttendeeRequest;
import com.autominutes.dto.UpdateAttendeeRequest;
import com.autominutes.service.AttendeeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class AttendeeController {
    private final AttendeeService attendeeService;

    public AttendeeController(AttendeeService attendeeService) {
        this.attendeeService = attendeeService;
    }

    @PostMapping("/api/meetings/{meetingId}/attendees")
    @ResponseStatus(HttpStatus.CREATED)
    public AttendeeResponse add(@PathVariable UUID meetingId, @Valid @RequestBody CreateAttendeeRequest request) {
        return attendeeService.add(meetingId, request);
    }

    @GetMapping("/api/meetings/{meetingId}/attendees")
    public List<AttendeeResponse> listByMeeting(@PathVariable UUID meetingId) {
        return attendeeService.listByMeeting(meetingId);
    }

    @PutMapping("/api/attendees/{attendeeId}")
    public AttendeeResponse update(@PathVariable UUID attendeeId, @Valid @RequestBody UpdateAttendeeRequest request) {
        return attendeeService.update(attendeeId, request);
    }

    @DeleteMapping("/api/attendees/{attendeeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID attendeeId) {
        attendeeService.delete(attendeeId);
    }
}
