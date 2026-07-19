package com.autominutes.repository;

import com.autominutes.entity.Transcript;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TranscriptRepository extends JpaRepository<Transcript, UUID> {
    Optional<Transcript> findByMeetingId(UUID meetingId);
    boolean existsByMeetingId(UUID meetingId);
}
