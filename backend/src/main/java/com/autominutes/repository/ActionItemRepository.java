package com.autominutes.repository;

import com.autominutes.entity.ActionItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ActionItemRepository extends JpaRepository<ActionItem, UUID> {
    List<ActionItem> findByMeetingIdOrderByCreatedAtAsc(UUID meetingId);
    List<ActionItem> findByAiResultIdOrderByCreatedAtAsc(UUID aiResultId);
}
