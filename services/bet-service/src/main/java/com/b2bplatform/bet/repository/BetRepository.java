package com.b2bplatform.bet.repository;

import com.b2bplatform.bet.model.Bet;
import com.b2bplatform.bet.model.BetStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BetRepository extends JpaRepository<Bet, Long> {
    Optional<Bet> findByBetId(String betId);
    
    List<Bet> findByPlayerId(Long playerId);
    
    Page<Bet> findByPlayerId(Long playerId, Pageable pageable);
    
    List<Bet> findByPlayerIdAndStatus(Long playerId, BetStatus status);
    
    List<Bet> findByOperatorId(Long operatorId);
    
    Page<Bet> findByOperatorId(Long operatorId, Pageable pageable);
    
    List<Bet> findByGameCodeAndGameRoundId(String gameCode, String gameRoundId);
    
    List<Bet> findByStatus(BetStatus status);
    
    @Query("SELECT b FROM Bet b WHERE b.playerId = :playerId AND b.status = :status ORDER BY b.createdAt DESC")
    List<Bet> findPlayerBetsByStatus(@Param("playerId") Long playerId, @Param("status") BetStatus status);
}
