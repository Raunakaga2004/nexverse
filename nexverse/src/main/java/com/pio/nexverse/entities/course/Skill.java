package com.pio.nexverse.entities.course;

import com.pio.nexverse.entities.BaseEntity;
import com.pio.nexverse.entities.organization.Organization;
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
@Table(name = "skill", uniqueConstraints = @UniqueConstraint(columnNames = {"name", "organization_id"}))
public class Skill extends BaseEntity {
    @Column(name = "name", nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;
    private String iconUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @OneToMany(mappedBy = "skill", fetch = FetchType.LAZY)
    private List<CourseSkill> courseSkills;
}