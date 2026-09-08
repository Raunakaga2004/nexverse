package com.pio.nexverse.service.impl;

import com.pio.nexverse.config.FtpProperties;
import com.pio.nexverse.enums.OrganizationFileType;
import com.pio.nexverse.exception.FileStorageException;
import com.pio.nexverse.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FtpFileStorageServiceImpl implements FileStorageService {
    private final FtpProperties ftpProperties;

    @Value("${file.validation.profile-image.max-size}")
    private DataSize profileImageMaxSize;

    @Value("${file.validation.course-thumbnail.max-size}")
    private DataSize courseThumbnailMaxSize;

    @Value("${file.validation.course-content.pdf.max-size}")
    private DataSize courseContentPdfMaxSize;

    @Value("${file.validation.course-content.mp4.max-size}")
    private DataSize courseContentMp4MaxSize;

    @Value("${file.validation.skill-badge.max-size}")
    private DataSize skillBadgeMaxSize;

    @Value("${file.validation.organization-logo.max-size}")
    private DataSize organizationLogoMaxSize;

    @Override
    public String upload(MultipartFile file, Long organizationId, OrganizationFileType fileType) {
        FTPClient ftpClient = null;
        try {
            validateFile(file, fileType);
            ftpClient = createConnection();
            String extension = getExtension(file);
            System.out.println(extension);
            String storedFileName = UUID.randomUUID() + (extension.isBlank() ? "" : "." + extension);
            String filePath = buildPath(organizationId, fileType, storedFileName);
            createDirectories(ftpClient, filePath);
            try (InputStream inputStream = file.getInputStream()) {
                boolean uploaded = ftpClient.storeFile(filePath, inputStream);
                if (!uploaded) {
                    throw new FileStorageException();
                }
            }
            log.info("Uploaded file '{}' for organization {}.", storedFileName, organizationId);
            return filePath;
        } catch (IOException exception) {
            log.error("Failed to upload file for organization {}.", organizationId, exception);
            throw new FileStorageException();
        } finally {
            disconnect(ftpClient);
        }
    }

    @Override
    public Resource get(String filePath) {
        FTPClient ftpClient = null;
        try {
            ftpClient = createConnection();
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                boolean retrieved = ftpClient.retrieveFile(filePath, outputStream);
                if (!retrieved) {
                    throw new FileStorageException();
                }
                log.info("Retrieved file = {}", filePath);
                return new ByteArrayResource(outputStream.toByteArray());
            }
        } catch (IOException exception) {
            log.error("Failed to retrieve file '{}'.", filePath, exception);
            throw new FileStorageException();
        } finally {
            disconnect(ftpClient);
        }
    }

    @Override
    public void delete(String filePath) {
        FTPClient ftpClient = null;
        try {
            ftpClient = createConnection();
            boolean deleted = ftpClient.deleteFile(filePath);

            if (!deleted) {
                log.error(
                        "Delete failed. Path={}, ReplyCode={}, Reply={}",
                        filePath,
                        ftpClient.getReplyCode(),
                        ftpClient.getReplyString()
                );

                throw new FileStorageException(
                        "Failed to delete file. FTP Reply: " +
                                ftpClient.getReplyString()
                );
            }
            log.info("Deleted file '{}'.", filePath);
        } catch (IOException exception) {
            log.error("Failed to delete file '{}'.", filePath, exception);
            throw new FileStorageException();
        } finally {
            disconnect(ftpClient);
        }
    }

    @Override
    public long getTotalFileStorageBytes() { // can improve this (by storing the org files directly in db or files metadata)
        FTPClient ftpClient = null;
        try {
            ftpClient = createConnection();
            long size = calculateDirectorySize(ftpClient, ftpProperties.getBaseDirectory() + "/organizations");
            log.info("Total size of organizations data is {} bytes", size);
            return size;
        } catch (IOException exception) {
            throw new FileStorageException();
        } finally {
            disconnect(ftpClient);
        }
    }

    @Override
    public long getTotalOrgFileStorageBytes(Long organizationId) {
        FTPClient ftpClient = null;
        try {
            ftpClient = createConnection();
            long size = calculateDirectorySize(ftpClient, ftpProperties.getBaseDirectory() + "/organizations/" + organizationId);
            log.info("Total size of organization {} data is {} bytes", organizationId, size);
            return size;
        } catch (IOException exception) {
            throw new FileStorageException();
        } finally {
            disconnect(ftpClient);
        }
    }

    private long calculateDirectorySize(FTPClient ftpClient, String directoryPath) throws IOException {
        long totalSize = 0;
        FTPFile[] files = ftpClient.listFiles(directoryPath);
        if (files == null) {
            return totalSize;
        }
        for (FTPFile file : files) {
            String name = file.getName();
            if (".".equals(name) || "..".equals(name)) continue;
            String fullPath = directoryPath + "/" + name;
            if (file.isFile()) {
                totalSize += file.getSize();
            } else if (file.isDirectory()) {
                totalSize += calculateDirectorySize(ftpClient, fullPath);
            }
        }
        return totalSize;
    }

    private FTPClient createConnection() {
        FTPClient ftpClient;
        try {
            ftpClient = new FTPClient();
            ftpClient.connect(ftpProperties.getHost(), ftpProperties.getPort());
            boolean loggedIn = ftpClient.login(ftpProperties.getUsername(), ftpProperties.getPassword());
            if (!loggedIn) {
                throw new IllegalStateException("Unable to login to FTP server.");
            }
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            return ftpClient;
        } catch (IOException e) {
            throw new FileStorageException();
        }
    }

    private void disconnect(FTPClient ftpClient) {
        if (ftpClient == null) return;
        try {
            if (ftpClient.isConnected()) {
                ftpClient.logout();
                ftpClient.disconnect();
            }
        } catch (IOException exception) {
            throw new FileStorageException();
        }
    }

    private String getExtension(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1);
    }

    private String buildPath(Long organizationId, OrganizationFileType fileType, String storedFileName) {
        return String.format(
                "%s/organizations/%d/%s/%s",
                ftpProperties.getBaseDirectory(),
                organizationId,
                fileType.getFolder(),
                storedFileName
        );
    }

    private void createDirectories(FTPClient ftpClient, String filePath) throws IOException {
        String directory = filePath.substring(0, filePath.lastIndexOf("/"));
        String[] folders = directory.split("/");
        StringBuilder currentPath = new StringBuilder();
        for (String folder : folders) {
            if (folder.isBlank()) continue;
            currentPath.append("/").append(folder);
            ftpClient.makeDirectory(currentPath.toString());
            log.info("Created directory. directory = {}", folder);
        }
    }

    private void validateFile(MultipartFile file, OrganizationFileType fileType) {
        String extension = getExtension(file).toLowerCase();
        long fileSize = file.getSize();
        switch (fileType) {
            case PROFILE_IMAGE -> {
                validateExtension(extension, "png", "jpg", "jpeg");
                validateMaxSize(fileSize, profileImageMaxSize.toBytes());
            }
            case COURSE_THUMBNAIL -> {
                validateExtension(extension, "png", "jpg", "jpeg");
                validateMaxSize(fileSize, courseThumbnailMaxSize.toBytes());
            }
            case COURSE_CONTENT -> {
                validateExtension(extension, "mp4", "pdf");
                if ("mp4".equals(extension)) {
                    validateMaxSize(fileSize, courseContentMp4MaxSize.toBytes());
                } else {
                    validateMaxSize(fileSize, courseContentPdfMaxSize.toBytes());
                }
            }
            case SKILL_BADGE_ICON -> {
                validateExtension(extension, "png", "svg");
                validateMaxSize(fileSize, skillBadgeMaxSize.toBytes());
            }
            case ORGANIZATION_LOGO -> {
                validateExtension(extension, "png", "jpg", "jpeg", "svg");
                validateMaxSize(fileSize, organizationLogoMaxSize.toBytes());
            }
            default -> throw new FileStorageException("Unsupported file type.");
        }
    }

    private void validateExtension(String extension, String... allowedExtensions) {
        boolean valid = Arrays.stream(allowedExtensions)
                .anyMatch(ext -> ext.equalsIgnoreCase(extension));
        if (!valid) {
            throw new FileStorageException("Invalid file format.");
        }
    }

    private void validateMaxSize(long actualSize, long maxSize) {
        if (actualSize > maxSize) {
            throw new FileStorageException("File size exceeds allowed limit.");
        }
    }
}