package com.autominutes.service;

import com.autominutes.dto.AIResultResponse;
import com.autominutes.entity.*;
import com.autominutes.enums.ActionItemStatus;
import com.autominutes.enums.ProcessingStatus;
import com.autominutes.exception.AiProcessingException;
import com.autominutes.exception.BadRequestException;
import com.autominutes.exception.ResourceNotFoundException;
import com.autominutes.llm.LlmActionItem;
import com.autominutes.llm.LlmClient;
import com.autominutes.llm.LlmMeetingResult;
import com.autominutes.repository.AIResultRepository;
import com.autominutes.repository.ActionItemRepository;
import com.autominutes.repository.MeetingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class AIProcessingService {
    private final MeetingService meetingService;
    private final TranscriptService transcriptService;
    private final PromptTemplateService promptTemplateService;
    private final LlmClient llmClient;
    private final AIResultRepository aiResultRepository;
    private final ActionItemRepository actionItemRepository;
    private final MeetingRepository meetingRepository;

    public AIProcessingService(MeetingService meetingService,
                               TranscriptService transcriptService,
                               PromptTemplateService promptTemplateService,
                               LlmClient llmClient,
                               AIResultRepository aiResultRepository,
                               ActionItemRepository actionItemRepository,
                               MeetingRepository meetingRepository) {
        this.meetingService = meetingService;
        this.transcriptService = transcriptService;
        this.promptTemplateService = promptTemplateService;
        this.llmClient = llmClient;
        this.aiResultRepository = aiResultRepository;
        this.actionItemRepository = actionItemRepository;
        this.meetingRepository = meetingRepository;
    }

    @Transactional(noRollbackFor = AiProcessingException.class)
    public AIResultResponse process(UUID meetingId) {
        return runProcessing(meetingId);
    }

    @Transactional(noRollbackFor = AiProcessingException.class)
    public AIResultResponse reprocess(UUID meetingId) {
        return runProcessing(meetingId);
    }

    @Transactional(readOnly = true)
    public List<AIResultResponse> listByMeeting(UUID meetingId) {
        meetingService.getMeeting(meetingId);
        return aiResultRepository.findByMeetingIdOrderByVersionNumberDesc(meetingId)
                .stream()
                .map(DtoMapper::toAIResultResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AIResultResponse latest(UUID meetingId) {
        meetingService.getMeeting(meetingId);
        AIResult result = aiResultRepository.findFirstByMeetingIdAndLatestTrue(meetingId)
                .orElseThrow(() -> new ResourceNotFoundException("No AI result found for meeting: " + meetingId));
        return DtoMapper.toAIResultResponse(result);
    }

    @Transactional(readOnly = true)
    public AIResultResponse getById(UUID aiResultId) {
        AIResult result = aiResultRepository.findById(aiResultId)
                .orElseThrow(() -> new ResourceNotFoundException("AI result not found: " + aiResultId));
        return DtoMapper.toAIResultResponse(result);
    }

    private AIResultResponse runProcessing(UUID meetingId) {
        Meeting meeting = meetingService.getMeeting(meetingId);
        Transcript transcript = transcriptService.getTranscriptEntity(meetingId);

        if (transcript.getContent() == null || transcript.getContent().isBlank()) {
            throw new BadRequestException("Transcript must not be empty when processing is requested");
        }

        PromptTemplate promptTemplate = promptTemplateService.getOrCreateActivePrompt();
        meeting.setProcessingStatus(ProcessingStatus.PROCESSING);
        meetingRepository.save(meeting);

        try {
            LlmMeetingResult llmResult = llmClient.processTranscript(promptTemplate.getPromptText(), transcript.getContent());
            aiResultRepository.findFirstByMeetingIdAndLatestTrue(meetingId)
                    .ifPresent(existing -> {
                        existing.setLatest(false);
                        aiResultRepository.save(existing);
                    });

            int nextVersion = aiResultRepository.findFirstByMeetingIdOrderByVersionNumberDesc(meetingId)
                    .map(result -> result.getVersionNumber() + 1)
                    .orElse(1);

            AIResult aiResult = new AIResult();
            aiResult.setMeeting(meeting);
            aiResult.setPromptTemplate(promptTemplate);
            aiResult.setVersionNumber(nextVersion);
            aiResult.setLatest(true);
            aiResult.setSummary(nullToEmpty(llmResult.summary()));
            aiResult.setDetailedSummary(joinLines(llmResult.detailedSummary()));
            aiResult.setKeyDiscussionPoints(joinLines(llmResult.keyDiscussionPoints()));
            aiResult.setDecisions(joinLines(llmResult.decisions()));
            aiResult.setFollowUpNotes(joinLines(llmResult.followUpNotes()));
            aiResult.setRawModelResponse(llmResult.rawResponse());

            AIResult savedResult = aiResultRepository.save(aiResult);
            List<ActionItem> actionItems = createActionItems(llmResult.actionItems(), meeting, savedResult);
            actionItemRepository.saveAll(actionItems);
            savedResult.setActionItems(actionItems);

            meeting.setProcessingStatus(ProcessingStatus.COMPLETED);
            meetingRepository.save(meeting);
            return DtoMapper.toAIResultResponse(savedResult);
        } catch (RuntimeException exception) {
            meeting.setProcessingStatus(ProcessingStatus.FAILED);
            meetingRepository.save(meeting);
            if (exception instanceof AiProcessingException aiProcessingException) {
                throw aiProcessingException;
            }
            throw new AiProcessingException("AI processing failed", exception);
        }
    }

    private List<ActionItem> createActionItems(List<LlmActionItem> drafts, Meeting meeting, AIResult aiResult) {
        if (drafts == null) {
            return List.of();
        }

        return drafts.stream()
                .filter(draft -> draft.description() != null && !draft.description().isBlank())
                .map(draft -> {
                    ActionItem actionItem = new ActionItem();
                    actionItem.setMeeting(meeting);
                    actionItem.setAiResult(aiResult);
                    actionItem.setDescription(draft.description());
                    actionItem.setProposedAssignee(draft.proposedAssignee());
                    actionItem.setDeadline(parseDeadline(draft.deadline()));
                    actionItem.setStatus(parseStatus(draft.status()));
                    return actionItem;
                })
                .toList();
    }

    private ActionItemStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return ActionItemStatus.UNKNOWN;
        }
        try {
            return ActionItemStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            return ActionItemStatus.UNKNOWN;
        }
    }

    private LocalDate parseDeadline(String deadline) {
        if (deadline == null || deadline.isBlank() || "null".equalsIgnoreCase(deadline)) {
            return null;
        }
        try {
            return LocalDate.parse(deadline);
        } catch (Exception exception) {
            return null;
        }
    }

    private String joinLines(List<String> values) {
        if (values == null || values.isEmpty()) {
            return "";
        }
        return String.join("\n", values);
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
