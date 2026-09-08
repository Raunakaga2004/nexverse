package com.pio.nexverse.service;

import com.pio.nexverse.dto.EmployeeImportPreviewResponse;
import org.springframework.web.multipart.MultipartFile;

public interface EmployeeImportService {
    EmployeeImportPreviewResponse readEmployees(MultipartFile file);
}
