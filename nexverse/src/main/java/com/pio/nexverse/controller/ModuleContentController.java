package com.pio.nexverse.controller;

import com.pio.nexverse.dto.*;
import com.pio.nexverse.service.ModuleContentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static com.pio.nexverse.constants.SuccessResponseMessages.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@PreAuthorize("hasAuthority('MANAGER')")
public class ModuleContentController {
    private final ModuleContentService moduleContentService;

    @PostMapping(value = "/course-module/{moduleId}/content", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponseDTO<Void>> addContent(
            @PathVariable Long moduleId,
            @Valid @RequestPart("request") CreateModuleContentRequestDTO request,
            @RequestPart(value = "content", required = false) MultipartFile content
    ) {
        moduleContentService.addContent(moduleId, request, content);
        ApiResponseDTO<Void> response = ApiResponseDTO.<Void>builder()
                .message(MODULE_CONTENT_CREATED_SUCCESS)
                .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping(value = "/module-content/{contentId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponseDTO<Void>> updateContent(
            @PathVariable Long contentId,
            @Valid @RequestPart("request") UpdateModuleContentRequestDTO request,
            @RequestPart(value = "content", required = false) MultipartFile content
    ) {
        moduleContentService.updateContent(contentId, request, content);
        ApiResponseDTO<Void> response = ApiResponseDTO.<Void>builder()
                .message(MODULE_CONTENT_UPDATED_SUCCESS)
                .build();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/module-content/{contentId}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteContent(@PathVariable Long contentId) {
        moduleContentService.deleteContent(contentId);
        ApiResponseDTO<Void> response = ApiResponseDTO.<Void>builder()
                .message(MODULE_CONTENT_DELETED_SUCCESS)
                .build();
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyAuthority('MANAGER', 'ADMIN')")
    @GetMapping("/module-content/{contentId}")
    public ResponseEntity<ApiResponseDTO<ModuleContentResponseDTO>> getContent(@PathVariable Long contentId) {
        ModuleContentResponseDTO moduleContent = moduleContentService.getContent(contentId);
        ApiResponseDTO<ModuleContentResponseDTO> response = ApiResponseDTO.<ModuleContentResponseDTO>builder()
                .message(MODULE_CONTENT_RETRIEVED_SUCCESS)
                .data(moduleContent)
                .build();
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyAuthority('MANAGER', 'ADMIN', 'EMPLOYEE)")
    @GetMapping("/module-content/{contentId}/document")
    public ResponseEntity<Resource> getContentFile(@PathVariable Long contentId) {
        return moduleContentService.getDocumentContent(contentId);
    }

    @PreAuthorize("hasAnyAuthority('MANAGER', 'ADMIN', 'EMPLOYEE')")
    @GetMapping("/module-content/{contentId}/text ")
    public ResponseEntity<ApiResponseDTO<TextContentResponseDTO>> getTextContent(@PathVariable Long contentId) {
        log.info("Request received to get text content. contentId={}", contentId);
        TextContentResponseDTO response = moduleContentService.getTextContent(contentId);
        return ResponseEntity.ok(ApiResponseDTO.<TextContentResponseDTO>builder()
                .message(CONTENT_RETRIEVED_SUCCESS)
                .data(response)
                .build()
        );
    }

    @PreAuthorize("hasAnyAuthority('MANAGER', 'ADMIN', 'EMPLOYEE')")
    @GetMapping("/module-content/{contentId}/video")
    public ResponseEntity<ResourceRegion> streamVideo(@PathVariable Long contentId, @RequestHeader HttpHeaders headers) throws IOException {
        return moduleContentService.streamVideo(contentId, headers);
    }

    @PutMapping("/course/modules/{moduleId}/contents/order")
    public ResponseEntity<ApiResponseDTO<Void>> reorderModuleContents(@PathVariable Long moduleId, @Valid @RequestBody ReorderModuleContentsRequestDTO request) {
        moduleContentService.reorderContents(moduleId, request);
        ApiResponseDTO<Void> response = ApiResponseDTO.<Void>builder()
                .message(MODULE_CONTENTS_REORDERED_SUCCESS)
                .build();
        return ResponseEntity.ok(response);
    }
}