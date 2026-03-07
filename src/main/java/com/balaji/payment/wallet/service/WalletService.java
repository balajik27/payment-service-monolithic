package com.balaji.payment.wallet.service;

import com.balaji.payment.user.entity.UserEntity;
import com.balaji.payment.wallet.entity.WalletEntity;
import com.balaji.payment.wallet.enums.Currency;
import java.math.BigDecimal;
import java.util.UUID;

import java.util.List;
import com.balaji.payment.wallet.dto.response.WalletResponse;

public interface WalletService {
    void createWallet(UserEntity user, Currency currency);

    WalletEntity fetchUserPrimaryWallet(UUID userId);

    WalletEntity fetchBestUserWallet(UUID userId, Currency currency);

    void handleTransaction(UUID senderId, UUID receiverId, BigDecimal senderAmount, BigDecimal receiverAmount);

    List<WalletResponse> getUserWallets(UUID userId);
}
