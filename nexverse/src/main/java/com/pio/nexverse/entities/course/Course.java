package com.pio.nexverse.entities.course;

import com.pio.nexverse.entities.BaseEntity;
import com.pio.nexverse.entities.organization.Department;
import com.pio.nexverse.entities.resourceAccess.DepartmentCourseAccess;
import com.pio.nexverse.entities.user.User;
import com.pio.nexverse.entities.user.UserCourse;
import com.pio.nexverse.enums.CourseAccessType;
import com.pio.nexverse.enums.CourseVisibility;
import com.pio.nexverse.enums.Level;
import com.pio.nexverse.enums.ResourceCreationStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "course")
public class Course extends BaseEntity {
    @Column(name="title", nullable = false)
    private String title;

    @Column(name = "short_description", columnDefinition = "TEXT")
    private String shortDescription;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = false)
    private Level level;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", nullable = false)
    private CourseVisibility visibility;

    @Enumerated(EnumType.STRING)
    @Column(name = "access_type", nullable = false)
    private CourseAccessType accessType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ResourceCreationStatus status;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "estimated_duration_seconds", nullable = false)
    private Integer estimatedDurationSeconds;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "published_by")
    private User publishedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owning_department_id", nullable = false)
    private Department owningDepartment;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<CourseModule> modules = new ArrayList<>();

    @OneToMany(mappedBy = "course", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CourseSkill> courseSkills;

    @OneToMany(mappedBy = "course", fetch = FetchType.LAZY)
    private List<DepartmentCourseAccess> departmentCourseAccesses;

    @OneToMany(mappedBy = "course", fetch = FetchType.LAZY)
    private List<UserCourse> userCourses;
}