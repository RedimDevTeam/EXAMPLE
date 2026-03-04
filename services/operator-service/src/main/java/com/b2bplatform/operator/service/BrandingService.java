package com.b2bplatform.operator.service;

import com.b2bplatform.operator.dto.request.UpdateBrandingRequest;
import com.b2bplatform.operator.dto.response.BrandingResponse;
import com.b2bplatform.operator.model.*;
import com.b2bplatform.operator.repository.*;
import com.b2bplatform.operator.util.RequestContextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing branding assets (logos).
 * Handles file uploads and stores file paths/URLs.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BrandingService {
    
    private final OperatorBrandingRepository operatorBrandingRepository;
    private final OperatorGameProviderBrandingRepository providerBrandingRepository;
    private final OperatorGameBrandingRepository gameBrandingRepository;
    private final OperatorRepository operatorRepository;
    private final OperatorAuditService auditService;
    
    private static final String UPLOAD_DIR = "uploads/branding/";
    
    // ==================== Operator Branding ====================
    
    @Transactional
    public BrandingResponse uploadOperatorLogo(
        Long operatorId, LogoType logoType, MultipartFile file, Integer displayOrder) throws IOException {
        log.info("Uploading operator logo for operator {}, type {}", operatorId, logoType);
        
        // Verify operator exists
        if (!operatorRepository.existsById(operatorId)) {
            throw new IllegalArgumentException("Operator not found: " + operatorId);
        }
        
        // Validate file
        validateFile(file);
        
        // Save file
        String filePath = saveFile(operatorId, "operator", logoType.name(), file);
        
        // Create branding record
        OperatorBranding branding = new OperatorBranding();
        branding.setOperatorId(operatorId);
        branding.setLogoType(logoType);
        branding.setFileName(file.getOriginalFilename());
        branding.setFilePath(filePath);
        branding.setFileFormat(getFileFormat(file.getContentType()));
        branding.setFileSize(file.getSize());
        branding.setDisplayOrder(displayOrder != null ? displayOrder : 0);
        branding.setCreatedBy(RequestContextUtil.getAuthenticatedUser());
        
        OperatorBranding saved = operatorBrandingRepository.save(branding);
        
        // Log audit event
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("logoType", saved.getLogoType());
        newValues.put("fileName", saved.getFileName());
        newValues.put("filePath", saved.getFilePath());
        
        auditService.logAuditEvent(
            operatorId,
            "OPERATOR_BRANDING_UPLOADED",
            String.format("Uploaded operator logo type %s for operator %d", logoType, operatorId),
            RequestContextUtil.getAuthenticatedUser(),
            RequestContextUtil.getClientIpAddress(),
            RequestContextUtil.getRequestId(),
            null,
            newValues
        );
        
        return mapToBrandingResponse(saved);
    }
    
    public List<BrandingResponse> getOperatorBranding(Long operatorId, LogoType logoType) {
        log.info("Getting operator branding for operator {}, type {}", operatorId, logoType);
        
        List<OperatorBranding> branding;
        if (logoType != null) {
            branding = operatorBrandingRepository.findByOperatorIdAndLogoTypeAndIsActiveTrueOrderByDisplayOrderAsc(
                operatorId, logoType);
        } else {
            branding = operatorBrandingRepository.findByOperatorIdAndIsActiveTrueOrderByDisplayOrderAsc(operatorId);
        }
        
        return branding.stream()
            .map(this::mapToBrandingResponse)
            .collect(Collectors.toList());
    }
    
    @Transactional
    public BrandingResponse updateOperatorBranding(
        Long operatorId, LogoType logoType, Integer displayOrder, UpdateBrandingRequest request) {
        log.info("Updating operator branding for operator {}, type {}, displayOrder {}", 
            operatorId, logoType, displayOrder);
        
        OperatorBranding branding = operatorBrandingRepository
            .findByOperatorIdAndLogoTypeAndDisplayOrder(operatorId, logoType, displayOrder)
            .orElseThrow(() -> new IllegalArgumentException(
                String.format("Branding not found for operator %d, type %s, order %d",
                    operatorId, logoType, displayOrder)));
        
        Map<String, Object> oldValues = new HashMap<>();
        oldValues.put("isActive", branding.getIsActive());
        oldValues.put("displayOrder", branding.getDisplayOrder());
        
        if (request.getIsActive() != null) {
            branding.setIsActive(request.getIsActive());
        }
        if (request.getDisplayOrder() != null) {
            branding.setDisplayOrder(request.getDisplayOrder());
        }
        branding.setUpdatedBy(RequestContextUtil.getAuthenticatedUser());
        
        OperatorBranding saved = operatorBrandingRepository.save(branding);
        
        // Log audit event
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("isActive", saved.getIsActive());
        newValues.put("displayOrder", saved.getDisplayOrder());
        
        auditService.logAuditEvent(
            operatorId,
            "OPERATOR_BRANDING_UPDATED",
            String.format("Updated operator branding for operator %d", operatorId),
            RequestContextUtil.getAuthenticatedUser(),
            RequestContextUtil.getClientIpAddress(),
            RequestContextUtil.getRequestId(),
            oldValues,
            newValues
        );
        
        return mapToBrandingResponse(saved);
    }
    
    // ==================== Game Provider Branding ====================
    
    @Transactional
    public BrandingResponse uploadGameProviderLogo(
        Long operatorId, String gameProviderId, LogoType logoType, MultipartFile file, Integer displayOrder) 
        throws IOException {
        log.info("Uploading game provider logo for operator {}, provider {}, type {}", 
            operatorId, gameProviderId, logoType);
        
        if (!operatorRepository.existsById(operatorId)) {
            throw new IllegalArgumentException("Operator not found: " + operatorId);
        }
        
        validateFile(file);
        String filePath = saveFile(operatorId, "provider", gameProviderId + "_" + logoType.name(), file);
        
        OperatorGameProviderBranding branding = new OperatorGameProviderBranding();
        branding.setOperatorId(operatorId);
        branding.setGameProviderId(gameProviderId);
        branding.setLogoType(logoType);
        branding.setFileName(file.getOriginalFilename());
        branding.setFilePath(filePath);
        branding.setFileFormat(getFileFormat(file.getContentType()));
        branding.setFileSize(file.getSize());
        branding.setDisplayOrder(displayOrder != null ? displayOrder : 0);
        branding.setCreatedBy(RequestContextUtil.getAuthenticatedUser());
        
        OperatorGameProviderBranding saved = providerBrandingRepository.save(branding);
        
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("gameProviderId", saved.getGameProviderId());
        newValues.put("logoType", saved.getLogoType());
        newValues.put("fileName", saved.getFileName());
        
        auditService.logAuditEvent(
            operatorId,
            "GAME_PROVIDER_BRANDING_UPLOADED",
            String.format("Uploaded game provider logo for operator %d, provider %s", operatorId, gameProviderId),
            RequestContextUtil.getAuthenticatedUser(),
            RequestContextUtil.getClientIpAddress(),
            RequestContextUtil.getRequestId(),
            null,
            newValues
        );
        
        return mapToProviderBrandingResponse(saved);
    }
    
    public List<BrandingResponse> getGameProviderBranding(Long operatorId, String gameProviderId, LogoType logoType) {
        List<OperatorGameProviderBranding> branding;
        if (logoType != null) {
            branding = providerBrandingRepository.findByOperatorIdAndGameProviderIdAndLogoTypeAndIsActiveTrueOrderByDisplayOrderAsc(
                operatorId, gameProviderId, logoType);
        } else {
            branding = providerBrandingRepository.findByOperatorIdAndGameProviderIdAndIsActiveTrueOrderByDisplayOrderAsc(
                operatorId, gameProviderId);
        }
        
        return branding.stream()
            .map(this::mapToProviderBrandingResponse)
            .collect(Collectors.toList());
    }
    
    // ==================== Game Branding ====================
    
    @Transactional
    public BrandingResponse uploadGameLogo(
        Long operatorId, String gameId, String gameProviderId, LogoType logoType, 
        MultipartFile file, Integer displayOrder) throws IOException {
        log.info("Uploading game logo for operator {}, game {}, type {}", operatorId, gameId, logoType);
        
        if (!operatorRepository.existsById(operatorId)) {
            throw new IllegalArgumentException("Operator not found: " + operatorId);
        }
        
        validateFile(file);
        String filePath = saveFile(operatorId, "game", gameId + "_" + logoType.name(), file);
        
        OperatorGameBranding branding = new OperatorGameBranding();
        branding.setOperatorId(operatorId);
        branding.setGameId(gameId);
        branding.setGameProviderId(gameProviderId);
        branding.setLogoType(logoType);
        branding.setFileName(file.getOriginalFilename());
        branding.setFilePath(filePath);
        branding.setFileFormat(getFileFormat(file.getContentType()));
        branding.setFileSize(file.getSize());
        branding.setDisplayOrder(displayOrder != null ? displayOrder : 0);
        branding.setCreatedBy(RequestContextUtil.getAuthenticatedUser());
        
        OperatorGameBranding saved = gameBrandingRepository.save(branding);
        
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("gameId", saved.getGameId());
        newValues.put("logoType", saved.getLogoType());
        newValues.put("fileName", saved.getFileName());
        
        auditService.logAuditEvent(
            operatorId,
            "GAME_BRANDING_UPLOADED",
            String.format("Uploaded game logo for operator %d, game %s", operatorId, gameId),
            RequestContextUtil.getAuthenticatedUser(),
            RequestContextUtil.getClientIpAddress(),
            RequestContextUtil.getRequestId(),
            null,
            newValues
        );
        
        return mapToGameBrandingResponse(saved);
    }
    
    public List<BrandingResponse> getGameBranding(Long operatorId, String gameId, LogoType logoType) {
        List<OperatorGameBranding> branding;
        if (logoType != null) {
            branding = gameBrandingRepository.findByOperatorIdAndGameIdAndLogoTypeAndIsActiveTrueOrderByDisplayOrderAsc(
                operatorId, gameId, logoType);
        } else {
            branding = gameBrandingRepository.findByOperatorIdAndGameIdAndIsActiveTrueOrderByDisplayOrderAsc(
                operatorId, gameId);
        }
        
        return branding.stream()
            .map(this::mapToGameBrandingResponse)
            .collect(Collectors.toList());
    }
    
    // ==================== Helper Methods ====================
    
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }
        
        String contentType = file.getContentType();
        if (contentType == null || 
            (!contentType.startsWith("image/png") && 
             !contentType.startsWith("image/jpeg") && 
             !contentType.startsWith("image/jpg") &&
             !contentType.equals("image/svg+xml"))) {
            throw new IllegalArgumentException("Invalid file format. Only PNG, JPG, JPEG, SVG are allowed");
        }
        
        // Max file size: 10MB
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("File size exceeds 10MB limit");
        }
    }
    
    private String saveFile(Long operatorId, String category, String identifier, MultipartFile file) throws IOException {
        // Create directory structure: uploads/branding/{operatorId}/{category}/
        Path uploadPath = Paths.get(UPLOAD_DIR + operatorId + "/" + category);
        Files.createDirectories(uploadPath);
        
        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".") 
            ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
            : "";
        String uniqueFilename = UUID.randomUUID().toString() + extension;
        
        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // Return relative path for storage
        return filePath.toString().replace("\\", "/");
    }
    
    private FileFormat getFileFormat(String contentType) {
        if (contentType == null) {
            return FileFormat.PNG; // Default
        }
        if (contentType.contains("png")) {
            return FileFormat.PNG;
        } else if (contentType.contains("jpeg") || contentType.contains("jpg")) {
            return FileFormat.JPG;
        } else if (contentType.contains("svg")) {
            return FileFormat.SVG;
        }
        return FileFormat.PNG;
    }
    
    private BrandingResponse mapToBrandingResponse(OperatorBranding branding) {
        return BrandingResponse.builder()
            .id(branding.getId())
            .operatorId(branding.getOperatorId())
            .logoType(branding.getLogoType())
            .fileName(branding.getFileName())
            .filePath(branding.getFilePath())
            .fileFormat(branding.getFileFormat())
            .fileSize(branding.getFileSize())
            .width(branding.getWidth())
            .height(branding.getHeight())
            .isActive(branding.getIsActive())
            .displayOrder(branding.getDisplayOrder())
            .createdBy(branding.getCreatedBy())
            .createdAt(branding.getCreatedAt())
            .updatedBy(branding.getUpdatedBy())
            .updatedAt(branding.getUpdatedAt())
            .build();
    }
    
    private BrandingResponse mapToProviderBrandingResponse(OperatorGameProviderBranding branding) {
        BrandingResponse response = BrandingResponse.builder()
            .id(branding.getId())
            .operatorId(branding.getOperatorId())
            .logoType(branding.getLogoType())
            .fileName(branding.getFileName())
            .filePath(branding.getFilePath())
            .fileFormat(branding.getFileFormat())
            .fileSize(branding.getFileSize())
            .width(branding.getWidth())
            .height(branding.getHeight())
            .isActive(branding.getIsActive())
            .displayOrder(branding.getDisplayOrder())
            .createdBy(branding.getCreatedBy())
            .createdAt(branding.getCreatedAt())
            .updatedBy(branding.getUpdatedBy())
            .updatedAt(branding.getUpdatedAt())
            .gameProviderId(branding.getGameProviderId())
            .build();
        return response;
    }
    
    private BrandingResponse mapToGameBrandingResponse(OperatorGameBranding branding) {
        BrandingResponse response = BrandingResponse.builder()
            .id(branding.getId())
            .operatorId(branding.getOperatorId())
            .logoType(branding.getLogoType())
            .fileName(branding.getFileName())
            .filePath(branding.getFilePath())
            .fileFormat(branding.getFileFormat())
            .fileSize(branding.getFileSize())
            .width(branding.getWidth())
            .height(branding.getHeight())
            .isActive(branding.getIsActive())
            .displayOrder(branding.getDisplayOrder())
            .createdBy(branding.getCreatedBy())
            .createdAt(branding.getCreatedAt())
            .updatedBy(branding.getUpdatedBy())
            .updatedAt(branding.getUpdatedAt())
            .gameProviderId(branding.getGameProviderId())
            .gameId(branding.getGameId())
            .build();
        return response;
    }
}
