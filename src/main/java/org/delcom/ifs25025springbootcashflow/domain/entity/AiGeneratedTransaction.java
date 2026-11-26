package org.delcom.ifs25025springbootcashflow.domain.entity;

import org.delcom.ifs25025springbootcashflow.domain.exception.ValidationException;

public record AiGeneratedTransaction(
        String description,
        long amount,
        TransactionType type) {

    public AiGeneratedTransaction {
        if (description == null || description.isBlank()) {
            throw new ValidationException("Keterangan transaksi tidak boleh kosong");
        }
        if (amount <= 0) {
            throw new ValidationException("Jumlah transaksi harus lebih dari nol");
        }
        if (type == null) {
            throw new ValidationException("Tipe transaksi harus dipilih");
        }

        description = description.trim();
    }
}