package org.delcom.ifs25025springbootcashflow.domain.entity;

import java.time.LocalDateTime;

import org.delcom.ifs25025springbootcashflow.domain.exception.ValidationException;

public class Transaction {
    private final int id;
    private final int userId;
    private final String description;
    private final long amount;
    private final TransactionType type;
    private final String proofPath;
    private final LocalDateTime createdAt;

    public Transaction(
            int id,
            int userId,
            String description,
            long amount,
            TransactionType type,
            String proofPath,
            LocalDateTime createdAt) {
        if (id < 0) {
            throw new ValidationException("ID transaksi tidak valid");
        }
        if (userId <= 0) {
            throw new ValidationException("ID pengguna tidak valid");
        }
        if (description == null || description.isBlank()) {
            throw new ValidationException("Keterangan transaksi tidak boleh kosong");
        }
        if (amount <= 0) {
            throw new ValidationException("Jumlah transaksi harus lebih dari nol");
        }
        if (type == null) {
            throw new ValidationException("Tipe transaksi harus dipilih");
        }
        if (createdAt == null) {
            throw new ValidationException("Waktu transaksi tidak boleh kosong");
        }

        this.id = id;
        this.userId = userId;
        this.description = description.trim();
        this.amount = amount;
        this.type = type;
        this.proofPath = proofPath == null || proofPath.isBlank()
                ? null
                : proofPath.trim();
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public String getDescription() {
        return description;
    }

    public long getAmount() {
        return amount;
    }

    public TransactionType getType() {
        return type;
    }

    public String getProofPath() {
        return proofPath;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}