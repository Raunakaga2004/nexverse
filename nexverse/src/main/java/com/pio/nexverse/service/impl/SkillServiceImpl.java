package com.pio.nexverse.service.impl;

import com.pio.nexverse.dto.*;
import com.pio.nexverse.entities.course.Skill;
import com.pio.nexverse.entities.organization.Organization;
import com.pio.nexverse.enums.OrganizationFileType;
import com.pio.nexverse.exception.ResourceAlreadyExistsException;
import com.pio.nexverse.exception.SkillIconNotFoundException;
import com.pio.nexverse.exception.SkillNotFoundException;
import com.pio.nexverse.repository.SkillRepository;
import com.pio.nexverse.service.CurrentUserService;
import com.pio.nexverse.service.FileStorageService;
import com.pio.nexverse.service.SkillService;
import com.pio.nexverse.specification.SkillSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

import static com.pio.nexverse.constants.ExceptionMessages.SKILL_NAME_EXIST;

@Service
@Slf4j
@RequiredArgsConstructor
public class SkillServiceImpl implements SkillService {
    private final SkillRepository skillRepository;
    private final CurrentUserService currentUserService;
    private final ModelMapper modelMapper;
    private final FileStorageService fileStorageService;

    @Transactional
    public void createSkill(CreateSkillRequestDTO request, MultipartFile icon) {
        log.info("Creating skill. name = {}", request.getName());
        Organization organization = currentUserService.getCurrentOrganization();
        validateSkillCreation(request, organization.getId());
        Skill skill = modelMapper.map(request, Skill.class);
        skill.setOrganization(organization);
        if (icon != null) {
            String iconPath = fileStorageService.upload(icon, organization.getId(), OrganizationFileType.SKILL_BADGE_ICON);
            skill.setIconUrl(iconPath);
        }
        skillRepository.save(skill);
        log.info("Skill created successfully. skillId = {}, organizationId = {}", skill.getId(), organization.getId());
    }

    @Transactional
    public void updateSkill(Long skillId, UpdateSkillRequestDTO request, MultipartFile icon) {
        log.info("Updating skill. skillId = {}", skillId);
        Skill skill = getSkillEntity(skillId);
        mapRequestToSkillForUpdate(request, skill);
        String oldIcon = skill.getIconUrl();
        String newIconPath = fileStorageService.upload(icon, currentUserService.getCurrentOrganization().getId(), OrganizationFileType.SKILL_BADGE_ICON);
        skill.setIconUrl(newIconPath);
        if (oldIcon != null && !oldIcon.isBlank()) {
            fileStorageService.delete(oldIcon);
        }
        log.info("Skill updated successfully. skillId = {}", skillId);
    }

    @Override
    public Page<SkillResponseDTO> getSkills(Pageable pageable, SkillSearchRequest filter) {
        Pageable adjustedPageable = PageRequest.of(
                Math.max(0, pageable.getPageNumber() - 1),
                pageable.getPageSize(),
                pageable.getSort());
        Organization organization = currentUserService.getCurrentOrganization();
        Specification<Skill> specification = buildSkillSearchSpecification(filter);
        Page<Skill> skills = skillRepository.findAll(specification, adjustedPageable);
        log.info("Successfully retrieved {} skills of organizationId = {}.", skills.getTotalElements(), organization.getId());
        return skills.map(skill -> modelMapper.map(skill, SkillResponseDTO.class));
    }

    private Specification<Skill> buildSkillSearchSpecification(SkillSearchRequest filter) {
        Specification<Skill> specification = SkillSpecification.belongsToOrganization(currentUserService.getCurrentOrganization().getId());
        if (filter.getIsEnabled() != null) {
            specification = specification.and(SkillSpecification.isEnabled(filter.getIsEnabled()));
        }
        if (filter.getSearch() != null && !filter.getSearch().isBlank()) {
            specification = specification.and(SkillSpecification.containsKeyword(filter.getSearch()));
        }
        return specification;
    }

    @Transactional
    public SkillResponseDTO getSkill(Long skillId) {
        return modelMapper.map(getSkillEntity(skillId), SkillResponseDTO.class);
    }

    @Override
    public Resource getSkillIcon(Long skillId) {
        Skill skill = getSkillEntity(skillId);
        if (skill.getIconUrl() == null || skill.getIconUrl().isBlank()) {
            throw new SkillIconNotFoundException();
        }
        return fileStorageService.get(skill.getIconUrl());
    }

    @Transactional
    public void updateActivation(Long skillId, ActivationUpdateRequestDTO updateActivationRequest) {
        Skill skill = getSkillEntity(skillId);
        Boolean previousState = skill.isEnabled();
        skill.setEnabled(updateActivationRequest.getIsEnabled());
        log.info("Skill activation updated. skillId={}, oldState={}, newState={}", skillId, previousState, updateActivationRequest.getIsEnabled());
    }

    private Skill getSkillEntity(Long skillId) {
        Long organizationId = currentUserService.getCurrentOrganization().getId();
        Skill skill = skillRepository.findByIdAndOrganizationId(skillId, organizationId).orElseThrow(SkillNotFoundException::new);
        log.info("Skill retrieved successfully. skillId = {}, organizationId = {}", skillId, organizationId);
        return skill;
    }

    private void validateSkillCreation(CreateSkillRequestDTO request, Long organizationId) {
        if (skillRepository.existsByNameAndOrganizationId(request.getName(), organizationId)) {
            log.warn("Skill creation failed. Skill name already exists. name = {}, organizationId = {}", request.getName(), organizationId);
            throw new ResourceAlreadyExistsException(SKILL_NAME_EXIST);
        }
    }

    private void mapRequestToSkillForUpdate(UpdateSkillRequestDTO request, Skill skill) {
        String name = request.getName();
        if (name != null) {
            Organization organization = currentUserService.getCurrentOrganization();
            if (skillRepository.existsByNameAndOrganizationIdAndIdNot(name, organization.getId(), skill.getId())) {
                throw new ResourceAlreadyExistsException(SKILL_NAME_EXIST);
            }
            Optional.ofNullable(request.getName()).ifPresent(skill::setName);
        }
        Optional.ofNullable(request.getDescription()).ifPresent(skill::setDescription);
        Optional.ofNullable(request.getIconUrl()).ifPresent(skill::setIconUrl);
    }
}