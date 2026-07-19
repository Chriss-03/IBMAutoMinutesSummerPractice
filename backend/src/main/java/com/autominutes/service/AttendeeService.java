package com.autominutes.service;

import com.autominutes.dto.AttendeeResponse;
import com.autominutes.dto.CreateAttendeeRequest;
import com.autominutes.dto.UpdateAttendeeRequest;
import com.autominutes.entity.Attendee;
import com.autominutes.entity.Meeting;
import com.autominutes.exception.ResourceNotFoundException;
import com.autominutes.repository.AttendeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AttendeeService {
    private final AttendeeRepository attendeeRepository;
    private final MeetingService meetingService;

    public AttendeeService(AttendeeRepository attendeeRepository, MeetingService meetingService) {
        this.attendeeRepository = attendeeRepository;
        this.meetingService = meetingService;
    }

    @Transactional
    public AttendeeResponse add(UUID meetingId, CreateAttendeeRequest request) {
        Meeting meeting = meetingService.getMeeting(meetingId);
        Attendee attendee = new Attendee();
        attendee.setName(request.name());
        attendee.setEmail(request.email());
        attendee.setRole(request.role());
        attendee.setMeeting(meeting);
        return DtoMapper.toAttendeeResponse(attendeeRepository.save(attendee));
    }

    @Transactional(readOnly = true)
    public List<AttendeeResponse> listByMeeting(UUID meetingId) {
        meetingService.getMeeting(meetingId);
        return attendeeRepository.findByMeetingIdOrderByNameAsc(meetingId)
                .stream()
                .map(DtoMapper::toAttendeeResponse)
                .toList();
    }

    @Transactional
    public AttendeeResponse update(UUID attendeeId, UpdateAttendeeRequest request) {
        Attendee attendee = getAttendee(attendeeId);
        attendee.setName(request.name());
        attendee.setEmail(request.email());
        attendee.setRole(request.role());
        return DtoMapper.toAttendeeResponse(attendeeRepository.save(attendee));
    }

    @Transactional
    public void delete(UUID attendeeId) {
        Attendee attendee = getAttendee(attendeeId);
        attendeeRepository.delete(attendee);
    }

    private Attendee getAttendee(UUID attendeeId) {
        return attendeeRepository.findById(attendeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendee not found: " + attendeeId));
    }
}
