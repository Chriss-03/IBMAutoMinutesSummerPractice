package com.autominutes.service;

import com.autominutes.dto.*;
import com.autominutes.entity.*;

import java.util.List;

public final class DtoMapper {
    private DtoMapper() {
    }

    public static MeetingResponse toMeetingResponse(Meeting meeting) {
        return new MeetingResponse(
                meeting.getId(),
                meeting.getTitle(),
                meeting.getMeetingDateTime(),
                meeting.getDescription(),
                meeting.getProcessingStatus(),
                meeting.getTranscript() != null,
                meeting.getCreatedAt(),
                meeting.getUpdatedAt()
        );
    }

    public static AttendeeResponse toAttendeeResponse(Attendee attendee) {
        return new AttendeeResponse(
                attendee.getId(),
                attendee.getName(),
                attendee.getEmail(),
                attendee.getRole(),
                attendee.getMeeting().getId()
        );
    }

    public static TranscriptResponse toTranscriptResponse(Transcript transcript) {
        return new TranscriptResponse(
                transcript.getId(),
                transcript.getMeeting().getId(),
                transcript.getContent(),
                transcript.getCreatedAt(),
                transcript.getUpdatedAt()
        );
    }

    public static ActionItemResponse toActionItemResponse(ActionItem actionItem) {
        return new ActionItemResponse(
                actionItem.getId(),
                actionItem.getDescription(),
                actionItem.getProposedAssignee(),
                actionItem.getDeadline(),
                actionItem.getStatus(),
                actionItem.getMeeting().getId(),
                actionItem.getAiResult() == null ? null : actionItem.getAiResult().getId(),
                actionItem.getCreatedAt(),
                actionItem.getUpdatedAt()
        );
    }

    public static AIResultResponse toAIResultResponse(AIResult aiResult) {
        List<ActionItemResponse> actions = aiResult.getActionItems() == null
                ? List.of()
                : aiResult.getActionItems().stream().map(DtoMapper::toActionItemResponse).toList();

        return new AIResultResponse(
                aiResult.getId(),
                aiResult.getMeeting().getId(),
                aiResult.getPromptTemplate().getId(),
                aiResult.getSummary(),
                aiResult.getDetailedSummary(),
                aiResult.getKeyDiscussionPoints(),
                aiResult.getDecisions(),
                aiResult.getFollowUpNotes(),
                aiResult.getVersionNumber(),
                aiResult.getLatest(),
                aiResult.getCreatedAt(),
                actions
        );
    }

    public static PromptTemplateResponse toPromptTemplateResponse(PromptTemplate promptTemplate) {
        return new PromptTemplateResponse(
                promptTemplate.getId(),
                promptTemplate.getName(),
                promptTemplate.getPromptText(),
                promptTemplate.getActive(),
                promptTemplate.getCreatedAt(),
                promptTemplate.getUpdatedAt()
        );
    }
}
