package com.balaji.payment.wallet.service;

import com.balaji.payment.user.entity.UserEntity;
import com.balaji.payment.wallet.entity.WalletEntity;
import com.balaji.payment.wallet.enums.Currency;
import java.math.BigDecimal;
import java.util.UUID;

import java.util.List;
import com.balaji.payment.wallet.dto.response.WalletResponse;

public interface WalletService {
    WalletEntity initializeWallet(UserEntity user, boolean isPrimary, Currency currency);

    WalletEntity fetchUserPrimaryWallet(UUID userId);

    WalletEntity fetchBestUserWallet(UUID userId, Currency currency);

    boolean hasAtLeastBalance(UUID userId, BigDecimal amount);

    void handleTransaction(UUID senderId, UUID receiverId, BigDecimal senderAmount, BigDecimal receiverAmount);

    List<WalletResponse> getUserWallets(UUID userId);

    WalletResponse deposit(UUID userId, BigDecimal amount, Currency currency);

    WalletResponse addWallet(UUID userId, Currency currency);
}
