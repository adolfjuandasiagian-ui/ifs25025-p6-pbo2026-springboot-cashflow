package org.delcom.ifs25025springbootcashflow.usecase;

import java.util.List;

import org.delcom.ifs25025springbootcashflow.domain.entity.AiGeneratedTransaction;
import org.delcom.ifs25025springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25025springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25025springbootcashflow.domain.repository.IFinanceAiService;

public class FinanceAiUseCase {
    private final IFinanceAiService financeAiService;

    public FinanceAiUseCase(IFinanceAiService financeAiService) {
        this.financeAiService = financeAiService;
    }

    public List<AiGeneratedTransaction> generateTransactions(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            throw new ValidationException("Prompt tidak boleh kosong");
        }

        List<AiGeneratedTransaction> transactions =
                financeAiService.generateTransactions(prompt.trim());

        if (transactions == null || transactions.isEmpty()) {
            throw new DomainException("AI tidak menghasilkan transaksi");
        }

        return List.copyOf(transactions);
    }
}