package com.autominutes.entity;

import com.autominutes.enums.ActionItemStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class ActionItem {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(length = 1000)
    private String description;

    @Column(length = 255)
    private String proposedAssignee;

    private LocalDate deadline;

    @Enumerated(EnumType.STRING)
    private ActionItemStatus status;

    @ManyToOne
    @JoinColumn(name = "meetingId")
    private Meeting meeting;

    @ManyToOne
    @JoinColumn(name = "aiResultId")
    private AIResult aiResult;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
