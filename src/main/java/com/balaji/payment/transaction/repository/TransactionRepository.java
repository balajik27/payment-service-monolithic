package com.balaji.payment.transaction.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.balaji.payment.transaction.entity.TransactionEntity;
import com.balaji.payment.transaction.enums.TransactionStatus;
import com.balaji.payment.transaction.enums.TransactionType;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, UUID> {
    List<TransactionEntity> findBySrcWalletUserIdOrTgtWalletUserIdOrderByCreatedAtDesc(UUID senderId, UUID receiverId);

    Optional<TransactionEntity> findByIdAndTypeAndStatus(
            UUID originalTransactionId,
            TransactionType type,
            TransactionStatus status);

    boolean existsByReferenceTransactionIdAndTypeAndStatus(
            UUID originalTransactionId,
            TransactionType type,
            TransactionStatus status);

    java.util.Optional<TransactionEntity> findByIdempotencyKey(String idempotencyKey);
}
