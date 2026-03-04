package com.b2bplatform.operator.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HierarchyResponse {
    
    private Long id;
    private Long operatorId;
    private Long parentOperatorId;
    private Integer hierarchyLevel; // 1=Master, 2=Agent, 3=Sub-Agent
    private String hierarchyPath;
    private Boolean isMaster;
    private Boolean isAgent;
    private Boolean isSubAgent;
    private Boolean canCreateChildren;
    private Integer maxChildrenCount;
    private Long childrenCount; // Number of child operators
    private List<Long> childOperatorIds; // List of child operator IDs
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
}
