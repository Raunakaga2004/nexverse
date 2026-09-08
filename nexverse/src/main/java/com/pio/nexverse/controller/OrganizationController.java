package com.pio.nexverse.controller;

import com.pio.nexverse.dto.*;
import com.pio.nexverse.service.OrganizationService;
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
@RequestMapping("/api/v1/organization")
@PreAuthorize("hasAuthority('SUPER_ADMIN')")
public class OrganizationController {
    private final OrganizationService organizationService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponseDTO<Void>> createOrganization(@Valid @RequestPart("organization") CreateOrganizationRequestDTO request, @RequestPart(value = "logoUrl") MultipartFile logo) {
        log.info("Create organization request received.");
        organizationService.createOrganization(request, logo);
        ApiResponseDTO<Void> response = ApiResponseDTO.<Void>builder()
                .message(ORGANIZATION_CREATED_SUCCESS)
                .build();
        return ResponseEntity.ok(response);
    }

    @PatchMapping(value = "/{organizationId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponseDTO<Void>> updateOrganization(@PathVariable Long organizationId, @Valid @RequestPart("organization") UpdateOrganizationRequestDTO request, @RequestPart(value = "logoUrl", required = false) MultipartFile logo) {
        log.info("Update organization request received. organizationId = {}", organizationId);
        organizationService.updateOrganization(organizationId, request, logo);
        ApiResponseDTO<Void> response = ApiResponseDTO.<Void>builder()
                .message(ORGANIZATION_UPDATED_SUCCESS)
                .build();
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{organizationId}/suspend")
    public ResponseEntity<ApiResponseDTO<Void>> suspendOrganization(@PathVariable Long organizationId) {
        log.info("Suspend organization request received. organizationId = {}", organizationId);
        organizationService.suspendOrganization(organizationId);
        ApiResponseDTO<Void> response = ApiResponseDTO.<Void>builder()
                .message(ORGANIZATION_SUSPENDED_SUCCESS)
                .build();
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{organizationId}/reactivate")
    public ResponseEntity<ApiResponseDTO<Void>> reactivateOrganization(@PathVariable Long organizationId) {
        log.info("Reactivate organization request received. organizationId = {}", organizationId);
        organizationService.reactivateOrganization(organizationId);
        ApiResponseDTO<Void> response = ApiResponseDTO.<Void>builder()
                .message(ORGANIZATION_REACTIVATE_SUCCESS)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<ApiResponseDTO<Page<OrganizationsResponseDTO>>> getOrganizations(@ModelAttribute OrganizationSearchRequestDTO filter, @PageableDefault(page = 1, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("Fetch organization request received. filters={}, pageable={}", filter, pageable);
        Page<OrganizationsResponseDTO> orgResponse = organizationService.getOrganizations(filter, pageable);
        ApiResponseDTO<Page<OrganizationsResponseDTO>> response = ApiResponseDTO.<Page<OrganizationsResponseDTO>>builder()
                .message(ORGANIZATIONS_RETRIEVED_SUCCESS)
                .data(orgResponse)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{organizationId}")
    public ResponseEntity<ApiResponseDTO<OrganizationResponseDTO>> getOrganization(@PathVariable Long organizationId) {
        log.info("Fetch organization request received. organizationId = {}", organizationId);
        OrganizationResponseDTO orgResponse = organizationService.getOrganization(organizationId);
        ApiResponseDTO<OrganizationResponseDTO> response = ApiResponseDTO.<OrganizationResponseDTO>builder()
                .message(ORGANIZATION_RETRIEVED_SUCCESS)
                .data(orgResponse)
                .build();
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{organizationId}/activation")
    public ResponseEntity<ApiResponseDTO<Void>> updateActivation(@PathVariable Long organizationId, @Valid @RequestBody ActivationUpdateRequestDTO updateActivationRequest) {
        log.info("Update activation organization request received. organizationId = {}, enabled = {}", organizationId, updateActivationRequest.getIsEnabled());
        organizationService.updateActivation(organizationId, updateActivationRequest);
        ApiResponseDTO<Void> response = ApiResponseDTO.<Void>builder()
                .message(updateActivationRequest.getIsEnabled() ? ORGANIZATION_ENABLE_SUCCESS : ORGANIZATION_DISABLED_SUCCESS)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{organizationId}/logo")
    public ResponseEntity<Resource> getOrganizationLogo(@PathVariable Long organizationId) throws IOException {
        log.info("Fetching organization logo request received. orgId={}", organizationId);
        Resource resource = organizationService.getOrganizationLogo(organizationId);
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