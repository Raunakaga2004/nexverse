package com.pio.nexverse.service.impl;

import com.pio.nexverse.dto.*;
import com.pio.nexverse.entities.auth.UserSetPasswordToken;
import com.pio.nexverse.entities.organization.Organization;
import com.pio.nexverse.entities.user.User;
import com.pio.nexverse.enums.*;
import com.pio.nexverse.exception.*;
import com.pio.nexverse.repository.OrganizationRepository;
import com.pio.nexverse.repository.UserRepository;
import com.pio.nexverse.repository.UserSetPasswordRepository;
import com.pio.nexverse.service.EmailService;
import com.pio.nexverse.service.FileStorageService;
import com.pio.nexverse.service.OrganizationService;
import com.pio.nexverse.specification.OrganizationSpecification;
import com.pio.nexverse.utils.TokenUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.pio.nexverse.constants.ExceptionMessages.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizationServiceImpl implements OrganizationService {
    private final ModelMapper modelMapper;
    private final OrganizationRepository organizationRepository;
    private final UserSetPasswordRepository userSetPasswordRepository;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    @Transactional
    public void createOrganization(CreateOrganizationRequestDTO request, MultipartFile logo) {
        log.info("Creating organization. name = {}, email = {}, phone = {}", request.getName(), request.getEmail(), request.getPhone());
        validateOrganizationCreation(request);
        Organization organization = modelMapper.map(request, Organization.class);
        organization.setStatus(OrganizationStatus.PENDING);
        organizationRepository.save(organization);
        if (logo != null) {
            String logoPath = fileStorageService.upload(logo, organization.getId(), OrganizationFileType.ORGANIZATION_LOGO);
            organization.setLogoUrl(logoPath);
        }
        createOrgAdmin(request, organization);
        log.info("Organization created successfully. organizationId = {}", organization.getId());
    }

    @Transactional
    public void updateOrganization(Long organizationId, UpdateOrganizationRequestDTO request, MultipartFile logo) {
        log.info("Updating organization. organizationId = {}", organizationId);
        Organization organization = getOrganizationEntity(organizationId);
        mapRequestToOrganizationForUpdate(request, organization);
        if (logo != null) {
            if (organization.getLogoUrl() != null && !organization.getLogoUrl().isBlank()) {
                fileStorageService.delete(organization.getLogoUrl());
            }
            String logoPath = fileStorageService.upload(logo, organization.getId(), OrganizationFileType.ORGANIZATION_LOGO);
            organization.setLogoUrl(logoPath);
        }
        log.info("Organization updated successfully. organizationId = {}", organizationId);
    }

    public Page<OrganizationsResponseDTO> getOrganizations(OrganizationSearchRequestDTO filter, Pageable pageable) {
        Pageable adjustedPageable = PageRequest.of(
                Math.max(0, pageable.getPageNumber() - 1),
                pageable.getPageSize(),
                pageable.getSort());
        Specification<Organization> specification = buildOrganizationSearchSpecification(filter);
        Page<Organization> organizations = organizationRepository.findAll(specification, adjustedPageable);
        log.info("Successfully retrieved {} organizations.", organizations.getTotalElements());
        return organizations.map(org -> modelMapper.map(org, OrganizationsResponseDTO.class));
    }

    private Specification<Organization> buildOrganizationSearchSpecification(OrganizationSearchRequestDTO filter) {
        Specification<Organization> specification = (root, query, cb) -> cb.conjunction();
        if (filter.getIsEnabled() != null) {
            specification = specification.and(OrganizationSpecification.isEnabled(filter.getIsEnabled()));
        }
        if (filter.getStatus() != null) {
            specification = specification.and(OrganizationSpecification.hasStatus(filter.getStatus()));
        }
        if (filter.getSearch() != null && !filter.getSearch().isBlank()) {
            specification = specification.and(OrganizationSpecification.containsKeyword(filter.getSearch()));
        }
        return specification;
    }

    public OrganizationResponseDTO getOrganization(Long organizationId) {
        Organization organization = getOrganizationEntity(organizationId);
        OrganizationResponseDTO orgResponse = modelMapper.map(organization, OrganizationResponseDTO.class);
        User admin = organization.getUsers().stream()
                .filter((user) -> user.getRole() == Role.ADMIN)
                .findFirst()
                .orElseThrow(UserNotFoundException::new);
        orgResponse.setOrgAdmin(modelMapper.map(admin, OrgAdminResponseDTO.class));
        orgResponse.setTotalStorageBytes(fileStorageService.getTotalOrgFileStorageBytes(organizationId));
        return orgResponse;
    }

    public Resource getOrganizationLogo(Long organizationId) {
        Organization organization = getOrganizationEntity(organizationId);
        return fileStorageService.get(organization.getLogoUrl());
    }

    @Transactional
    public void suspendOrganization(Long organizationId) {
        Organization organization = getOrganizationEntity(organizationId);
        if (organization.getStatus() == OrganizationStatus.SUSPENDED) {
            throw new OrganizationSuspendedException();
        }
        if (organization.getStatus() == OrganizationStatus.PENDING) {
            throw new OrganizationPendingException();
        }
        organization.setStatus(OrganizationStatus.SUSPENDED);
    }

    @Transactional
    public void reactivateOrganization(Long organizationId) {
        Organization organization = getOrganizationEntity(organizationId);
        if (organization.getStatus() != OrganizationStatus.SUSPENDED) {
            throw new OrganizationSuspendedException();
        }
        organization.setStatus(OrganizationStatus.ACTIVE);
    }

    @Transactional
    public void updateActivation(Long organizationId, ActivationUpdateRequestDTO updateActivationRequest) {
        Organization organization = getOrganizationEntity(organizationId);
        if (organization.getStatus() == OrganizationStatus.PENDING) {
            User orgAdmin = getOrganizationAdmin(organization);
            sendAccountActivationEmail(orgAdmin, organization);
        }
        organization.setEnabled(updateActivationRequest.getIsEnabled());
    }

    private User getOrganizationAdmin(Organization organization) {
        List<User> orgUsers = organization.getUsers();
        return orgUsers.stream().filter((orgUser) -> orgUser.getRole() == Role.ADMIN).findFirst().orElseThrow(UserNotFoundException::new);
    }

    private void validateOrganizationCreation(CreateOrganizationRequestDTO request) {
        if (organizationRepository.existsByEmailIgnoreCaseAndIsEnabledTrue(request.getEmail())) {
            log.warn("Organization creation failed. Organization's email already exists. email = {}", request.getName());
            throw new ResourceAlreadyExistsException(ORG_EMAIL_EXIST);
        }
        if (organizationRepository.existsByPhoneAndIsEnabledTrue(request.getPhone())) {
            log.warn("Organization creation failed. Organization's phone number already exists. name = {}, phone = {}", request.getName(), request.getPhone());
            throw new ResourceAlreadyExistsException(ORG_PHONE_EXIST);
        }
    }

    private Organization getOrganizationEntity(Long organizationId) {
        Organization organization = organizationRepository.findById(organizationId).orElseThrow(OrganizationNotFoundException::new);
        log.info("Organization retrieved successfully. organizationId = {}", organizationId);
        return organization;
    }

    private void mapRequestToOrganizationForUpdate(UpdateOrganizationRequestDTO request, Organization organization) {
        Optional.ofNullable(request.getName()).ifPresent(organization::setName);
        if (request.getEmail() != null) {
            if (organizationRepository.existsByEmailIgnoreCaseAndIsEnabledTrueAndIdNot(request.getEmail(), organization.getId())) {
                throw new ResourceAlreadyExistsException(ORG_EMAIL_EXIST);
            }
            organization.setEmail(request.getEmail());
        }
        Optional.ofNullable(request.getAddress()).ifPresent(organization::setAddress);
        Optional.ofNullable(request.getCity()).ifPresent(organization::setCity);
        Optional.ofNullable(request.getState()).ifPresent(organization::setState);
        Optional.ofNullable(request.getZipCode()).ifPresent(organization::setZipCode);
        Optional.ofNullable(request.getCountry()).ifPresent(organization::setCountry);
    }

    private void createOrgAdmin(CreateOrganizationRequestDTO request, Organization organization) {
        log.info("Creating organization admin for organization {}", organization.getId());
        User orgAdmin = mapRequestToUser(request, organization);
        userRepository.save(orgAdmin);
        sendAccountActivationEmail(orgAdmin, organization);
        log.info("Organization admin created for organization {}", organization.getId());
    }

    private void sendAccountActivationEmail(User orgAdmin, Organization organization) {
        String token = TokenUtils.generatePasswordSetToken();
        UserSetPasswordToken userSetPasswordToken = userSetPasswordRepository.findByUserId(orgAdmin.getId())
                .orElse(new UserSetPasswordToken(TokenUtils.hashToken(token), LocalDateTime.now().plusDays(1), TokenType.ACCOUNT_ACTIVATION, orgAdmin));
        userSetPasswordRepository.save(userSetPasswordToken);
        emailService.sendAccountActivationEmail(orgAdmin.getEmail(), token, organization.getName());
    }

    private User mapRequestToUser(CreateOrganizationRequestDTO request, Organization organization) {
        User user = new User();
        user.setFirstName(request.getOrgAdminFirstName());
        user.setLastName(request.getOrgAdminLastName());
        user.setRole(Role.ADMIN);
        user.setStatus(UserStatus.PENDING);
        if (userRepository.existsByEmailIgnoreCaseAndIsEnabledTrue(request.getOrgAdminEmail()))
            throw new ResourceAlreadyExistsException(USER_EMAIL_EXIST);
        user.setEmail(request.getOrgAdminEmail());
        if (userRepository.existsByPhoneNumberAndIsEnabledTrue(request.getOrgAdminPhone()))
            throw new ResourceAlreadyExistsException(USER_PHONE_NUMBER_EXIST);
        user.setPhoneNumber(request.getOrgAdminPhone());
        if (userRepository.existsByEmployeeCodeAndOrganizationIdAndIsEnabledTrue(request.getOrgAdminEmployeeCode(), organization.getId()))
            throw new ResourceAlreadyExistsException(USER_EMP_CODE_EXIST);
        user.setEmployeeCode(request.getOrgAdminEmployeeCode());
        user.setJobTitle(request.getOrgAdminJobTitle());
        user.setOrganization(organization);
        return user;
    }
}