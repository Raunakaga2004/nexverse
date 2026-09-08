package com.pio.nexverse.service;

import com.pio.nexverse.enums.OrganizationFileType;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String upload(MultipartFile file, Long organizationId, OrganizationFileType fileType);

    Resource get(String filePath);

    void delete(String filePath);

    long getTotalFileStorageBytes();

    long getTotalOrgFileStorageBytes(Long organizationId);
}