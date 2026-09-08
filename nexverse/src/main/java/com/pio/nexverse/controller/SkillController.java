package com.pio.nexverse.controller;

import com.pio.nexverse.dto.*;
import com.pio.nexverse.service.SkillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

import static com.pio.nexverse.constants.SuccessResponseMessages.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/skill")
@PreAuthorize("hasAuthority('ADMIN')")
public class SkillController {
    private final SkillService skillService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponseDTO<Void>> createSkill(@Valid @RequestPart("skill") CreateSkillRequestDTO request, @RequestPart(value = "icon", required = false) MultipartFile icon) {
        log.info("Create skill request received. name = {}", request.getName());
        skillService.createSkill(request, icon);
        log.info("Skill created successfully. name = {}", request.getName());
        ApiResponseDTO<Void> response = ApiResponseDTO.<Void>builder()
                .message(SKILL_CREATED_SUCCESS)
                .build();
        return ResponseEntity.ok(response);
    }

    @PatchMapping(value = "/{skillId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponseDTO<Void>> updateSkill(@PathVariable Long skillId, @Valid @RequestPart(value = "skill", required = false) UpdateSkillRequestDTO request, @RequestPart(value = "icon", required = false) MultipartFile icon) {
        log.info("Update skill request received. skillId = {}", skillId);
        skillService.updateSkill(skillId, request, icon);
        log.info("Skill updated successfully. skillId = {}", skillId);
        ApiResponseDTO<Void> response = ApiResponseDTO.<Void>builder()
                .message(SKILL_UPDATED_SUCCESS)
                .build();
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    @GetMapping
    public ResponseEntity<ApiResponseDTO<Page<SkillResponseDTO>>> getSkills(@PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable, @ModelAttribute SkillSearchRequest filter) {
        log.info("Fetching skills. pageable = {}, filter = {}", pageable, filter);
        Page<SkillResponseDTO> skillResponse = skillService.getSkills(pageable, filter);
        ApiResponseDTO<Page<SkillResponseDTO>> response = ApiResponseDTO.<Page<SkillResponseDTO>>builder()
                .message(SKILLS_RETRIEVED_SUCCESS)
                .data(skillResponse)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{skillId}")
    public ResponseEntity<ApiResponseDTO<SkillResponseDTO>> getSkill(@PathVariable Long skillId) {
        log.info("Fetching skill. skillId = {}", skillId);
        SkillResponseDTO skillResponse = skillService.getSkill(skillId);
        log.info("Skill retrieved successfully. skillId = {}", skillId);
        ApiResponseDTO<SkillResponseDTO> response = ApiResponseDTO.<SkillResponseDTO>builder()
                .message(SKILL_RETRIEVED_SUCCESS)
                .data(skillResponse)
                .build();
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{skillId}/activation")
    public ResponseEntity<ApiResponseDTO<Void>> updateActivation(@PathVariable Long skillId, @Valid @RequestBody ActivationUpdateRequestDTO updateActivationRequest) {
        log.info("Update activation skill request received. skillId = {}, enabled = {}", skillId, updateActivationRequest.getIsEnabled());
        skillService.updateActivation(skillId, updateActivationRequest);
        log.info("Skill activation changed. skillId = {}", skillId);
        ApiResponseDTO<Void> response = ApiResponseDTO.<Void>builder()
                .message(updateActivationRequest.getIsEnabled() ? SKILL_ENABLED_SUCCESS : SKILL_DISABLED_SUCCESS)
                .build();
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'EMPLOYEE')")
    @GetMapping("/{skillId}/icon")
    public ResponseEntity<Resource> getSkillIcon(@PathVariable Long skillId) throws IOException {
        log.info("Request for fetching icon received. skillId = {}", skillId);
        Resource resource = skillService.getSkillIcon(skillId);
        log.info("Fetched skill icon. skillId = {}", skillId);
        String contentType;
        try (InputStream is = resource.getInputStream()) {
            contentType = URLConnection.guessContentTypeFromStream(is);
        }
        if (contentType == null) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }
}