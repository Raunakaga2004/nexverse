package com.pio.nexverse.entities.user;

import com.pio.nexverse.entities.BaseEntity;
import com.pio.nexverse.entities.course.Course;
import com.pio.nexverse.entities.organization.Department;
import com.pio.nexverse.enums.CourseAccessRequestStatus;
import com.pio.nexverse.enums.CourseProgressStatus;
import com.pio.nexverse.enums.EnrollmentType;
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
@Table(name = "user_course", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "course_id"}))
public class UserCourse extends BaseEntity {
    @Column(name = "request_note", columnDefinition = "TEXT")
    private String requestNote;

    @Enumerated(EnumType.STRING)
    @Column(name = "enrollment_type", nullable = false)
    private EnrollmentType enrollmentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "course_progress_status", nullable = false)
    private CourseProgressStatus courseProgressStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "access_request_status")
    private CourseAccessRequestStatus accessRequestStatus;

    @Column(name = "progress_percent", nullable = false)
    private Double progressPercent = 0.0;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "last_accessed_at")
    private LocalDateTime lastAccessedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // requested by

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department; // just to know in which dep this course was accessed/requested/assigned

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by")
    private User processedBy;

    @OneToMany(mappedBy = "userCourse", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<UserCourseModule> userCourseModules;
}