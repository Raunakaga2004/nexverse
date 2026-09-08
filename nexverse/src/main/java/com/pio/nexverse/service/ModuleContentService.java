package com.pio.nexverse.service;

import com.pio.nexverse.dto.*;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ModuleContentService {
    void addContent(Long moduleId, CreateModuleContentRequestDTO request, MultipartFile content);

    void updateContent(Long contentId, UpdateModuleContentRequestDTO request, MultipartFile content);

    void deleteContent(Long contentId);

    void reorderContents(Long moduleId, ReorderModuleContentsRequestDTO request);

    ModuleContentResponseDTO getContent(Long contentId);

    ResponseEntity<Resource> getDocumentContent(Long contentId);

    ResponseEntity<ResourceRegion> streamVideo(Long contentId, HttpHeaders headers) throws IOException;

    TextContentResponseDTO getTextContent(Long contentId);
}