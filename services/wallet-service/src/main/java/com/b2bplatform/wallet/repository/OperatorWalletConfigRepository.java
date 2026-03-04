package com.b2bplatform.wallet.repository;

import com.b2bplatform.wallet.model.OperatorWalletConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OperatorWalletConfigRepository extends JpaRepository<OperatorWalletConfig, Long> {
    Optional<OperatorWalletConfig> findByOperatorId(Long operatorId);
    
    Optional<OperatorWalletConfig> findByOperatorIdAndEnabledTrue(Long operatorId);
}
