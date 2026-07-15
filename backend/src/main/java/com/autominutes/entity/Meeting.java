package com.autominutes.entity;

import com.autominutes.enums.ProcessingStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
public class Meeting {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    private String title;

    @NotNull
    private LocalDateTime meetingDateTime;

    private String description;

    @Enumerated(EnumType.STRING)
    private ProcessingStatus processingStatus = ProcessingStatus.PENDING;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "meeting")
    private List<Attendee> attendees = new ArrayList<>();

    @OneToOne(mappedBy = "meeting")
    private Transcript transcript;

    @OneToMany(mappedBy = "meeting")
    private List<AIResult> aiResults = new ArrayList<>();

    @OneToMany(mappedBy = "meeting")
    private List<ActionItem> actionItems = new ArrayList<>();

}

