package com.pio.nexverse.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class EmployeeDashboardProgressDTO {
    private List<Integer> years;
    private List<Integer> months;
    private List<Long> completedCourses;
}