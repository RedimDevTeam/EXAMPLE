package com.b2bplatform.operator.service;

import com.b2bplatform.operator.dto.request.CreateCreditAllocationRequest;
import com.b2bplatform.operator.dto.request.CreateHierarchyRequest;
import com.b2bplatform.operator.dto.request.CreateRevenueSharingRequest;
import com.b2bplatform.operator.dto.request.UpdateCreditAllocationRequest;
import com.b2bplatform.operator.dto.request.UpdateRevenueSharingRequest;
import com.b2bplatform.operator.dto.response.CreditAllocationResponse;
import com.b2bplatform.operator.dto.response.HierarchyResponse;
import com.b2bplatform.operator.dto.response.RevenueSharingResponse;
import com.b2bplatform.operator.model.*;
import com.b2bplatform.operator.repository.*;
import com.b2bplatform.operator.util.RequestContextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Service for managing operator hierarchy, revenue sharing, and credit allocation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HierarchyService {
    
    private final OperatorHierarchyRepository hierarchyRepository;
    private final OperatorRevenueSharingRepository revenueSharingRepository;
    private final OperatorCreditAllocationRepository creditAllocationRepository;
    private final OperatorRepository operatorRepository;
    private final OperatorAuditService auditService;
    
    @Transactional
    public HierarchyResponse createHierarchy(Long operatorId, CreateHierarchyRequest request) {
        log.info("Creating hierarchy for operator {}, level {}", operatorId, request.getHierarchyLevel());
        
        Objects.requireNonNull(operatorId, "Operator ID cannot be null");
        
        if (!operatorRepository.existsById(operatorId)) {
            throw new IllegalArgumentException("Operator not found: " + operatorId);
        }
        
        if (hierarchyRepository.existsByOperatorId(operatorId)) {
            throw new IllegalStateException("Hierarchy already exists for operator: " + operatorId);
        }
        
        // Validate hierarchy level
        if (request.getHierarchyLevel() < 1 || request.getHierarchyLevel() > 3) {
            throw new IllegalArgumentException("Hierarchy level must be between 1 and 3");
        }
        
        // Validate parent for non-Master operators
        if (request.getHierarchyLevel() > 1 && request.getParentOperatorId() == null) {
            throw new IllegalArgumentException("Parent operator ID is required for Agent and Sub-Agent levels");
        }
        
        // Build hierarchy path
        String hierarchyPath = buildHierarchyPath(operatorId, request.getParentOperatorId(), request.getHierarchyLevel());
        
        OperatorHierarchy hierarchy = new OperatorHierarchy();
        hierarchy.setOperatorId(operatorId);
        hierarchy.setParentOperatorId(request.getParentOperatorId());
        hierarchy.setHierarchyLevel(request.getHierarchyLevel());
        hierarchy.setHierarchyPath(hierarchyPath);
        hierarchy.setIsMaster(request.getHierarchyLevel() == 1);
        hierarchy.setIsAgent(request.getHierarchyLevel() == 2);
        hierarchy.setIsSubAgent(request.getHierarchyLevel() == 3);
        hierarchy.setCanCreateChildren(request.getCanCreateChildren() != null ? request.getCanCreateChildren() : false);
        hierarchy.setMaxChildrenCount(request.getMaxChildrenCount());
        hierarchy.setCreatedBy(RequestContextUtil.getAuthenticatedUser());
        
        OperatorHierarchy saved = hierarchyRepository.save(hierarchy);
        
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("hierarchyLevel", saved.getHierarchyLevel());
        newValues.put("parentOperatorId", saved.getParentOperatorId());
        newValues.put("hierarchyPath", saved.getHierarchyPath());
        
        auditService.logAuditEvent(
            operatorId,
            "OPERATOR_HIERARCHY_CREATED",
            String.format("Created hierarchy for operator %d, level %d", operatorId, request.getHierarchyLevel()),
            RequestContextUtil.getAuthenticatedUser(),
            RequestContextUtil.getClientIpAddress(),
            RequestContextUtil.getRequestId(),
            null,
            newValues
        );
        
        return mapToHierarchyResponse(saved);
    }
    
    public HierarchyResponse getHierarchy(Long operatorId) {
        OperatorHierarchy hierarchy = hierarchyRepository.findByOperatorId(operatorId)
            .orElseThrow(() -> new IllegalArgumentException("Hierarchy not found for operator: " + operatorId));
        
        return mapToHierarchyResponse(hierarchy);
    }
    
    @Transactional
    public RevenueSharingResponse createRevenueSharing(Long operatorId, CreateRevenueSharingRequest request) {
        log.info("Creating revenue sharing for operator {}, parent {}", operatorId, request.getParentOperatorId());
        
        Objects.requireNonNull(operatorId, "Operator ID cannot be null");
        
        if (!operatorRepository.existsById(operatorId)) {
            throw new IllegalArgumentException("Operator not found: " + operatorId);
        }
        
        // Validate percentages sum
        BigDecimal total = request.getParentSharePercentage().add(request.getOperatorSharePercentage());
        if (total.compareTo(new BigDecimal("100.00")) > 0) {
            throw new IllegalArgumentException("Parent and operator share percentages must not exceed 100%");
        }
        
        OperatorRevenueSharing sharing = new OperatorRevenueSharing();
        sharing.setOperatorId(operatorId);
        sharing.setParentOperatorId(request.getParentOperatorId());
        sharing.setRevenueType(request.getRevenueType());
        sharing.setParentSharePercentage(request.getParentSharePercentage());
        sharing.setOperatorSharePercentage(request.getOperatorSharePercentage());
        sharing.setEffectiveFrom(LocalDateTime.now());
        sharing.setCreatedBy(RequestContextUtil.getAuthenticatedUser());
        
        OperatorRevenueSharing saved = revenueSharingRepository.save(sharing);
        
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("revenueType", saved.getRevenueType());
        newValues.put("parentSharePercentage", saved.getParentSharePercentage());
        newValues.put("operatorSharePercentage", saved.getOperatorSharePercentage());
        
        auditService.logAuditEvent(
            operatorId,
            "REVENUE_SHARING_CREATED",
            String.format("Created revenue sharing for operator %d, type %s", operatorId, request.getRevenueType()),
            RequestContextUtil.getAuthenticatedUser(),
            RequestContextUtil.getClientIpAddress(),
            RequestContextUtil.getRequestId(),
            null,
            newValues
        );
        
        return mapToRevenueSharingResponse(saved);
    }
    
    @Transactional
    public CreditAllocationResponse createCreditAllocation(Long parentOperatorId, CreateCreditAllocationRequest request) {
        log.info("Creating credit allocation from parent {} to child {}", parentOperatorId, request.getChildOperatorId());
        
        Objects.requireNonNull(parentOperatorId, "Parent operator ID cannot be null");
        Objects.requireNonNull(request.getChildOperatorId(), "Child operator ID cannot be null");
        
        if (!operatorRepository.existsById(parentOperatorId) || !operatorRepository.existsById(request.getChildOperatorId())) {
            throw new IllegalArgumentException("Parent or child operator not found");
        }
        
        if (creditAllocationRepository.existsByParentOperatorIdAndChildOperatorIdAndCurrencyCode(
            parentOperatorId, request.getChildOperatorId(), request.getCurrencyCode())) {
            throw new IllegalStateException(
                String.format("Credit allocation already exists for parent %d, child %d, currency %s",
                    parentOperatorId, request.getChildOperatorId(), request.getCurrencyCode()));
        }
        
        OperatorCreditAllocation allocation = new OperatorCreditAllocation();
        allocation.setParentOperatorId(parentOperatorId);
        allocation.setChildOperatorId(request.getChildOperatorId());
        allocation.setCreditLimit(request.getCreditLimit());
        allocation.setUsedCredit(BigDecimal.ZERO);
        allocation.setCurrencyCode(request.getCurrencyCode());
        allocation.setAllocationType(request.getAllocationType() != null ? request.getAllocationType() : AllocationType.MANUAL);
        allocation.setAutoReplenish(request.getAutoReplenish() != null ? request.getAutoReplenish() : false);
        allocation.setReplenishThreshold(request.getReplenishThreshold());
        allocation.setCreatedBy(RequestContextUtil.getAuthenticatedUser());
        
        OperatorCreditAllocation saved = creditAllocationRepository.save(allocation);
        
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("childOperatorId", saved.getChildOperatorId());
        newValues.put("creditLimit", saved.getCreditLimit());
        newValues.put("currencyCode", saved.getCurrencyCode());
        
        auditService.logAuditEvent(
            parentOperatorId,
            "CREDIT_ALLOCATION_CREATED",
            String.format("Created credit allocation from parent %d to child %d", parentOperatorId, request.getChildOperatorId()),
            RequestContextUtil.getAuthenticatedUser(),
            RequestContextUtil.getClientIpAddress(),
            RequestContextUtil.getRequestId(),
            null,
            newValues
        );
        
        return mapToCreditAllocationResponse(saved);
    }
    
    public List<RevenueSharingResponse> getRevenueSharings(Long operatorId) {
        return revenueSharingRepository.findByOperatorIdAndIsActiveTrue(operatorId).stream()
            .map(this::mapToRevenueSharingResponse)
            .collect(Collectors.toList());
    }
    
    public List<CreditAllocationResponse> getCreditAllocations(Long parentOperatorId) {
        return creditAllocationRepository.findByParentOperatorIdAndIsActiveTrue(parentOperatorId).stream()
            .map(this::mapToCreditAllocationResponse)
            .collect(Collectors.toList());
    }
    
    public List<CreditAllocationResponse> getCreditAllocationsForChild(Long childOperatorId) {
        return creditAllocationRepository.findByChildOperatorIdAndIsActiveTrue(childOperatorId).stream()
            .map(this::mapToCreditAllocationResponse)
            .collect(Collectors.toList());
    }
    
    @Transactional
    public RevenueSharingResponse updateRevenueSharing(Long operatorId, Long parentOperatorId, 
                                                      RevenueType revenueType, UpdateRevenueSharingRequest request) {
        log.info("Updating revenue sharing for operator {}, parent {}, type {}", 
            operatorId, parentOperatorId, revenueType);
        
        OperatorRevenueSharing sharing = revenueSharingRepository
            .findByOperatorIdAndParentOperatorIdAndRevenueTypeAndIsActiveTrue(operatorId, parentOperatorId, revenueType)
            .orElseThrow(() -> new IllegalArgumentException(
                String.format("Revenue sharing not found for operator %d, parent %d, type %s",
                    operatorId, parentOperatorId, revenueType)));
        
        Map<String, Object> oldValues = new HashMap<>();
        oldValues.put("parentSharePercentage", sharing.getParentSharePercentage());
        oldValues.put("operatorSharePercentage", sharing.getOperatorSharePercentage());
        oldValues.put("isActive", sharing.getIsActive());
        
        // Validate percentages if both are provided
        if (request.getParentSharePercentage() != null && request.getOperatorSharePercentage() != null) {
            BigDecimal total = request.getParentSharePercentage().add(request.getOperatorSharePercentage());
            if (total.compareTo(new BigDecimal("100.00")) > 0) {
                throw new IllegalArgumentException("Parent and operator share percentages must not exceed 100%");
            }
        } else if (request.getParentSharePercentage() != null) {
            BigDecimal total = request.getParentSharePercentage().add(sharing.getOperatorSharePercentage());
            if (total.compareTo(new BigDecimal("100.00")) > 0) {
                throw new IllegalArgumentException("Parent and operator share percentages must not exceed 100%");
            }
        } else if (request.getOperatorSharePercentage() != null) {
            BigDecimal total = sharing.getParentSharePercentage().add(request.getOperatorSharePercentage());
            if (total.compareTo(new BigDecimal("100.00")) > 0) {
                throw new IllegalArgumentException("Parent and operator share percentages must not exceed 100%");
            }
        }
        
        if (request.getParentSharePercentage() != null) {
            sharing.setParentSharePercentage(request.getParentSharePercentage());
        }
        if (request.getOperatorSharePercentage() != null) {
            sharing.setOperatorSharePercentage(request.getOperatorSharePercentage());
        }
        if (request.getEffectiveTo() != null) {
            sharing.setEffectiveTo(request.getEffectiveTo());
        }
        if (request.getIsActive() != null) {
            sharing.setIsActive(request.getIsActive());
        }
        sharing.setUpdatedBy(RequestContextUtil.getAuthenticatedUser());
        
        OperatorRevenueSharing saved = revenueSharingRepository.save(sharing);
        
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("parentSharePercentage", saved.getParentSharePercentage());
        newValues.put("operatorSharePercentage", saved.getOperatorSharePercentage());
        newValues.put("isActive", saved.getIsActive());
        
        auditService.logAuditEvent(
            operatorId,
            "REVENUE_SHARING_UPDATED",
            String.format("Updated revenue sharing for operator %d, type %s", operatorId, revenueType),
            RequestContextUtil.getAuthenticatedUser(),
            RequestContextUtil.getClientIpAddress(),
            RequestContextUtil.getRequestId(),
            oldValues,
            newValues
        );
        
        return mapToRevenueSharingResponse(saved);
    }
    
    @Transactional
    public CreditAllocationResponse updateCreditAllocation(Long parentOperatorId, Long childOperatorId, 
                                                          String currencyCode, UpdateCreditAllocationRequest request) {
        log.info("Updating credit allocation from parent {} to child {}, currency {}", 
            parentOperatorId, childOperatorId, currencyCode);
        
        OperatorCreditAllocation allocation = creditAllocationRepository
            .findByParentOperatorIdAndChildOperatorIdAndCurrencyCodeAndIsActiveTrue(
                parentOperatorId, childOperatorId, currencyCode)
            .orElseThrow(() -> new IllegalArgumentException(
                String.format("Credit allocation not found for parent %d, child %d, currency %s",
                    parentOperatorId, childOperatorId, currencyCode)));
        
        Map<String, Object> oldValues = new HashMap<>();
        oldValues.put("creditLimit", allocation.getCreditLimit());
        oldValues.put("allocationType", allocation.getAllocationType());
        oldValues.put("autoReplenish", allocation.getAutoReplenish());
        oldValues.put("isActive", allocation.getIsActive());
        
        if (request.getCreditLimit() != null) {
            if (request.getCreditLimit().compareTo(allocation.getUsedCredit()) < 0) {
                throw new IllegalArgumentException(
                    "Credit limit cannot be less than used credit: " + allocation.getUsedCredit());
            }
            allocation.setCreditLimit(request.getCreditLimit());
        }
        if (request.getAllocationType() != null) {
            allocation.setAllocationType(request.getAllocationType());
        }
        if (request.getAutoReplenish() != null) {
            allocation.setAutoReplenish(request.getAutoReplenish());
        }
        if (request.getReplenishThreshold() != null) {
            allocation.setReplenishThreshold(request.getReplenishThreshold());
        }
        if (request.getIsActive() != null) {
            allocation.setIsActive(request.getIsActive());
        }
        allocation.setUpdatedBy(RequestContextUtil.getAuthenticatedUser());
        
        OperatorCreditAllocation saved = creditAllocationRepository.save(allocation);
        
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("creditLimit", saved.getCreditLimit());
        newValues.put("allocationType", saved.getAllocationType());
        newValues.put("autoReplenish", saved.getAutoReplenish());
        newValues.put("isActive", saved.getIsActive());
        
        auditService.logAuditEvent(
            parentOperatorId,
            "CREDIT_ALLOCATION_UPDATED",
            String.format("Updated credit allocation from parent %d to child %d", parentOperatorId, childOperatorId),
            RequestContextUtil.getAuthenticatedUser(),
            RequestContextUtil.getClientIpAddress(),
            RequestContextUtil.getRequestId(),
            oldValues,
            newValues
        );
        
        return mapToCreditAllocationResponse(saved);
    }
    
    @Transactional
    public void deleteCreditAllocation(Long parentOperatorId, Long childOperatorId, String currencyCode) {
        log.info("Deleting credit allocation from parent {} to child {}, currency {}", 
            parentOperatorId, childOperatorId, currencyCode);
        
        OperatorCreditAllocation allocation = creditAllocationRepository
            .findByParentOperatorIdAndChildOperatorIdAndCurrencyCodeAndIsActiveTrue(
                parentOperatorId, childOperatorId, currencyCode)
            .orElseThrow(() -> new IllegalArgumentException(
                String.format("Credit allocation not found for parent %d, child %d, currency %s",
                    parentOperatorId, childOperatorId, currencyCode)));
        
        Map<String, Object> oldValues = new HashMap<>();
        oldValues.put("creditLimit", allocation.getCreditLimit());
        oldValues.put("usedCredit", allocation.getUsedCredit());
        
        creditAllocationRepository.delete(allocation);
        
        auditService.logAuditEvent(
            parentOperatorId,
            "CREDIT_ALLOCATION_DELETED",
            String.format("Deleted credit allocation from parent %d to child %d", parentOperatorId, childOperatorId),
            RequestContextUtil.getAuthenticatedUser(),
            RequestContextUtil.getClientIpAddress(),
            RequestContextUtil.getRequestId(),
            oldValues,
            null
        );
    }
    
    private String buildHierarchyPath(Long operatorId, Long parentOperatorId, Integer level) {
        if (level == 1) {
            return String.valueOf(operatorId);
        } else if (parentOperatorId != null) {
            OperatorHierarchy parent = hierarchyRepository.findByOperatorId(parentOperatorId).orElse(null);
            if (parent != null && parent.getHierarchyPath() != null) {
                return parent.getHierarchyPath() + "/" + operatorId;
            }
            return parentOperatorId + "/" + operatorId;
        }
        return String.valueOf(operatorId);
    }
    
    private HierarchyResponse mapToHierarchyResponse(OperatorHierarchy hierarchy) {
        List<OperatorHierarchy> children = hierarchyRepository.findByParentOperatorId(hierarchy.getOperatorId());
        List<Long> childIds = children.stream()
            .map(OperatorHierarchy::getOperatorId)
            .collect(Collectors.toList());
        
        return HierarchyResponse.builder()
            .id(hierarchy.getId())
            .operatorId(hierarchy.getOperatorId())
            .parentOperatorId(hierarchy.getParentOperatorId())
            .hierarchyLevel(hierarchy.getHierarchyLevel())
            .hierarchyPath(hierarchy.getHierarchyPath())
            .isMaster(hierarchy.getIsMaster())
            .isAgent(hierarchy.getIsAgent())
            .isSubAgent(hierarchy.getIsSubAgent())
            .canCreateChildren(hierarchy.getCanCreateChildren())
            .maxChildrenCount(hierarchy.getMaxChildrenCount())
            .childrenCount((long) children.size())
            .childOperatorIds(childIds)
            .createdBy(hierarchy.getCreatedBy())
            .createdAt(hierarchy.getCreatedAt())
            .updatedBy(hierarchy.getUpdatedBy())
            .updatedAt(hierarchy.getUpdatedAt())
            .build();
    }
    
    private RevenueSharingResponse mapToRevenueSharingResponse(OperatorRevenueSharing sharing) {
        return RevenueSharingResponse.builder()
            .id(sharing.getId())
            .operatorId(sharing.getOperatorId())
            .parentOperatorId(sharing.getParentOperatorId())
            .revenueType(sharing.getRevenueType())
            .parentSharePercentage(sharing.getParentSharePercentage())
            .operatorSharePercentage(sharing.getOperatorSharePercentage())
            .effectiveFrom(sharing.getEffectiveFrom())
            .effectiveTo(sharing.getEffectiveTo())
            .isActive(sharing.getIsActive())
            .createdBy(sharing.getCreatedBy())
            .createdAt(sharing.getCreatedAt())
            .updatedBy(sharing.getUpdatedBy())
            .updatedAt(sharing.getUpdatedAt())
            .build();
    }
    
    private CreditAllocationResponse mapToCreditAllocationResponse(OperatorCreditAllocation allocation) {
        return CreditAllocationResponse.builder()
            .id(allocation.getId())
            .parentOperatorId(allocation.getParentOperatorId())
            .childOperatorId(allocation.getChildOperatorId())
            .creditLimit(allocation.getCreditLimit())
            .usedCredit(allocation.getUsedCredit())
            .availableCredit(allocation.getAvailableCredit())
            .currencyCode(allocation.getCurrencyCode())
            .allocationType(allocation.getAllocationType())
            .autoReplenish(allocation.getAutoReplenish())
            .replenishThreshold(allocation.getReplenishThreshold())
            .isActive(allocation.getIsActive())
            .allocatedAt(allocation.getAllocatedAt())
            .expiresAt(allocation.getExpiresAt())
            .createdBy(allocation.getCreatedBy())
            .createdAt(allocation.getCreatedAt())
            .updatedBy(allocation.getUpdatedBy())
            .updatedAt(allocation.getUpdatedAt())
            .build();
    }
}
