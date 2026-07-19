package com.autominutes.repository;

import com.autominutes.entity.AIResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AIResultRepository extends JpaRepository<AIResult, UUID> {
    List<AIResult> findByMeetingIdOrderByVersionNumberDesc(UUID meetingId);
    Optional<AIResult> findFirstByMeetingIdAndLatestTrue(UUID meetingId);
    Optional<AIResult> findFirstByMeetingIdOrderByVersionNumberDesc(UUID meetingId);
}
