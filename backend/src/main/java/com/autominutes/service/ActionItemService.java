package com.autominutes.service;

import com.autominutes.dto.ActionItemResponse;
import com.autominutes.dto.UpdateActionItemRequest;
import com.autominutes.entity.ActionItem;
import com.autominutes.exception.ResourceNotFoundException;
import com.autominutes.repository.ActionItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ActionItemService {
    private final ActionItemRepository actionItemRepository;
    private final MeetingService meetingService;

    public ActionItemService(ActionItemRepository actionItemRepository, MeetingService meetingService) {
        this.actionItemRepository = actionItemRepository;
        this.meetingService = meetingService;
    }

    @Transactional(readOnly = true)
    public List<ActionItemResponse> listByMeeting(UUID meetingId) {
        meetingService.getMeeting(meetingId);
        return actionItemRepository.findByMeetingIdOrderByCreatedAtAsc(meetingId)
                .stream()
                .map(DtoMapper::toActionItemResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ActionItemResponse> listByAiResult(UUID aiResultId) {
        return actionItemRepository.findByAiResultIdOrderByCreatedAtAsc(aiResultId)
                .stream()
                .map(DtoMapper::toActionItemResponse)
                .toList();
    }

    @Transactional
    public ActionItemResponse update(UUID actionItemId, UpdateActionItemRequest request) {
        ActionItem actionItem = actionItemRepository.findById(actionItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Action item not found: " + actionItemId));
        actionItem.setDescription(request.description());
        actionItem.setProposedAssignee(request.proposedAssignee());
        actionItem.setDeadline(request.deadline());
        actionItem.setStatus(request.status());
        return DtoMapper.toActionItemResponse(actionItemRepository.save(actionItem));
    }
}
