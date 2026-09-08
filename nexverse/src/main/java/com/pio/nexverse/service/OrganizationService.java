package com.pio.nexverse.service;

import com.pio.nexverse.dto.*;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface OrganizationService {
    void createOrganization(CreateOrganizationRequestDTO request, MultipartFile logo);

    void updateOrganization(Long organizationId, UpdateOrganizationRequestDTO request, MultipartFile logo);

    OrganizationResponseDTO getOrganization(Long organizationId);

    Resource getOrganizationLogo(Long organizationId);

    void suspendOrganization(Long organizationId);

    void reactivateOrganization(Long organizationId);

    void updateActivation(Long organizationId, ActivationUpdateRequestDTO updateActivationRequest);

    Page<OrganizationsResponseDTO> getOrganizations(OrganizationSearchRequestDTO filter, Pageable pageable);
}