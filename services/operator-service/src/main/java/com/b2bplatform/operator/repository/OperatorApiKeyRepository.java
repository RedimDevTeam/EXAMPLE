package com.b2bplatform.operator.repository;

import com.b2bplatform.operator.model.OperatorApiKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OperatorApiKeyRepository extends JpaRepository<OperatorApiKey, Long> {
    Optional<OperatorApiKey> findByApiKey(String apiKey);
    
    boolean existsByApiKey(String apiKey);
    
    @Modifying
    @Query("UPDATE OperatorApiKey k SET k.lastUsedAt = :lastUsedAt WHERE k.id = :id")
    void updateLastUsedAt(@Param("id") Long id, @Param("lastUsedAt") LocalDateTime lastUsedAt);
}
