package com.pio.nexverse.dto;

import com.pio.nexverse.enums.CourseAccessRequestStatus;
import lombok.Data;

@Data
public class ChangeRequestStatusDTO {
    private CourseAccessRequestStatus status;
}
