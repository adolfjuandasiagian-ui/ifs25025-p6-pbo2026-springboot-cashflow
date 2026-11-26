package org.delcom.ifs25025springbootcashflow.domain.repository;

import java.util.List;
import java.util.Optional;

import org.delcom.ifs25025springbootcashflow.domain.entity.Transaction;

public interface ITransactionRepository extends IRepository<Transaction, Integer> {
    List<Transaction> findByUserId(int userId);

    Optional<Transaction> findByIdAndUserId(int transactionId, int userId);

    void deleteByIdAndUserId(int transactionId, int userId);
}