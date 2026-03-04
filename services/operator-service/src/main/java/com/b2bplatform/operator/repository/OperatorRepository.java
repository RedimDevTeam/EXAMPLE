package com.b2bplatform.operator.repository;

import com.b2bplatform.operator.model.Operator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OperatorRepository extends JpaRepository<Operator, Long> {
    Optional<Operator> findByCode(String code);
    boolean existsByCode(String code);
}
