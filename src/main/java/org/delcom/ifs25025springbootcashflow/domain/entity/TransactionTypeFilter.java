package org.delcom.ifs25025springbootcashflow.domain.entity;

public enum TransactionTypeFilter {
    ALL(null),
    INCOME(TransactionType.INCOME),
    EXPENSE(TransactionType.EXPENSE);

    private final TransactionType transactionType;

    TransactionTypeFilter(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public boolean matches(Transaction transaction) {
        return transactionType == null
                || transaction.getType() == transactionType;
    }
}