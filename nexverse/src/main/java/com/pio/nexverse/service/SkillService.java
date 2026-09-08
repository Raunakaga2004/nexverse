package com.pio.nexverse.service;

import com.pio.nexverse.dto.*;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface SkillService {
    void createSkill(CreateSkillRequestDTO request, MultipartFile icon);

    void updateSkill(Long skillId, UpdateSkillRequestDTO request, MultipartFile icon);

    Page<SkillResponseDTO> getSkills(Pageable pageable, SkillSearchRequest filter);

    SkillResponseDTO getSkill(Long skillId);

    Resource getSkillIcon(Long skillId);

    void updateActivation(Long skillId, ActivationUpdateRequestDTO updateActivationRequest);
}