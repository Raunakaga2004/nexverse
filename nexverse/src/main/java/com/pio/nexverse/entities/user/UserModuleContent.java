package com.pio.nexverse.entities.user;

import com.pio.nexverse.entities.BaseEntity;
import com.pio.nexverse.entities.course.ModuleContent;
import com.pio.nexverse.enums.CompletionStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "user_module_content", uniqueConstraints = @UniqueConstraint(columnNames = {"user_course_module_id", "module_content_id"}))
public class UserModuleContent extends BaseEntity {
    @Column(name = "progress", nullable = false)
    @Enumerated(EnumType.STRING)
    private CompletionStatus progress;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "last_accessed_at")
    private LocalDateTime lastAccessedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_course_module_id", nullable = false)
    private UserCourseModule userCourseModule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_content_id", nullable = false)
    private ModuleContent moduleContent;
}