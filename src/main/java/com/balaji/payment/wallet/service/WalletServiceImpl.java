package com.balaji.payment.wallet.service;

import java.util.List;
import java.util.stream.Collectors;
import com.balaji.payment.wallet.dto.response.WalletResponse;

import org.springframework.stereotype.Service;
import com.balaji.payment.user.entity.UserEntity;
import com.balaji.payment.user.service.UserService;
import com.balaji.payment.wallet.entity.WalletEntity;
import com.balaji.payment.wallet.enums.Currency;
import com.balaji.payment.wallet.enums.WalletStatus;
import com.balaji.payment.wallet.repository.WalletRepository;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.annotation.Lazy;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class WalletServiceImpl implements WalletService {

    private final UserService userService;
    private final WalletRepository walletRepository;

    public WalletServiceImpl(@Lazy UserService userService, WalletRepository walletRepository) {
        this.userService = userService;
        this.walletRepository = walletRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public WalletEntity fetchUserPrimaryWallet(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        return walletRepository.findByUserIdAndIsPrimaryTrue(userId)
                .orElseThrow(() -> new RuntimeException("Primary wallet not found for user: " + userId));
    }

    @Override
    @Transactional(readOnly = true)
    public WalletEntity fetchBestUserWallet(UUID userId, Currency currency) {

        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        return walletRepository.findByUserIdAndCurrency(userId, currency)
                .orElseGet(() -> fetchUserPrimaryWallet(userId));
    }

    @Override
    @Transactional
    public WalletEntity initializeWallet(UserEntity user, boolean isPrimary, Currency currency) {
        BigDecimal initialBalance = isPrimary ? new BigDecimal("100.00") : BigDecimal.ZERO;

        WalletEntity wallet = new WalletEntity.Builder(user, currency)
                .withBalance(initialBalance)
                .withPrimary(isPrimary)
                .withStatus(WalletStatus.ACTIVE)
                .build();

        return walletRepository.save(wallet);
    }

    @Override
    @Transactional
    public WalletResponse deposit(UUID userId, BigDecimal amount, Currency currency) {
        WalletEntity wallet;
        if (currency == null) {
            wallet = fetchUserPrimaryWallet(userId);
        } else {
            wallet = walletRepository.findByUserIdAndCurrency(userId, currency)
                    .orElseThrow(() -> new RuntimeException("Wallet not found for currency: " + currency));
        }

        int rowsUpdated = walletRepository.addBalance(wallet.getId(), amount);
        if (rowsUpdated == 0) {
            throw new RuntimeException("Deposit failed: Wallet not found or update error.");
        }

        // Refresh from DB to get updated balance
        WalletEntity updatedWallet = walletRepository.findById(wallet.getId())
                .orElseThrow(() -> new RuntimeException("Wallet not found after update"));

        return mapToResponse(updatedWallet);
    }

    @Override
    @Transactional
    public WalletResponse addWallet(UUID userId, Currency currency) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (currency == null) {
            throw new IllegalArgumentException("Currency cannot be null");
        }

        if (walletRepository.existsByUserIdAndCurrency(userId, currency)) {
            throw new RuntimeException("Wallet already exists for user in " + currency);
        }

        UserEntity user = userService.fetchUserById(userId);

        WalletEntity wallet = initializeWallet(user, false, currency);

        return mapToResponse(wallet);
    }

    @Override
    @Transactional
    public void handleTransaction(UUID senderWalletId, UUID receiverWalletId, BigDecimal senderAmount,
            BigDecimal receiverAmount) {

        int rowsUpdated = walletRepository.deductBalance(senderWalletId, senderAmount);
        if (rowsUpdated == 0) {
            throw new RuntimeException("Transaction failed: Insufficient balance or sender wallet not found.");
        }

        int rowsAdded = walletRepository.addBalance(receiverWalletId, receiverAmount);
        if (rowsAdded == 0) {
            throw new RuntimeException("Transaction failed: Receiver wallet not found.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<WalletResponse> getUserWallets(UUID userId) {
        return walletRepository.findAllByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private WalletResponse mapToResponse(WalletEntity wallet) {
        return WalletResponse.builder()
                .id(wallet.getId())
                .userId(wallet.getUser().getId())
                .balance(wallet.getBalance())
                .currency(wallet.getCurrency())
                .status(wallet.getStatus())
                .isPrimary(wallet.isPrimary())
                .createdAt(wallet.getCreatedAt())
                .updatedAt(wallet.getUpdatedAt())
                .build();
    }
}

// if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
// throw new IllegalArgumentException("Transfer amount must be positive");
// }

// if (fromUserId.equals(toUserId)) {
// throw new IllegalArgumentException("Cannot transfer to the same wallet");
// }

// WalletEntity senderWallet =
// walletRepository.findByUserIdAndCurrency(fromUserId, currency)
// .orElseThrow(() -> new RuntimeException("Sender wallet not found for
// currency: " + currency));

// WalletEntity receiverWallet =
// walletRepository.findByUserIdAndCurrency(toUserId, currency)
// .orElseThrow(() -> new RuntimeException("Receiver wallet not found for
// currency: " + currency));

// if (senderWallet.getBalance().compareTo(amount) < 0) {
// throw new RuntimeException("Insufficient balance in sender wallet");
// }

// senderWallet.setBalance(senderWallet.getBalance().subtract(amount));
// receiverWallet.setBalance(receiverWallet.getBalance().add(amount));

// walletRepository.save(senderWallet);
// walletRepository.save(receiverWallet);