package com.balaji.payment.transaction.service;

import java.util.UUID;
import java.util.stream.Collectors;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.balaji.payment.transaction.dto.request.RefundRequest;
import com.balaji.payment.transaction.dto.request.TransactionRequest;
import com.balaji.payment.transaction.dto.response.TransactionRecordResponse;
import com.balaji.payment.transaction.dto.response.TransactionResponse;
import com.balaji.payment.transaction.entity.TransactionEntity;
import com.balaji.payment.transaction.repository.TransactionRepository;
import com.balaji.payment.wallet.entity.WalletEntity;
import com.balaji.payment.wallet.service.WalletService;

import com.balaji.payment.transaction.enums.TransactionStatus;
import com.balaji.payment.transaction.enums.TransactionType;
import com.balaji.payment.user.dto.response.UserSimpleResponse;
import com.balaji.payment.user.entity.UserEntity;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final WalletService walletService;
    private final TransactionRepository transactionRepository;
    private final ExchangeFeeService exchangeFeeService;

    public List<TransactionRecordResponse> getTransactionHistory(UUID userId) {
        return transactionRepository.findBySrcWalletUserIdOrTgtWalletUserIdOrderByCreatedAtDesc(userId, userId)
                .stream()
                .map(this::mapToRecordResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request) {
        // 0. Idempotency Check
        Optional<TransactionEntity> existingTx = transactionRepository
                .findByIdempotencyKey(request.getIdempotencyKey());
        if (existingTx.isPresent()) {
            return mapToResponse(existingTx.get(), "Wallet transaction successful");
        }

        validateRequest(request);

        WalletEntity senderWallet = walletService.fetchUserPrimaryWallet(request.getSenderId());
        if (senderWallet.getBalance().compareTo(request.getAmount()) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        WalletEntity receiverWallet = walletService.fetchBestUserWallet(request.getReceiverId(),
                senderWallet.getCurrency());

        BigDecimal exchangeFee = exchangeFeeService.calculateExchangeFee(senderWallet.getCurrency(),
                receiverWallet.getCurrency(),
                request.getAmount());

        BigDecimal exchangeRate = exchangeFeeService.getExchangeRate(
                senderWallet.getCurrency(),
                receiverWallet.getCurrency());

        BigDecimal netAmount = request.getAmount().subtract(exchangeFee);
        BigDecimal receiverAmount = netAmount.multiply(exchangeRate);

        // 1. Create PENDING Transaction Record (for Idempotency Reservation)
        TransactionEntity transaction = TransactionEntity.builder()
                .sender(senderWallet.getUser())
                .receiver(receiverWallet.getUser())
                .feeAmount(exchangeFee)
                .srcAmount(request.getAmount())
                .srcWallet(senderWallet)
                .srcCurrency(senderWallet.getCurrency().name())
                .tgtAmount(receiverAmount)
                .tgtWallet(receiverWallet)
                .tgtCurrency(receiverWallet.getCurrency().name())
                .exchangeRate(exchangeRate)
                .type(TransactionType.PAYMENT) // Add this missing field
                .status(TransactionStatus.PENDING)
                .idempotencyKey(request.getIdempotencyKey())
                .build();

        TransactionEntity reservedTransaction = reserveIdempotencyKey(transaction, request.getIdempotencyKey());

        try {
            // 2. Execute Wallet Balance Updates
            walletService.handleTransaction(senderWallet.getId(), receiverWallet.getId(), request.getAmount(),
                    receiverAmount);
            // 3. Mark as SUCCESS
            reservedTransaction.setStatus(TransactionStatus.SUCCESS);
            transactionRepository.save(reservedTransaction);

            return mapToResponse(reservedTransaction, "Wallet transaction successful");
        } catch (Exception err) {
            reservedTransaction.setStatus(TransactionStatus.FAILED);
            throw new RuntimeException("Money movement failed: " + err.getMessage());
        }

    }

    @Transactional
    public TransactionResponse refundTransaction(RefundRequest request) {

        // idempotency key check
        Optional<TransactionEntity> existingTx = transactionRepository
                .findByIdempotencyKey(request.getIdempotencyKey());
        if (existingTx.isPresent()) {
            return mapToResponse(existingTx.get(), "Refunded Successfully");
        }

        // check if already refunded & success for this id
        if (transactionRepository.existsByReferenceTransactionIdAndTypeAndStatus(request.getTransactionId(),
                TransactionType.REFUND, TransactionStatus.SUCCESS)) {
            throw new RuntimeException("Already refunded for this transaction");
        }

        if (transactionRepository.existsByReferenceTransactionIdAndTypeAndStatus(request.getTransactionId(),
                TransactionType.REFUND, TransactionStatus.PENDING)) {
            throw new RuntimeException("Refund is processing");
        }

        existingTx = transactionRepository.findByIdAndTypeAndStatus(request.getTransactionId(), TransactionType.PAYMENT,
                TransactionStatus.SUCCESS);
        if (existingTx.isEmpty()) {
            throw new RuntimeException("No Successfull Transaction found");
        }

        TransactionEntity tx = existingTx.get();

        if (!walletService.hasAtLeastBalance(tx.getTgtWallet().getId(), tx.getTgtAmount())) {
            throw new RuntimeException("Insufficient balance to refund");
        }

        TransactionEntity refundTx = TransactionEntity.builder()
                .sender(tx.getReceiver())
                .receiver(tx.getSender())
                .feeAmount(BigDecimal.ZERO)
                .srcAmount(tx.getTgtAmount())
                .srcWallet(tx.getTgtWallet())
                .srcCurrency(tx.getTgtCurrency())
                .tgtAmount(tx.getSrcAmount().subtract(tx.getFeeAmount()))
                .tgtWallet(tx.getSrcWallet())
                .tgtCurrency(tx.getSrcCurrency())
                .exchangeRate(tx.getExchangeRate())
                .type(TransactionType.REFUND)
                .referenceTransaction(tx)
                .status(TransactionStatus.PENDING)
                .idempotencyKey(request.getIdempotencyKey())
                .build();

        refundTx = transactionRepository.save(refundTx);

        try {

            walletService.handleTransaction(refundTx.getSrcWallet().getId(), refundTx.getTgtWallet().getId(),
                    refundTx.getSrcAmount(),
                    refundTx.getTgtAmount());

            refundTx.setStatus(TransactionStatus.SUCCESS);

        } catch (Exception err) {
            refundTx.setStatus(TransactionStatus.FAILED);
            throw new RuntimeException("Money movement failed: " + err.getMessage());
        }

        return mapToResponse(transactionRepository.save(refundTx), "Refund processed successfully");
    }

    private TransactionEntity reserveIdempotencyKey(TransactionEntity transaction, String idempotencyKey) {
        try {
            return transactionRepository.saveAndFlush(transaction);
        } catch (DataIntegrityViolationException ex) {
            // Check if it's actually an idempotency key conflict
            TransactionEntity existing = transactionRepository.findByIdempotencyKey(idempotencyKey)
                    .orElseThrow(() -> ex); // Rethrow the original DB exception if it's NOT an idempotency conflict

            // before
            // .orElseThrow(() -> new RuntimeException(
            // "Duplicate idempotency key detected, but existing transaction could not be
            // loaded."));

            if (!sameTransactionRequest(existing, transaction)) {
                throw new RuntimeException("Idempotency key is already used for a different transaction request.");
            }

            return existing;
        }
    }

    private boolean sameTransactionRequest(TransactionEntity existing, TransactionEntity incoming) {
        if (existing == null || incoming == null) {
            return false;
        }

        boolean sameSender = existing.getSender() != null && incoming.getSender() != null
                && existing.getSender().getId().equals(incoming.getSender().getId());

        boolean sameReceiver = existing.getReceiver() != null && incoming.getReceiver() != null
                && existing.getReceiver().getId().equals(incoming.getReceiver().getId());

        boolean sameSourceWallet = existing.getSrcWallet() != null && incoming.getSrcWallet() != null
                && existing.getSrcWallet().getId().equals(incoming.getSrcWallet().getId());

        boolean sameTargetWallet = existing.getTgtWallet() != null && incoming.getTgtWallet() != null
                && existing.getTgtWallet().getId().equals(incoming.getTgtWallet().getId());

        boolean sameSourceAmount = existing.getSrcAmount() != null && incoming.getSrcAmount() != null
                && existing.getSrcAmount().compareTo(incoming.getSrcAmount()) == 0;

        return sameSender
                && sameReceiver
                && sameSourceWallet
                && sameTargetWallet
                && sameSourceAmount;
    }

    public boolean validateRequest(TransactionRequest request) {
        if (request.getSenderId().equals(request.getReceiverId())) {
            throw new RuntimeException("Cannot transfer money to yourself");
        }
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Transaction amount must be greater than zero");
        }

        // Example currency validation logic
        // WalletEntity senderWallet =
        // walletService.fetchUserPrimaryWallet(request.getSenderId());
        // if
        // (!paymentProperties.getSupportedCurrencies().contains(senderWallet.getCurrency()))
        // {
        // throw new RuntimeException("Currency " + senderWallet.getCurrency() + " is
        // not supported");
        // }

        return true;
    }

    private TransactionRecordResponse mapToRecordResponse(TransactionEntity tx) {
        return TransactionRecordResponse.builder()
                .transactionId(tx.getId())
                .transactionStatus(tx.getStatus())
                .sender(mapToSimpleUserResponse(tx.getSender()))
                .receiver(mapToSimpleUserResponse(tx.getReceiver()))
                .srcAmount(tx.getSrcAmount())
                .srcCurrency(tx.getSrcCurrency())
                .tgtAmount(tx.getTgtAmount())
                .tgtCurrency(tx.getTgtCurrency())
                .feeAmount(tx.getFeeAmount())
                .exchangeRate(tx.getExchangeRate())
                .createdAt(tx.getCreatedAt())
                .build();
    }

    private UserSimpleResponse mapToSimpleUserResponse(UserEntity user) {
        if (user == null)
            return null;
        return UserSimpleResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    private TransactionResponse mapToResponse(TransactionEntity tx, String message) {
        return TransactionResponse.builder()
                .transactionId(tx.getId())
                .transactionStatus(tx.getStatus())
                .message(message)
                .build();
    }

}