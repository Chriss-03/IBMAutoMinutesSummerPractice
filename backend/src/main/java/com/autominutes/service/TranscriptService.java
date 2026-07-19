package com.autominutes.service;

import com.autominutes.dto.TranscriptRequest;
import com.autominutes.dto.TranscriptResponse;
import com.autominutes.entity.Meeting;
import com.autominutes.entity.Transcript;
import com.autominutes.exception.ResourceNotFoundException;
import com.autominutes.repository.TranscriptRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class TranscriptService {
    private final TranscriptRepository transcriptRepository;
    private final MeetingService meetingService;

    public TranscriptService(TranscriptRepository transcriptRepository, MeetingService meetingService) {
        this.transcriptRepository = transcriptRepository;
        this.meetingService = meetingService;
    }

    @Transactional
    public TranscriptResponse upsert(UUID meetingId, TranscriptRequest request) {
        Meeting meeting = meetingService.getMeeting(meetingId);
        Transcript transcript = transcriptRepository.findByMeetingId(meetingId).orElseGet(Transcript::new);
        transcript.setMeeting(meeting);
        transcript.setContent(request.content());
        Transcript saved = transcriptRepository.save(transcript);
        meeting.setTranscript(saved);
        return DtoMapper.toTranscriptResponse(saved);
    }

    @Transactional(readOnly = true)
    public TranscriptResponse getByMeeting(UUID meetingId) {
        return DtoMapper.toTranscriptResponse(getTranscriptEntity(meetingId));
    }

    public Transcript getTranscriptEntity(UUID meetingId) {
        meetingService.getMeeting(meetingId);
        return transcriptRepository.findByMeetingId(meetingId)
                .orElseThrow(() -> new ResourceNotFoundException("Transcript not found for meeting: " + meetingId));
    }
}
