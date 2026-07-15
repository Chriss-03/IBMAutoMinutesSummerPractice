package com.autominutes.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class Transcript {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @Column(length = 4000)
    private String content;

    @OneToOne
    @JoinColumn(name = "meetingId")
    private Meeting meeting;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
