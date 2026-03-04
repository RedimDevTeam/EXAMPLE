package com.b2bplatform.wallet.service;

import com.b2bplatform.wallet.dto.b2b.FundTransferConfirmRequest;
import com.b2bplatform.wallet.dto.b2b.FundTransferConfirmResponse;
import com.b2bplatform.wallet.dto.b2b.FundTransferRequest;
import com.b2bplatform.wallet.dto.b2b.FundTransferRequestResponse;
import com.b2bplatform.wallet.dto.b2b.FundTransferStatusResponse;
import com.b2bplatform.wallet.model.PendingFundTransaction;
import com.b2bplatform.wallet.model.UnitType;
import com.b2bplatform.wallet.model.Wallet;
import com.b2bplatform.wallet.model.Wallet.WalletModel;
import com.b2bplatform.wallet.model.WalletTransaction;
import com.b2bplatform.wallet.model.WalletTransaction.TransactionStatus;
import com.b2bplatform.wallet.repository.PendingFundTransactionRepository;
import com.b2bplatform.wallet.repository.WalletRepository;
import com.b2bplatform.wallet.repository.WalletTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * B2B Fund Transfer Service
 * Implements two-step flow: Request → Confirm
 * 
 * Flow:
 * 1. requestFund() - Create pending transaction (status: PENDING)
 * 2. confirmFund() - Confirm pending transaction (status: CONFIRMED)
 * 3. Auto-expire old pending transactions (scheduled job)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class B2BFundTransferService {
    
    private final PendingFundTransactionRepository pendingRepository;
    private final WalletTransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final OperatorServiceClient operatorServiceClient;
    private final WalletManagementService walletManagementService;
    private final EnhancedWalletTransactionService enhancedTransactionService;
    
    /**
     * Step 1: Request Fund Transfer
     * Creates a pending transaction that must be confirmed within expiration time
     */
    @Transactional
    public FundTransferRequestResponse requestFund(FundTransferRequest request) {
        log.info("Fund Transfer Request - operatorCode: {}, playerId: {}, amount: {}, type: {}", 
            request.getOperatorCode(), request.getPlayerId(), request.getAmount(), request.getTransactionType());
        
        // Convert operatorCode to operatorId
        Long operatorId = operatorServiceClient.getOperatorIdByCode(request.getOperatorCode());
        if (operatorId == null) {
            throw new IllegalArgumentException("Operator not found: " + request.getOperatorCode());
        }
        
        // Convert amount based on unitType
        BigDecimal amount = convertAmount(request.getAmount(), request.getUnitType());
        
        // Calculate expiration time
        int expirationMinutes = request.getExpirationMinutes() != null ? request.getExpirationMinutes() : 15;
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(expirationMinutes);
        
        // Create pending transaction
        PendingFundTransaction pending = PendingFundTransaction.builder()
            .operatorId(operatorId)
            .playerId(request.getPlayerId())
            .amount(amount)
            .currency(request.getCurrency())
            .unitType(request.getUnitType())
            .transactionType(mapToPendingType(request.getTransactionType()))
            .status(PendingFundTransaction.PendingStatus.PENDING)
            .expiresAt(expiresAt)
            .createdBy("SYSTEM")
            .build();
        
        pending = pendingRepository.save(pending);
        
        log.info("Created pending fund transaction - paymentId: {}, expiresAt: {}", 
            pending.getPaymentId(), pending.getExpiresAt());
        
        return FundTransferRequestResponse.builder()
            .paymentId(pending.getPaymentId())
            .status("PENDING")
            .expiresAt(pending.getExpiresAt())
            .message("Fund transfer request created. Please confirm within " + expirationMinutes + " minutes.")
            .build();
    }
    
    /**
     * Step 2: Confirm Fund Transfer
     * Confirms a pending transaction and processes the actual fund transfer
     */
    @Transactional
    public FundTransferConfirmResponse confirmFund(FundTransferConfirmRequest request) {
        log.info("Fund Transfer Confirm - paymentId: {}, transactionId: {}", 
            request.getPaymentId(), request.getTransactionId());
        
        // Find pending transaction
        PendingFundTransaction pending = pendingRepository.findByPaymentId(request.getPaymentId())
            .orElseThrow(() -> new IllegalArgumentException("Pending transaction not found: " + request.getPaymentId()));
        
        // Validate status
        if (!pending.canBeConfirmed()) {
            if (pending.isExpired()) {
                pending.setStatus(PendingFundTransaction.PendingStatus.EXPIRED);
                pendingRepository.save(pending);
                throw new IllegalStateException("Pending transaction has expired: " + request.getPaymentId());
            }
            throw new IllegalStateException("Pending transaction cannot be confirmed. Status: " + pending.getStatus());
        }
        
        // Check idempotency (if transactionId already exists)
        Optional<WalletTransaction> existingTransaction = transactionRepository
            .findByTransactionId(request.getTransactionId());
        if (existingTransaction.isPresent()) {
            log.warn("Duplicate transaction ID detected - transactionId: {}, returning existing transaction", 
                request.getTransactionId());
            
            // Update pending as confirmed
            pending.setStatus(PendingFundTransaction.PendingStatus.CONFIRMED);
            pending.setConfirmedBy("SYSTEM");
            pendingRepository.save(pending);
            
            WalletTransaction txn = existingTransaction.get();
            return FundTransferConfirmResponse.builder()
                .status(0)
                .balance(txn.getBalanceAfter() != null ? txn.getBalanceAfter() : BigDecimal.ZERO)
                .currency(txn.getCurrency())
                .unitType(pending.getUnitType().name())
                .transactionId(txn.getTransactionId())
                .paymentId(pending.getPaymentId())
                .message("Transaction already processed")
                .build();
        }
        
        // Get or create wallet (Fund Transfer model)
        Wallet wallet = walletManagementService.getOrCreateWallet(
            pending.getOperatorId(),
            pending.getPlayerId(),
            WalletModel.FUND_TRANSFER,
            pending.getCurrency()
        );
        
        // Process transaction based on type
        Map<String, Object> transactionResult;
        BigDecimal newBalance;
        
        if (pending.getTransactionType() == PendingFundTransaction.FundTransferType.DEPOSIT) {
            // Deposit: Credit wallet
            transactionResult = processDeposit(wallet, pending, request.getTransactionId());
        } else {
            // Withdrawal: Debit wallet
            transactionResult = processWithdrawal(wallet, pending, request.getTransactionId());
        }
        
        // Get balance from result or refresh wallet
        Object balanceObj = transactionResult.get("balance");
        if (balanceObj instanceof BigDecimal) {
            newBalance = (BigDecimal) balanceObj;
        } else {
            // Refresh wallet to get latest balance
            wallet = walletRepository.findByOperatorIdAndPlayerId(pending.getOperatorId(), pending.getPlayerId())
                .orElse(wallet);
            newBalance = wallet.getBalance();
        }
        
        // Get transaction ID from result
        String actualTransactionId = (String) transactionResult.get("transactionId");
        if (actualTransactionId == null) {
            actualTransactionId = request.getTransactionId();
        }
        
        // Update pending transaction as confirmed
        pending.setStatus(PendingFundTransaction.PendingStatus.CONFIRMED);
        pending.setConfirmedAt(LocalDateTime.now());
        pending.setConfirmedBy("SYSTEM");
        pendingRepository.save(pending);
        
        log.info("Fund transfer confirmed - paymentId: {}, transactionId: {}, newBalance: {}", 
            pending.getPaymentId(), request.getTransactionId(), newBalance);
        
        return FundTransferConfirmResponse.builder()
            .status(0)
            .balance(newBalance)
            .currency(pending.getCurrency())
            .unitType(pending.getUnitType().name())
            .transactionId(actualTransactionId)
            .paymentId(pending.getPaymentId())
            .message("Fund transfer confirmed successfully")
            .build();
    }
    
    /**
     * Get status of pending fund transfer
     */
    public FundTransferStatusResponse getStatus(String paymentId) {
        log.debug("Getting fund transfer status - paymentId: {}", paymentId);
        
        PendingFundTransaction pending = pendingRepository.findByPaymentId(paymentId)
            .orElseThrow(() -> new IllegalArgumentException("Pending transaction not found: " + paymentId));
        
        // Check if expired
        if (pending.isExpired() && pending.getStatus() == PendingFundTransaction.PendingStatus.PENDING) {
            pending.setStatus(PendingFundTransaction.PendingStatus.EXPIRED);
            pendingRepository.save(pending);
        }
        
        return FundTransferStatusResponse.builder()
            .paymentId(pending.getPaymentId())
            .status(pending.getStatus())
            .amount(pending.getAmount())
            .currency(pending.getCurrency())
            .unitType(pending.getUnitType().name())
            .transactionType(pending.getTransactionType().name())
            .expiresAt(pending.getExpiresAt())
            .createdAt(pending.getCreatedAt())
            .confirmedAt(pending.getConfirmedAt())
            .confirmedBy(pending.getConfirmedBy())
            .operatorResponse(pending.getOperatorResponse())
            .build();
    }
    
    /**
     * Get all pending transactions for operator and player
     */
    public List<FundTransferStatusResponse> getPendingTransactions(String operatorCode, String playerId) {
        log.debug("Getting pending transactions - operatorCode: {}, playerId: {}", operatorCode, playerId);
        
        Long operatorId = operatorServiceClient.getOperatorIdByCode(operatorCode);
        if (operatorId == null) {
            throw new IllegalArgumentException("Operator not found: " + operatorCode);
        }
        
        List<PendingFundTransaction> pendingList = pendingRepository
            .findByOperatorIdAndPlayerIdAndStatus(operatorId, playerId, PendingFundTransaction.PendingStatus.PENDING);
        
        return pendingList.stream()
            .map(this::mapToStatusResponse)
            .toList();
    }
    
    /**
     * Auto-expire old pending transactions
     * Runs every 5 minutes
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    @Transactional
    public void expirePendingTransactions() {
        log.debug("Running scheduled job to expire pending transactions");
        
        LocalDateTime now = LocalDateTime.now();
        List<PendingFundTransaction> expired = pendingRepository.findExpiredPending(now);
        
        for (PendingFundTransaction pending : expired) {
            log.info("Expiring pending transaction - paymentId: {}, expiredAt: {}", 
                pending.getPaymentId(), pending.getExpiresAt());
            pending.setStatus(PendingFundTransaction.PendingStatus.EXPIRED);
            pendingRepository.save(pending);
        }
        
        if (!expired.isEmpty()) {
            log.info("Expired {} pending transactions", expired.size());
        }
    }
    
    // ============================================================================
    // Helper Methods
    // ============================================================================
    
    /**
     * Process deposit (credit wallet)
     */
    private Map<String, Object> processDeposit(Wallet wallet, PendingFundTransaction pending, String transactionId) {
        return enhancedTransactionService.processCredit(
            pending.getOperatorId(),
            pending.getPlayerId(),
            pending.getAmount(),
            pending.getCurrency(),
            transactionId,
            "Fund transfer deposit - " + pending.getPaymentId(),
            com.b2bplatform.wallet.model.TransactionType.DEPOSIT,
            WalletModel.FUND_TRANSFER,
            null
        );
    }
    
    /**
     * Process withdrawal (debit wallet)
     */
    private Map<String, Object> processWithdrawal(Wallet wallet, PendingFundTransaction pending, String transactionId) {
        return enhancedTransactionService.processDebit(
            pending.getOperatorId(),
            pending.getPlayerId(),
            pending.getAmount(),
            pending.getCurrency(),
            transactionId,
            "Fund transfer withdrawal - " + pending.getPaymentId(),
            com.b2bplatform.wallet.model.TransactionType.WITHDRAWAL,
            WalletModel.FUND_TRANSFER,
            null
        );
    }
    
    /**
     * Convert amount based on unitType
     */
    private BigDecimal convertAmount(BigDecimal amount, UnitType unitType) {
        if (amount == null) {
            return null;
        }
        
        if (unitType == null || unitType == UnitType.DECIMAL) {
            return amount; // Already in decimal format
        } else if (unitType == UnitType.CENTS) {
            return UnitType.centsToDecimal(amount); // Convert cents to decimal
        }
        
        return amount; // Default to decimal
    }
    
    /**
     * Map FundTransferRequest.FundTransferType to PendingFundTransaction.FundTransferType
     */
    private PendingFundTransaction.FundTransferType mapToPendingType(FundTransferRequest.FundTransferType type) {
        return switch (type) {
            case DEPOSIT -> PendingFundTransaction.FundTransferType.DEPOSIT;
            case WITHDRAWAL -> PendingFundTransaction.FundTransferType.WITHDRAWAL;
        };
    }
    
    /**
     * Map PendingFundTransaction to FundTransferStatusResponse
     */
    private FundTransferStatusResponse mapToStatusResponse(PendingFundTransaction pending) {
        return FundTransferStatusResponse.builder()
            .paymentId(pending.getPaymentId())
            .status(pending.getStatus())
            .amount(pending.getAmount())
            .currency(pending.getCurrency())
            .unitType(pending.getUnitType().name())
            .transactionType(pending.getTransactionType().name())
            .expiresAt(pending.getExpiresAt())
            .createdAt(pending.getCreatedAt())
            .confirmedAt(pending.getConfirmedAt())
            .confirmedBy(pending.getConfirmedBy())
            .operatorResponse(pending.getOperatorResponse())
            .build();
    }
}
