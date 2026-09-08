package com.pio.nexverse.entities.user;

import com.pio.nexverse.entities.BaseEntity;
import com.pio.nexverse.entities.course.CourseModule;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "user_course_module", uniqueConstraints = @UniqueConstraint(columnNames = {"user_course_id", "course_module_id"}))
public class UserCourseModule extends BaseEntity {
    @Column(name = "progress_percent", nullable = false)
    private Double progressPercent = 0.0;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "last_accessed_at")
    private LocalDateTime lastAccessedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_course_id")
    private UserCourse userCourse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_module_id")
    private CourseModule courseModule;

    @OneToMany(mappedBy = "userCourseModule", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<UserModuleContent> userModuleContents;
}