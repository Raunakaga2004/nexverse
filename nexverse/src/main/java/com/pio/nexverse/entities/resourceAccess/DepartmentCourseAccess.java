package com.pio.nexverse.entities.resourceAccess;

import com.pio.nexverse.entities.BaseEntity;
import com.pio.nexverse.entities.course.Course;
import com.pio.nexverse.entities.organization.Department;
import com.pio.nexverse.entities.user.User;
import com.pio.nexverse.enums.CourseAccessRequestStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "department_course_access")
public class DepartmentCourseAccess extends BaseEntity {
    @Column(name = "request_note", columnDefinition = "TEXT")
    private String requestNote;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_status")
    private CourseAccessRequestStatus requestStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    private User requestedBy; // just for auditing the requesting person

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requesting_department_id", nullable = false)
    private Department requestingDepartment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_to_department_id", nullable = false)
    private Department requestedToDepartment;

    @ManyToOne(fetch = FetchType.LAZY)
    private User processedBy;
}