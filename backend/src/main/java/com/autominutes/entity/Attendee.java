package com.autominutes.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Entity
public class Attendee {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;
    private String email;
    private String role;

    @ManyToOne
    @JoinColumn(name = "meetingId")
    private Meeting meeting;
}
