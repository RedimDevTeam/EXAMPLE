package com.b2bplatform.wallet.service;

import com.b2bplatform.wallet.model.TransactionType;
import com.b2bplatform.wallet.model.Wallet;
import com.b2bplatform.wallet.model.Wallet.WalletModel;
import com.b2bplatform.wallet.model.WalletTransaction;
import com.b2bplatform.wallet.model.WalletTransaction.TransactionStatus;
import com.b2bplatform.wallet.repository.WalletRepository;
import com.b2bplatform.wallet.repository.WalletTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for wallet transaction audit queries and balance verification.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionAuditService {
    
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;
    
    /**
     * Verify wallet balance matches transaction sum (Fund Transfer model only)
     * 
     * @param walletId Wallet ID
     * @return Verification result with difference
     */
    public Map<String, Object> verifyBalance(Long walletId) {
        Optional<Wallet> walletOpt = walletRepository.findById(walletId);
        if (walletOpt.isEmpty()) {
            throw new IllegalArgumentException("Wallet not found: " + walletId);
        }
        
        Wallet wallet = walletOpt.get();
        
        // Only verify for Fund Transfer model
        if (wallet.getWalletModel() != WalletModel.FUND_TRANSFER) {
            return Map.of(
                "walletId", walletId,
                "model", wallet.getWalletModel().name(),
                "verification", "NOT_APPLICABLE",
                "message", "Balance verification only applicable for Fund Transfer model"
            );
        }
        
        // Calculate balance from transactions
        List<WalletTransaction> transactions = transactionRepository.findByWalletIdOrderByCreatedAtDesc(walletId);
        
        BigDecimal calculatedBalance = transactions.stream()
            .filter(t -> t.getStatus() == TransactionStatus.COMPLETED)
            .map(t -> {
                TransactionType type = t.getTransactionType();
                BigDecimal amount = t.getAmount();
                
                if (type.isCredit()) {
                    return amount;
                } else if (type.isDebit()) {
                    return amount.negate();
                } else if (type == TransactionType.ADJUSTMENT) {
                    // Adjustment can be positive or negative
                    return amount;
                }
                return BigDecimal.ZERO;
            })
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal currentBalance = wallet.getBalance();
        BigDecimal difference = currentBalance.subtract(calculatedBalance);
        
        Map<String, Object> result = new HashMap<>();
        result.put("walletId", walletId);
        result.put("playerId", wallet.getPlayerId());
        result.put("currentBalance", currentBalance);
        result.put("calculatedBalance", calculatedBalance);
        result.put("difference", difference);
        result.put("isBalanced", difference.compareTo(BigDecimal.ZERO) == 0);
        result.put("transactionCount", transactions.size());
        
        if (difference.compareTo(BigDecimal.ZERO) != 0) {
            log.warn("Balance mismatch detected - wallet: {}, difference: {}", walletId, difference);
        }
        
        return result;
    }
    
    /**
     * Get complete transaction history for a wallet
     */
    public List<WalletTransaction> getTransactionHistory(Long walletId, Integer limit) {
        List<WalletTransaction> transactions = transactionRepository.findByWalletIdOrderByCreatedAtDesc(walletId);
        
        if (limit != null && limit > 0) {
            return transactions.stream().limit(limit).toList();
        }
        
        return transactions;
    }
    
    /**
     * Get transaction chain (related transactions)
     * 
     * @param transactionId Starting transaction ID
     * @return All transactions in the chain
     */
    public List<WalletTransaction> getTransactionChain(Long transactionId) {
        Optional<WalletTransaction> startTransaction = transactionRepository.findById(transactionId);
        if (startTransaction.isEmpty()) {
            throw new IllegalArgumentException("Transaction not found: " + transactionId);
        }
        
        // Get all transactions related to this one
        List<WalletTransaction> chain = transactionRepository.findByRelatedTransactionId(transactionId);
        chain.add(0, startTransaction.get()); // Add starting transaction
        
        return chain;
    }
    
    /**
     * Get balance at specific point in time
     */
    public BigDecimal getBalanceAtTime(Long walletId, LocalDateTime atTime) {
        Optional<WalletTransaction> lastTransaction = transactionRepository
            .findByWalletIdAndDateRange(walletId, LocalDateTime.MIN, atTime)
            .stream()
            .filter(t -> t.getStatus() == TransactionStatus.COMPLETED)
            .findFirst();
        
        if (lastTransaction.isPresent() && lastTransaction.get().getBalanceAfter() != null) {
            return lastTransaction.get().getBalanceAfter();
        }
        
        // If no transactions, return wallet balance or 0
        Optional<Wallet> wallet = walletRepository.findById(walletId);
        return wallet.map(Wallet::getBalance).orElse(BigDecimal.ZERO);
    }
    
    /**
     * Track funds from operator and utilization in B2B
     * 
     * @param walletId Wallet ID
     * @return Fund tracking summary
     */
    public Map<String, Object> trackFundUtilization(Long walletId) {
        List<WalletTransaction> transactions = transactionRepository.findByWalletIdOrderByCreatedAtDesc(walletId);
        
        BigDecimal fundsFromOperator = BigDecimal.ZERO;
        BigDecimal fundsUtilized = BigDecimal.ZERO;
        BigDecimal fundsReturned = BigDecimal.ZERO;
        
        for (WalletTransaction t : transactions) {
            if (t.getStatus() != TransactionStatus.COMPLETED) {
                continue;
            }
            
            TransactionType type = t.getTransactionType();
            BigDecimal amount = t.getAmount();
            
            if (type == TransactionType.DEPOSIT || type == TransactionType.TRANSFER_IN) {
                fundsFromOperator = fundsFromOperator.add(amount);
            } else if (type == TransactionType.BET || type == TransactionType.WITHDRAWAL) {
                fundsUtilized = fundsUtilized.add(amount);
            } else if (type == TransactionType.TRANSFER_OUT) {
                fundsReturned = fundsReturned.add(amount);
            }
        }
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("walletId", walletId);
        summary.put("fundsFromOperator", fundsFromOperator);
        summary.put("fundsUtilized", fundsUtilized);
        summary.put("fundsReturned", fundsReturned);
        summary.put("netFundsInB2B", fundsFromOperator.subtract(fundsReturned).subtract(fundsUtilized));
        summary.put("transactionCount", transactions.size());
        
        return summary;
    }
    
    /**
     * Get daily transaction summary
     */
    public List<Map<String, Object>> getDailyTransactionSummary(Long walletId, int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        List<WalletTransaction> transactions = transactionRepository
            .findByWalletIdAndDateRange(walletId, startDate, LocalDateTime.now());
        
        // Group by date and type
        Map<String, Map<String, Object>> dailySummary = new HashMap<>();
        
        for (WalletTransaction t : transactions) {
            if (t.getStatus() != TransactionStatus.COMPLETED) {
                continue;
            }
            
            String date = t.getCreatedAt().toLocalDate().toString();
            String type = t.getTransactionType().name();
            
            dailySummary.computeIfAbsent(date + "_" + type, k -> {
                Map<String, Object> summary = new HashMap<>();
                summary.put("date", date);
                summary.put("transactionType", type);
                summary.put("count", 0);
                summary.put("totalAmount", BigDecimal.ZERO);
                return summary;
            });
            
            Map<String, Object> summary = dailySummary.get(date + "_" + type);
            summary.put("count", ((Integer) summary.get("count")) + 1);
            summary.put("totalAmount", 
                ((BigDecimal) summary.get("totalAmount")).add(t.getAmount()));
        }
        
        return dailySummary.values().stream()
            .sorted((a, b) -> ((String) b.get("date")).compareTo((String) a.get("date")))
            .toList();
    }
}
