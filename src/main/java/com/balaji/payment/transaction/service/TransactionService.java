package com.balaji.payment.transaction.service;

import java.util.UUID;
import java.util.stream.Collectors;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.balaji.payment.transaction.dto.request.TransactionRequest;
import com.balaji.payment.transaction.dto.response.TransactionRecordResponse;
import com.balaji.payment.transaction.dto.response.TransactionResponse;
import com.balaji.payment.transaction.entity.TransactionEntity;
import com.balaji.payment.transaction.repository.TransactionRepository;
import com.balaji.payment.wallet.entity.WalletEntity;
import com.balaji.payment.wallet.enums.Currency;
import com.balaji.payment.wallet.service.WalletService;

import com.balaji.payment.config.PaymentProperties;
import com.balaji.payment.notification.service.NotificationService;
import com.balaji.payment.reward.service.RewardService;
import com.balaji.payment.transaction.enums.TransactionStatus;
import com.balaji.payment.user.dto.response.UserSimpleResponse;
import com.balaji.payment.user.entity.UserEntity;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final WalletService walletService;
    private final TransactionRepository transactionRepository;
    private final NotificationService notificationService;
    private final RewardService rewardService;
    private final PaymentProperties paymentProperties;

    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request) {
        // 0. Idempotency Check
        Optional<TransactionEntity> existingTx = transactionRepository
                .findByIdempotencyKey(request.getIdempotencyKey());
        if (existingTx.isPresent()) {
            return mapToResponse(existingTx.get());
        }

        validateRequest(request);

        WalletEntity senderWallet = walletService.fetchUserPrimaryWallet(request.getSenderId());
        if (senderWallet.getBalance().compareTo(request.getAmount()) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        WalletEntity receiverWallet = walletService.fetchBestUserWallet(request.getReceiverId(),
                senderWallet.getCurrency());

        BigDecimal exchangeFee = calculateExchangeFee(senderWallet.getCurrency(),
                receiverWallet.getCurrency(),
                request.getAmount());

        BigDecimal netAmount = request.getAmount().subtract(exchangeFee);

        BigDecimal exchangeRate = senderWallet.getCurrency() == receiverWallet.getCurrency() ? BigDecimal.ONE
                : paymentProperties.getExchange().getDefaultRate();

        BigDecimal receiverAmount = netAmount.multiply(exchangeRate);

        walletService.handleTransaction(senderWallet.getId(), receiverWallet.getId(), request.getAmount(),
                receiverAmount);

        // 4. Save Transaction Record
        TransactionEntity transaction = TransactionEntity.builder()
                .sender(senderWallet.getUser())
                .receiver(receiverWallet.getUser())
                .feeAmount(exchangeFee)
                .srcAmount(request.getAmount())
                .srcWallet(senderWallet)
                .tgtAmount(receiverAmount)
                .tgtWallet(receiverWallet)
                .exchangeRate(exchangeRate)
                .status(TransactionStatus.SUCCESS)
                .idempotencyKey(request.getIdempotencyKey())
                .build();

        TransactionEntity savedTx = transactionRepository.save(transaction);

        // 5. Post-transaction actions
        notificationService.sendPaymentNotification(request.getSenderId(),
                "Successfully sent " + request.getAmount() + " to " + receiverWallet.getUser().getName());
        notificationService.sendPaymentNotification(request.getReceiverId(),
                "Received " + receiverAmount + " " + receiverWallet.getCurrency() + " from "
                        + senderWallet.getUser().getName());

        rewardService.processTransactionRewards(request.getSenderId(), request.getAmount());

        return mapToResponse(savedTx);

    }

    public List<TransactionRecordResponse> getTransactionHistory(UUID userId) {
        return transactionRepository.findBySenderIdOrReceiverIdOrderByCreatedAtDesc(userId, userId)
                .stream()
                .map(this::mapToRecordResponse)
                .collect(Collectors.toList());
    }

    public BigDecimal calculateExchangeFee(Currency sendCurrency, Currency receiverCurrency, BigDecimal amount) {
        if (sendCurrency == receiverCurrency) {
            return BigDecimal.ZERO;
        }
        return amount.multiply(paymentProperties.getExchange().getFeeRate());
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
                .tgtAmount(tx.getTgtAmount())
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

    private TransactionResponse mapToResponse(TransactionEntity tx) {
        return TransactionResponse.builder()
                .transactionId(tx.getId())
                .transactionStatus(tx.getStatus())
                .message("Wallet transaction successful")
                .build();
    }

}