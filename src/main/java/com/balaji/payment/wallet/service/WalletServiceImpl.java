package com.balaji.payment.wallet.service;

import java.util.List;
import java.util.stream.Collectors;
import com.balaji.payment.wallet.dto.response.WalletResponse;

import org.springframework.stereotype.Service;
import com.balaji.payment.user.entity.UserEntity;
import com.balaji.payment.wallet.entity.WalletEntity;
import com.balaji.payment.wallet.enums.Currency;
import com.balaji.payment.wallet.enums.WalletStatus;
import com.balaji.payment.wallet.repository.WalletRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;

    public WalletEntity fetchUserPrimaryWallet(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        return walletRepository.findByUserIdAndIsPrimaryTrue(userId)
                .orElseThrow(() -> new RuntimeException("Primary wallet not found for user: " + userId));
    }

    public WalletEntity fetchBestUserWallet(UUID userId, Currency currency) {

        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        return walletRepository.findByUserIdAndCurrency(userId, currency)
                .orElseGet(() -> fetchUserPrimaryWallet(userId));
    }

    @Override
    @Transactional
    public void createWallet(UserEntity user, Currency currency) {
        WalletEntity wallet = new WalletEntity.Builder(user, currency)
                .withBalance(BigDecimal.ZERO)
                .withPrimary(true)
                .withStatus(WalletStatus.ACTIVE)
                .build();

        walletRepository.save(wallet);
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