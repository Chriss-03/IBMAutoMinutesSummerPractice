package com.autominutes.service;

import com.autominutes.dto.CreateMeetingRequest;
import com.autominutes.dto.MeetingResponse;
import com.autominutes.dto.UpdateMeetingRequest;
import com.autominutes.entity.Meeting;
import com.autominutes.enums.ProcessingStatus;
import com.autominutes.exception.ResourceNotFoundException;
import com.autominutes.repository.MeetingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class MeetingService {
    private final MeetingRepository meetingRepository;

    public MeetingService(MeetingRepository meetingRepository) {
        this.meetingRepository = meetingRepository;
    }

    @Transactional
    public MeetingResponse create(CreateMeetingRequest request) {
        Meeting meeting = new Meeting();
        meeting.setTitle(request.title());
        meeting.setMeetingDateTime(request.meetingDateTime());
        meeting.setDescription(request.description());
        meeting.setProcessingStatus(ProcessingStatus.PENDING);
        return DtoMapper.toMeetingResponse(meetingRepository.save(meeting));
    }

    @Transactional(readOnly = true)
    public List<MeetingResponse> findAll() {
        return meetingRepository.findAll().stream().map(DtoMapper::toMeetingResponse).toList();
    }

    @Transactional(readOnly = true)
    public MeetingResponse findById(UUID id) {
        return DtoMapper.toMeetingResponse(getMeeting(id));
    }

    @Transactional
    public MeetingResponse update(UUID id, UpdateMeetingRequest request) {
        Meeting meeting = getMeeting(id);
        meeting.setTitle(request.title());
        meeting.setMeetingDateTime(request.meetingDateTime());
        meeting.setDescription(request.description());
        return DtoMapper.toMeetingResponse(meetingRepository.save(meeting));
    }

    @Transactional
    public void delete(UUID id) {
        Meeting meeting = getMeeting(id);
        meetingRepository.delete(meeting);
    }

    public Meeting getMeeting(UUID id) {
        return meetingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meeting not found: " + id));
    }
}
