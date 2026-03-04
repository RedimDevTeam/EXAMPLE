package com.b2bplatform.auth.repository;

import com.b2bplatform.auth.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
    Optional<Player> findByOperatorIdAndUsername(Long operatorId, String username);
    
    Optional<Player> findByOperatorIdAndPlayerId(Long operatorId, String playerId);
    
    boolean existsByOperatorIdAndUsername(Long operatorId, String username);
}
