package com.balaji.payment.reward.service;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.UUID;

@Service
public class RewardService {
    public void processTransactionRewards(UUID userId, BigDecimal amount) {
        // Placeholder for reward logic (e.g., 1% cashback)
        BigDecimal reward = amount.multiply(new BigDecimal("0.01"));
        System.out.println("REWARD for User [" + userId + "]: Calculated " + reward + " for transaction of " + amount);
    }
}