package com.balaji.payment.wallet.repository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.balaji.payment.wallet.entity.WalletEntity;
import com.balaji.payment.wallet.enums.Currency;

import jakarta.persistence.LockModeType;

import java.util.List;

public interface WalletRepository extends JpaRepository<WalletEntity, UUID> {

    List<WalletEntity> findAllByUserId(UUID userId);

    // Custom query method to find a wallet by the user's ID
    Optional<WalletEntity> findByUserIdAndIsPrimaryTrue(UUID userId);

    Optional<WalletEntity> findByUserIdAndCurrency(UUID userId, Currency currency);

    boolean existsByIdAndBalanceGreaterThanEqual(UUID id, BigDecimal balance);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM WalletEntity w WHERE w.id = :id")
    Optional<WalletEntity> findByIdForUpdate(@Param("id") UUID id);

    boolean existsByUserIdAndCurrency(UUID userId, Currency currency);

    @Modifying
    @Query("UPDATE WalletEntity w SET w.balance = w.balance - :amount WHERE w.id = :id AND w.balance >= :amount")
    int deductBalance(@Param("id") UUID id, @Param("amount") BigDecimal amount);

    @Modifying
    @Query("UPDATE WalletEntity w SET w.balance = w.balance + :amount WHERE w.id = :id")
    int addBalance(@Param("id") UUID id, @Param("amount") BigDecimal amount);

}
