package com.b2bplatform.wallet.service;

import com.b2bplatform.wallet.model.Wallet;
import com.b2bplatform.wallet.model.Wallet.WalletModel;
import com.b2bplatform.wallet.model.Wallet.WalletStatus;
import com.b2bplatform.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Service for managing wallets (creation, retrieval, balance management).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WalletManagementService {
    
    private final WalletRepository walletRepository;
    
    /**
     * Get or create wallet for player
     */
    @Transactional
    public Wallet getOrCreateWallet(Long operatorId, String playerId, WalletModel walletModel, String currency) {
        return walletRepository.findByOperatorIdAndPlayerId(operatorId, playerId)
            .orElseGet(() -> {
                log.info("Creating new wallet - operator: {}, player: {}, model: {}", 
                    operatorId, playerId, walletModel);
                Wallet wallet = Wallet.builder()
                    .operatorId(operatorId)
                    .playerId(playerId)
                    .balance(BigDecimal.ZERO)
                    .currency(currency != null ? currency : "USD")
                    .walletModel(walletModel != null ? walletModel : WalletModel.SHARED_WALLET)
                    .status(WalletStatus.ACTIVE)
                    .build();
                return walletRepository.save(wallet);
            });
    }
    
    /**
     * Get wallet with pessimistic lock (for concurrent transactions)
     */
    @Transactional
    public Wallet getWalletWithLock(Long operatorId, String playerId) {
        return walletRepository.findByOperatorIdAndPlayerIdWithLock(operatorId, playerId)
            .orElseThrow(() -> new IllegalArgumentException(
                String.format("Wallet not found - operator: %d, player: %s", operatorId, playerId)));
    }
    
    /**
     * Update wallet balance (used in Fund Transfer model)
     * Note: Transaction record should be created separately
     */
    @Transactional
    public Wallet updateBalance(Wallet wallet, BigDecimal newBalance) {
        wallet.setBalance(newBalance);
        wallet.setUpdatedAt(LocalDateTime.now());
        return walletRepository.save(wallet);
    }
    
    /**
     * Validate wallet can process transactions
     */
    public void validateWalletStatus(Wallet wallet) {
        if (wallet.getStatus() != WalletStatus.ACTIVE) {
            throw new IllegalStateException(
                String.format("Wallet is not active - status: %s", wallet.getStatus()));
        }
    }
    
    /**
     * Validate currency match
     */
    public void validateCurrency(Wallet wallet, String currency) {
        if (!wallet.getCurrency().equals(currency)) {
            throw new IllegalArgumentException(
                String.format("Currency mismatch - wallet: %s, transaction: %s", 
                    wallet.getCurrency(), currency));
        }
    }
    
    /**
     * Validate sufficient balance (for debit transactions in Fund Transfer model)
     */
    public void validateBalance(Wallet wallet, BigDecimal amount) {
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException(
                String.format("Insufficient balance - current: %s, required: %s", 
                    wallet.getBalance(), amount));
        }
    }
    
    /**
     * Get wallet balance (for Fund Transfer model)
     */
    public BigDecimal getBalance(Long operatorId, String playerId) {
        return walletRepository.findByOperatorIdAndPlayerId(operatorId, playerId)
            .map(Wallet::getBalance)
            .orElse(BigDecimal.ZERO);
    }
}
