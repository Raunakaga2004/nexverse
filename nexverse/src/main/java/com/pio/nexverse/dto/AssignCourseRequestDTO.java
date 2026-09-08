package com.pio.nexverse.dto;

import lombok.Data;

import java.util.List;

@Data
public class AssignCourseRequestDTO {
    private Long courseId;
    private List<Long> employeesId;
}