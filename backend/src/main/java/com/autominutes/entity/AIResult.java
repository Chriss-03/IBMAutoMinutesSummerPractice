package com.autominutes.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
public class AIResult {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(length = 4000)
    private String summary;

    @Column(length = 4000)
    private String detailedSummary;

    @Column(length = 4000)
    private String decisions;

    @Column(length = 4000)
    private String followUpNotes;

    private Integer versionNumber;
    private Boolean latest;

    @ManyToOne
    @JoinColumn(name = "meetingId")
    private Meeting meeting;

    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "promptTemplateId")
    private PromptTemplate promptTemplate;

    @OneToMany(mappedBy = "aiResult")
    private List<ActionItem> actionItems;





}
