package com.b2bplatform.operator.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateHierarchyRequest {
    
    @NotNull(message = "Parent operator ID is required for Agent and Sub-Agent levels")
    private Long parentOperatorId; // null for Master operators
    
    @NotNull(message = "Hierarchy level is required")
    @Min(value = 1, message = "Hierarchy level must be between 1 and 3")
    private Integer hierarchyLevel; // 1=Master, 2=Agent, 3=Sub-Agent
    
    private Boolean canCreateChildren = false;
    
    @Min(value = 1, message = "Max children count must be positive")
    private Integer maxChildrenCount; // null = unlimited
}
