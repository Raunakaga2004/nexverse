package com.pio.nexverse.entities.organization;

import com.pio.nexverse.entities.BaseEntity;
import com.pio.nexverse.entities.course.Course;
import com.pio.nexverse.entities.resourceAccess.DepartmentCourseAccess;
import com.pio.nexverse.entities.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "department", uniqueConstraints = @UniqueConstraint(columnNames = {"name", "organization_id"}))
public class Department extends BaseEntity {
    @Column(name = "name", nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @OneToMany(mappedBy = "owningDepartment", fetch = FetchType.LAZY)
    private List<Course> courses;

    @OneToMany(mappedBy = "requestingDepartment", fetch = FetchType.LAZY)
    private List<DepartmentCourseAccess> courseAccesses;

    @OneToMany(mappedBy = "requestedToDepartment", fetch = FetchType.LAZY)
    private List<DepartmentCourseAccess> departmentCourseAccessRequest;

    @OneToMany(mappedBy = "department", fetch = FetchType.LAZY)
    private List<User> users;
}