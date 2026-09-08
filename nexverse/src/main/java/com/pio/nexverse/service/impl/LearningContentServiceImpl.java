package com.pio.nexverse.service.impl;

import com.pio.nexverse.dto.TextContentResponseDTO;
import com.pio.nexverse.entities.course.ModuleContent;
import com.pio.nexverse.enums.ContentType;
import com.pio.nexverse.exception.ContentNotFoundException;
import com.pio.nexverse.repository.ModuleContentRepository;
import com.pio.nexverse.service.FileStorageService;
import com.pio.nexverse.service.LearningContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LearningContentServiceImpl implements LearningContentService {
    private final FileStorageService fileStorageService;
    private final ModuleContentRepository moduleContentRepository;

    @Override
    public TextContentResponseDTO getTextContent(Long contentId) {
        ModuleContent content = getContentEntity(contentId);
        if (content.getContentType() == ContentType.TEXT) {
            TextContentResponseDTO textContentResponse = new TextContentResponseDTO();
            textContentResponse.setBody(content.getTextBody());
            return textContentResponse;
        } else {
            throw new ContentNotFoundException();
        }
    }

    @Override
    public ResponseEntity<Resource> getDocumentContent(Long contentId) {
        ModuleContent content = getContentEntity(contentId);
        if (content.getContentType() == ContentType.DOCUMENT) {
            Resource resource = fileStorageService.get(content.getContentUrl());
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).body(resource);
        } else {
            throw new ContentNotFoundException();
        }
    }

    @Override
    public ResponseEntity<ResourceRegion> streamVideo(Long contentId, HttpHeaders headers) throws IOException {
        ModuleContent content = getContentEntity(contentId);
        if (content.getContentType() != ContentType.VIDEO) {
            throw new ContentNotFoundException();
        }
        Resource resource = fileStorageService.get(content.getContentUrl());
        long contentLength = resource.contentLength();
        ResourceRegion region;
        List<HttpRange> ranges = headers.getRange();
        if (ranges.isEmpty()) {
            long chunkSize = Math.min(1024 * 1024, contentLength); // 1 MB
            region = new ResourceRegion(resource, 0, chunkSize);
        } else {
            HttpRange range = ranges.get(0);
            region = range.toResourceRegion(resource);
        }
        return ResponseEntity.status(206)
                .contentType(MediaTypeFactory.getMediaType(resource.getFilename()).orElse(MediaType.valueOf("video/mp4")))
                .body(region);
    }

    private ModuleContent getContentEntity(Long contentId) {
        return moduleContentRepository.findById(contentId).orElseThrow(ContentNotFoundException::new);
    }
}
