package com.autominutes.repository;

import com.autominutes.entity.Attendee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AttendeeRepository extends JpaRepository<Attendee, UUID> {
    List<Attendee> findByMeetingIdOrderByNameAsc(UUID meetingId);
}
