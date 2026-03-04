package com.b2bplatform.wallet.service;

import com.b2bplatform.wallet.dto.b2b.BlockUserRequest;
import com.b2bplatform.wallet.dto.b2b.KickoutRequest;
import com.b2bplatform.wallet.dto.b2b.OperationalResponse;
import com.b2bplatform.wallet.dto.b2b.UnblockUserRequest;
import com.b2bplatform.wallet.model.PlayerAccountStatus;
import com.b2bplatform.wallet.repository.PlayerAccountStatusRepository;
import com.b2bplatform.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * B2B Operational Service
 * Handles operational APIs: block/unblock/kickout/balance
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class B2BOperationalService {
    
    private final PlayerAccountStatusRepository accountStatusRepository;
    private final WalletRepository walletRepository;
    private final OperatorServiceClient operatorServiceClient;
    
    /**
     * Block user account
     */
    @Transactional
    public OperationalResponse blockUser(BlockUserRequest request) {
        log.info("Blocking user - operatorCode: {}, playerId: {}, reason: {}", 
            request.getOperatorCode(), request.getPlayerId(), request.getReason());
        
        // Convert operatorCode to operatorId
        Long operatorId = operatorServiceClient.getOperatorIdByCode(request.getOperatorCode());
        if (operatorId == null) {
            throw new IllegalArgumentException("Operator not found: " + request.getOperatorCode());
        }
        
        // Get or create account status
        PlayerAccountStatus accountStatus = accountStatusRepository
            .findByOperatorIdAndPlayerId(operatorId, request.getPlayerId())
            .orElseGet(() -> {
                PlayerAccountStatus newStatus = PlayerAccountStatus.builder()
                    .operatorId(operatorId)
                    .playerId(request.getPlayerId())
                    .isBlocked(false)
                    .build();
                return accountStatusRepository.save(newStatus);
            });
        
        // Block the account
        accountStatus.block(request.getReason(), "SYSTEM");
        accountStatus = accountStatusRepository.save(accountStatus);
        
        log.info("User blocked successfully - operatorCode: {}, playerId: {}", 
            request.getOperatorCode(), request.getPlayerId());
        
        return OperationalResponse.builder()
            .status(0)
            .message("User blocked successfully")
            .operatorCode(request.getOperatorCode())
            .playerId(request.getPlayerId())
            .isBlocked(true)
            .build();
    }
    
    /**
     * Unblock user account
     */
    @Transactional
    public OperationalResponse unblockUser(UnblockUserRequest request) {
        log.info("Unblocking user - operatorCode: {}, playerId: {}", 
            request.getOperatorCode(), request.getPlayerId());
        
        // Convert operatorCode to operatorId
        Long operatorId = operatorServiceClient.getOperatorIdByCode(request.getOperatorCode());
        if (operatorId == null) {
            throw new IllegalArgumentException("Operator not found: " + request.getOperatorCode());
        }
        
        // Get account status
        PlayerAccountStatus accountStatus = accountStatusRepository
            .findByOperatorIdAndPlayerId(operatorId, request.getPlayerId())
            .orElseThrow(() -> new IllegalArgumentException(
                "Account status not found for operator: " + request.getOperatorCode() + ", player: " + request.getPlayerId()));
        
        // Check if already unblocked
        if (!accountStatus.getIsBlocked()) {
            log.warn("User is already unblocked - operatorCode: {}, playerId: {}", 
                request.getOperatorCode(), request.getPlayerId());
            return OperationalResponse.builder()
                .status(0)
                .message("User is already unblocked")
                .operatorCode(request.getOperatorCode())
                .playerId(request.getPlayerId())
                .isBlocked(false)
                .build();
        }
        
        // Unblock the account
        accountStatus.unblock("SYSTEM");
        accountStatus = accountStatusRepository.save(accountStatus);
        
        log.info("User unblocked successfully - operatorCode: {}, playerId: {}", 
            request.getOperatorCode(), request.getPlayerId());
        
        return OperationalResponse.builder()
            .status(0)
            .message("User unblocked successfully")
            .operatorCode(request.getOperatorCode())
            .playerId(request.getPlayerId())
            .isBlocked(false)
            .build();
    }
    
    /**
     * Kickout user (force logout)
     */
    @Transactional
    public OperationalResponse kickout(KickoutRequest request) {
        log.info("Kicking out user - operatorCode: {}, playerId: {}", 
            request.getOperatorCode(), request.getPlayerId());
        
        // Convert operatorCode to operatorId
        Long operatorId = operatorServiceClient.getOperatorIdByCode(request.getOperatorCode());
        if (operatorId == null) {
            throw new IllegalArgumentException("Operator not found: " + request.getOperatorCode());
        }
        
        // Get or create account status
        PlayerAccountStatus accountStatus = accountStatusRepository
            .findByOperatorIdAndPlayerId(operatorId, request.getPlayerId())
            .orElseGet(() -> {
                PlayerAccountStatus newStatus = PlayerAccountStatus.builder()
                    .operatorId(operatorId)
                    .playerId(request.getPlayerId())
                    .isBlocked(false)
                    .build();
                return accountStatusRepository.save(newStatus);
            });
        
        // Kickout the user
        accountStatus.kickout("SYSTEM");
        accountStatus.updateActivity();
        accountStatus = accountStatusRepository.save(accountStatus);
        
        log.info("User kicked out successfully - operatorCode: {}, playerId: {}", 
            request.getOperatorCode(), request.getPlayerId());
        
        return OperationalResponse.builder()
            .status(0)
            .message("User kicked out successfully")
            .operatorCode(request.getOperatorCode())
            .playerId(request.getPlayerId())
            .kickedOutAt(accountStatus.getKickedOutAt() != null ? accountStatus.getKickedOutAt().toString() : null)
            .build();
    }
    
    /**
     * Get account balance (independent balance check)
     */
    public com.b2bplatform.wallet.dto.response.BalanceResponse getAccountBalance(String operatorCode, String playerId) {
        log.debug("Getting account balance - operatorCode: {}, playerId: {}", operatorCode, playerId);
        
        // Convert operatorCode to operatorId
        Long operatorId = operatorServiceClient.getOperatorIdByCode(operatorCode);
        if (operatorId == null) {
            throw new IllegalArgumentException("Operator not found: " + operatorCode);
        }
        
        // Check if account is blocked
        boolean isBlocked = accountStatusRepository.existsByOperatorIdAndPlayerIdAndIsBlockedTrue(operatorId, playerId);
        if (isBlocked) {
            throw new IllegalStateException("Account is blocked for operator: " + operatorCode + ", player: " + playerId);
        }
        
        // Get wallet balance
        Optional<com.b2bplatform.wallet.model.Wallet> walletOpt = walletRepository
            .findByOperatorIdAndPlayerId(operatorId, playerId);
        
        if (walletOpt.isEmpty()) {
            // Return zero balance if wallet doesn't exist
            return com.b2bplatform.wallet.dto.response.BalanceResponse.builder()
                .success(true)
                .playerId(playerId)
                .balance(BigDecimal.ZERO)
                .currency("USD")
                .timestamp(LocalDateTime.now())
                .build();
        }
        
        com.b2bplatform.wallet.model.Wallet wallet = walletOpt.get();
        
        return com.b2bplatform.wallet.dto.response.BalanceResponse.builder()
            .success(true)
            .playerId(playerId)
            .balance(wallet.getBalance())
            .currency(wallet.getCurrency())
            .timestamp(LocalDateTime.now())
            .build();
    }
    
    /**
     * Check if user is blocked
     */
    public boolean isUserBlocked(String operatorCode, String playerId) {
        Long operatorId = operatorServiceClient.getOperatorIdByCode(operatorCode);
        if (operatorId == null) {
            return false;
        }
        return accountStatusRepository.existsByOperatorIdAndPlayerIdAndIsBlockedTrue(operatorId, playerId);
    }
}
