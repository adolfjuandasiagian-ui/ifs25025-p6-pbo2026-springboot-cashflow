package org.delcom.ifs25025springbootcashflow.usecase;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import org.delcom.ifs25025springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25025springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25025springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25025springbootcashflow.domain.entity.TransactionTypeFilter;
import org.delcom.ifs25025springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25025springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25025springbootcashflow.domain.repository.ITransactionRepository;

public class FinanceUseCase {
    private final ITransactionRepository transactionRepository;

    public FinanceUseCase(ITransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public List<Transaction> listTransactions(int userId) {
        return listTransactions(
                userId, "", TransactionTypeFilter.ALL, SortOption.NEWEST_FIRST);
    }

    public List<Transaction> listTransactions(
            int userId,
            String search,
            TransactionTypeFilter filter,
            SortOption sortOption) {
        validateUserId(userId);
        if (filter == null || sortOption == null) {
            throw new ValidationException("Filter dan urutan transaksi wajib dipilih");
        }

        String searchTerm = search == null ? "" : search.trim().toLowerCase(Locale.ROOT);

        return transactionRepository.findByUserId(userId).stream()
                .filter(filter::matches)
                .filter(transaction -> searchTerm.isEmpty()
                        || transaction.getDescription()
                                .toLowerCase(Locale.ROOT)
                                .contains(searchTerm))
                .sorted(sortOption.comparator())
                .toList();
    }

    public Transaction createTransaction(
            int userId,
            String description,
            long amount,
            TransactionType type,
            String proofPath) {
        validateUserId(userId);

        Transaction transaction = new Transaction(
                0,
                userId,
                description,
                amount,
                type,
                proofPath,
                LocalDateTime.now());

        return transactionRepository.save(transaction);
    }

    public void deleteTransaction(int transactionId, int userId) {
        validateUserId(userId);
        if (transactionId <= 0) {
            throw new ValidationException("ID transaksi tidak valid");
        }

        transactionRepository.findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Transaksi tidak ditemukan"));
        transactionRepository.deleteByIdAndUserId(transactionId, userId);
    }

    public long getTotalIncome(int userId) {
        return getTotalByType(userId, TransactionType.INCOME);
    }

    public long getTotalExpense(int userId) {
        return getTotalByType(userId, TransactionType.EXPENSE);
    }

    public long getBalance(int userId) {
        return Math.subtractExact(getTotalIncome(userId), getTotalExpense(userId));
    }

    public long getTransactionCount(int userId) {
        return getTransactionsForUser(userId).size();
    }

    private long getTotalByType(int userId, TransactionType type) {
        return getTransactionsForUser(userId).stream()
                .filter(transaction -> transaction.getType() == type)
                .map(Transaction::getAmount)
                .reduce(0L, (total, amount) -> Math.addExact(total, amount));
    }

    private List<Transaction> getTransactionsForUser(int userId) {
        validateUserId(userId);
        return transactionRepository.findByUserId(userId);
    }

    private void validateUserId(int userId) {
        if (userId <= 0) {
            throw new ValidationException("ID pengguna tidak valid");
        }
    }
}