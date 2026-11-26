package org.delcom.ifs25025springbootcashflow.adapter.repository;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.delcom.ifs25025springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25025springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25025springbootcashflow.domain.repository.ITransactionRepository;

public class InMemoryTransactionRepository implements ITransactionRepository {
    private final Map<Integer, Transaction> transactions = new HashMap<>();
    private int nextId;

    @Override
    public synchronized Transaction save(Transaction transaction) {
        int id = transaction.getId();

        if (id == 0) {
            id = ++nextId;
        } else if (!transactions.containsKey(id)) {
            throw new EntityNotFoundException("Transaksi tidak ditemukan");
        } else {
            nextId = Math.max(nextId, id);
        }

        Transaction saved = new Transaction(
                id,
                transaction.getUserId(),
                transaction.getDescription(),
                transaction.getAmount(),
                transaction.getType(),
                transaction.getProofPath(),
                transaction.getCreatedAt());

        transactions.put(id, saved);
        return saved;
    }

    @Override
    public synchronized Optional<Transaction> findById(Integer id) {
        return Optional.ofNullable(transactions.get(id));
    }

    @Override
    public synchronized List<Transaction> findAll() {
        return transactions.values().stream()
                .sorted(newestFirst())
                .toList();
    }

    @Override
    public synchronized void deleteById(Integer id) {
        if (transactions.remove(id) == null) {
            throw new EntityNotFoundException("Transaksi tidak ditemukan");
        }
    }

    @Override
    public synchronized List<Transaction> findByUserId(int userId) {
        return transactions.values().stream()
                .filter(transaction -> transaction.getUserId() == userId)
                .sorted(newestFirst())
                .toList();
    }

    @Override
    public synchronized Optional<Transaction> findByIdAndUserId(
            int transactionId,
            int userId) {
        Transaction transaction = transactions.get(transactionId);
        if (transaction == null || transaction.getUserId() != userId) {
            return Optional.empty();
        }
        return Optional.of(transaction);
    }

    @Override
    public synchronized void deleteByIdAndUserId(int transactionId, int userId) {
        Transaction transaction = transactions.get(transactionId);
        if (transaction == null || transaction.getUserId() != userId) {
            throw new EntityNotFoundException("Transaksi tidak ditemukan");
        }
        transactions.remove(transactionId);
    }

    private Comparator<Transaction> newestFirst() {
        return Comparator.comparing(Transaction::getCreatedAt)
                .thenComparingInt(Transaction::getId)
                .reversed();
    }
}