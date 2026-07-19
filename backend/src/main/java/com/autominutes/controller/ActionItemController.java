package com.autominutes.controller;

import com.autominutes.dto.ActionItemResponse;
import com.autominutes.dto.UpdateActionItemRequest;
import com.autominutes.service.ActionItemService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class ActionItemController {
    private final ActionItemService actionItemService;

    public ActionItemController(ActionItemService actionItemService) {
        this.actionItemService = actionItemService;
    }

    @GetMapping("/api/meetings/{meetingId}/action-items")
    public List<ActionItemResponse> listByMeeting(@PathVariable UUID meetingId) {
        return actionItemService.listByMeeting(meetingId);
    }

    @GetMapping("/api/ai-results/{aiResultId}/action-items")
    public List<ActionItemResponse> listByAiResult(@PathVariable UUID aiResultId) {
        return actionItemService.listByAiResult(aiResultId);
    }

    @PutMapping("/api/action-items/{actionItemId}")
    public ActionItemResponse update(@PathVariable UUID actionItemId, @Valid @RequestBody UpdateActionItemRequest request) {
        return actionItemService.update(actionItemId, request);
    }
}
