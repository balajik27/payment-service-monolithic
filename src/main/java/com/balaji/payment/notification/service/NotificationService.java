package com.balaji.payment.notification.service;

import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class NotificationService {
    public void sendPaymentNotification(UUID userId, String message) {
        // Placeholder for real notification logic (Email/SMS/Push)
        System.out.println("NOTIFICATION to User [" + userId + "]: " + message);
    }
}