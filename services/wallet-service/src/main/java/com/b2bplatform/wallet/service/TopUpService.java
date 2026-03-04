package com.b2bplatform.wallet.service;

import com.b2bplatform.wallet.model.TransactionType;
import com.b2bplatform.wallet.model.Wallet;
import com.b2bplatform.wallet.model.Wallet.WalletModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Service for Top-up model operations.
 * 
 * Top-up Model (Game-agnostic):
 * - Player transfers amount from operator to B2B wallet (TRANSFER_IN)
 * - Player uses funds for multiple games (BET from B2B wallet)
 * - Player can top-up when balance is low (TRANSFER_IN)
 * - Player can transfer remaining balance back to operator (TRANSFER_OUT)
 * 
 * Use Cases:
 * - Poker games (multiple rounds, top-up as needed)
 * - Tournament games (buy-in, multiple rounds)
 * - Any game requiring prepaid wallet balance
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TopUpService {
    
    private final EnhancedWalletTransactionService transactionService;
    private final WalletManagementService walletManagementService;
    
    /**
     * Initial transfer: Transfer funds from operator to B2B wallet
     * 
     * @param operatorId Operator ID
     * @param playerId Player ID
     * @param amount Amount to transfer
     * @param currency Currency
     * @param reference Reference (e.g., "TOPUP_INITIAL")
     * @return Transaction response
     */
    @Transactional
    public Map<String, Object> initialTransfer(
            Long operatorId, 
            String playerId, 
            BigDecimal amount,
            String currency,
            String reference) {
        
        log.info("Top-up initial transfer - operator: {}, player: {}, amount: {}", 
            operatorId, playerId, amount);
        
        // Process transfer in (wallet will be created if needed by processCredit)
        return transactionService.processCredit(
            operatorId,
            playerId,
            amount,
            currency,
            reference,
            "Initial transfer to B2B wallet",
            TransactionType.TRANSFER_IN,
            WalletModel.FUND_TRANSFER,
            null
        );
    }
    
    /**
     * Top-up: Transfer more funds from operator to B2B wallet
     * 
     * @param operatorId Operator ID
     * @param playerId Player ID
     * @param amount Amount to top-up
     * @param currency Currency
     * @param reference Reference (e.g., "TOPUP_001")
     * @return Transaction response
     */
    @Transactional
    public Map<String, Object> topUp(
            Long operatorId,
            String playerId,
            BigDecimal amount,
            String currency,
            String reference) {
        
        log.info("Top-up - operator: {}, player: {}, amount: {}", 
            operatorId, playerId, amount);
        
        // Process transfer in
        return transactionService.processCredit(
            operatorId,
            playerId,
            amount,
            currency,
            reference,
            "Top-up B2B wallet",
            TransactionType.TRANSFER_IN,
            WalletModel.FUND_TRANSFER,
            null
        );
    }
    
    /**
     * Place bet from B2B wallet (no operator call)
     * 
     * @param operatorId Operator ID
     * @param playerId Player ID
     * @param amount Bet amount
     * @param currency Currency
     * @param reference Bet reference (e.g., bet ID)
     * @return Transaction response
     */
    @Transactional
    public Map<String, Object> placeBet(
            Long operatorId,
            String playerId,
            BigDecimal amount,
            String currency,
            String reference) {
        
        log.info("Top-up model bet - operator: {}, player: {}, amount: {}", 
            operatorId, playerId, amount);
        
        // Process bet (debit from B2B wallet, no operator call)
        return transactionService.processDebit(
            operatorId,
            playerId,
            amount,
            currency,
            reference,
            "Bet from B2B wallet",
            TransactionType.BET,
            WalletModel.FUND_TRANSFER,
            null
        );
    }
    
    /**
     * Win bet: Credit to B2B wallet (no operator call)
     * 
     * @param operatorId Operator ID
     * @param playerId Player ID
     * @param amount Win amount
     * @param currency Currency
     * @param reference Bet reference
     * @param betTransactionId Related bet transaction ID (for linking)
     * @return Transaction response
     */
    @Transactional
    public Map<String, Object> winBet(
            Long operatorId,
            String playerId,
            BigDecimal amount,
            String currency,
            String reference,
            Long betTransactionId) {
        
        log.info("Top-up model win - operator: {}, player: {}, amount: {}, betTransactionId: {}", 
            operatorId, playerId, amount, betTransactionId);
        
        // Process win (credit to B2B wallet, no operator call, linked to bet)
        return transactionService.processCredit(
            operatorId,
            playerId,
            amount,
            currency,
            reference,
            "Win bet",
            TransactionType.WIN,
            WalletModel.FUND_TRANSFER,
            betTransactionId
        );
    }
    
    /**
     * Transfer back: Transfer remaining balance from B2B wallet to operator
     * 
     * @param operatorId Operator ID
     * @param playerId Player ID
     * @param amount Amount to transfer (or null for full balance)
     * @param currency Currency
     * @param reference Reference (e.g., "TRANSFER_OUT")
     * @return Transaction response
     */
    @Transactional
    public Map<String, Object> transferBack(
            Long operatorId,
            String playerId,
            BigDecimal amount,
            String currency,
            String reference) {
        
        log.info("Top-up transfer back - operator: {}, player: {}, amount: {}", 
            operatorId, playerId, amount);
        
        // If amount is null, transfer full balance
        if (amount == null) {
            BigDecimal currentBalance = walletManagementService.getBalance(operatorId, playerId);
            if (currentBalance.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalStateException("No balance to transfer");
            }
            amount = currentBalance;
        }
        
        // Process transfer out (debit B2B wallet, credit operator)
        return transactionService.processDebit(
            operatorId,
            playerId,
            amount,
            currency,
            reference,
            "Transfer back to operator",
            TransactionType.TRANSFER_OUT,
            WalletModel.FUND_TRANSFER,
            null
        );
    }
    
    /**
     * Get current balance in B2B wallet
     */
    public BigDecimal getBalance(Long operatorId, String playerId) {
        return walletManagementService.getBalance(operatorId, playerId);
    }
}
