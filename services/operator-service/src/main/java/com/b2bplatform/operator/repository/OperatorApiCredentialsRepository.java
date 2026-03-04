package com.b2bplatform.operator.repository;

import com.b2bplatform.operator.model.OperatorApiCredentials;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OperatorApiCredentialsRepository extends JpaRepository<OperatorApiCredentials, Long> {
    
    Optional<OperatorApiCredentials> findByUsername(String username);
    
    Optional<OperatorApiCredentials> findByOperatorId(Long operatorId);
    
    Optional<OperatorApiCredentials> findByOperatorIdAndIsActiveTrue(Long operatorId);
    
    boolean existsByUsername(String username);
    
    boolean existsByOperatorId(Long operatorId);
}
