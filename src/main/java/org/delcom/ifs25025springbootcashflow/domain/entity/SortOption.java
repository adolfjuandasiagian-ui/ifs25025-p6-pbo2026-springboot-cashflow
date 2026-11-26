package org.delcom.ifs25025springbootcashflow.domain.entity;

import java.util.Comparator;

public enum SortOption {
    NEWEST_FIRST(
            "Terbaru",
            Comparator.comparing(Transaction::getCreatedAt)
                    .thenComparingInt(Transaction::getId)
                    .reversed()),
    OLDEST_FIRST(
            "Terlama",
            Comparator.comparing(Transaction::getCreatedAt)
                    .thenComparingInt(Transaction::getId)),
    AMOUNT_ASCENDING(
            "Jumlah terkecil",
            Comparator.comparingLong(Transaction::getAmount)),
    AMOUNT_DESCENDING(
            "Jumlah terbesar",
            Comparator.comparingLong(Transaction::getAmount).reversed());

    private final String label;
    private final Comparator<Transaction> comparator;

    SortOption(String label, Comparator<Transaction> comparator) {
        this.label = label;
        this.comparator = comparator;
    }

    public String label() {
        return label;
    }

    public Comparator<Transaction> comparator() {
        return comparator;
    }
}