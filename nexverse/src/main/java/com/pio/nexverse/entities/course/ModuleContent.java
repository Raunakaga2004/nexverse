package com.pio.nexverse.entities.course;

import com.pio.nexverse.entities.BaseEntity;
import com.pio.nexverse.entities.user.User;
import com.pio.nexverse.enums.ContentType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "module_content", uniqueConstraints = @UniqueConstraint(columnNames = {"module_id", "sequence_order"}))
public class ModuleContent extends BaseEntity {
    @Column(name = "title")
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", nullable = false)
    private ContentType contentType;

    @Column(name = "sequence_order", nullable = false)
    private Integer sequenceOrder;

    @Column(name = "is_mandatory", nullable = false)
    private Boolean isMandatory = true;

    @Column(name = "estimated_duration_seconds", nullable = false)
    private Integer estimatedDurationSeconds;

    @Column(name = "content_url")
    private String contentUrl; // for document url and video url

    @Column(name = "text_body", columnDefinition = "TEXT")
    private String textBody;

    @Column(name = "format")
    private String format; // mimetype for video and document / format for text

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id", nullable = false)
    private CourseModule courseModule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;
}