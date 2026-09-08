package com.pio.nexverse.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum OrganizationFileType {
    PROFILE_IMAGE("profile-images"),
    COURSE_THUMBNAIL("course-thumbnails"),
    COURSE_CONTENT("course-content"),
    SKILL_BADGE_ICON("skill-badges"),
    ORGANIZATION_LOGO("organization-logo");

    private final String folder;
}