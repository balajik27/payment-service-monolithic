package com.balaji.payment.transaction.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import com.balaji.payment.wallet.entity.WalletEntity;

import com.balaji.payment.transaction.enums.TransactionStatus;
import com.balaji.payment.transaction.enums.TransactionType;
import com.balaji.payment.user.entity.UserEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransactionStatus status;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private UserEntity sender;

    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    private UserEntity receiver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "src_wallet_id", nullable = false)
    private WalletEntity srcWallet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tgt_wallet_id", nullable = false)
    private WalletEntity tgtWallet;

    @Column(name = "src_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal srcAmount;

    @Column(name = "tgt_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal tgtAmount;

    @Column(name = "fee_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal feeAmount;

    @Column(name = "ex_rate", nullable = false, precision = 19, scale = 4)
    private BigDecimal exchangeRate;

    @Column(name = "src_currency", nullable = false)
    private String srcCurrency;

    @Column(name = "tgt_currency", nullable = false)
    private String tgtCurrency;

    @ManyToOne
    @JoinColumn(name = "reference_transaction_id")
    private TransactionEntity referenceTransaction;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "idempotency_key", unique = true, nullable = false)
    private String idempotencyKey;

}
