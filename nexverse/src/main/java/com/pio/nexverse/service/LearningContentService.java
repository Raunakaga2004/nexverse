package com.pio.nexverse.service;

import com.pio.nexverse.dto.TextContentResponseDTO;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

public interface LearningContentService {
    TextContentResponseDTO getTextContent(Long contentId);

    ResponseEntity<Resource> getDocumentContent(Long contentId);

    ResponseEntity<ResourceRegion> streamVideo(Long contentId, HttpHeaders headers) throws IOException;
}