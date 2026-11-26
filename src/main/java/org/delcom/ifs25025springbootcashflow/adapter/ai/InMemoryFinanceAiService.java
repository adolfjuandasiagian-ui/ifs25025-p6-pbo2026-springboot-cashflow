package org.delcom.ifs25025springbootcashflow.adapter.ai;

import java.util.List;

import org.delcom.ifs25025springbootcashflow.domain.entity.AiGeneratedTransaction;
import org.delcom.ifs25025springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25025springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25025springbootcashflow.domain.repository.IFinanceAiService;

public class InMemoryFinanceAiService implements IFinanceAiService {

    @Override
    public List<AiGeneratedTransaction> generateTransactions(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            throw new ValidationException("Prompt tidak boleh kosong");
        }

        return List.of(
                new AiGeneratedTransaction(
                        "Contoh pemasukan",
                        100_000,
                        TransactionType.INCOME),
                new AiGeneratedTransaction(
                        "Contoh pengeluaran",
                        25_000,
                        TransactionType.EXPENSE));
    }
}