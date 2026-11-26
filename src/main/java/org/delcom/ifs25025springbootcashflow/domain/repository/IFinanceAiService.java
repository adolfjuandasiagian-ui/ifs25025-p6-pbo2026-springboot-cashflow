package org.delcom.ifs25025springbootcashflow.domain.repository;

import java.util.List;

import org.delcom.ifs25025springbootcashflow.domain.entity.AiGeneratedTransaction;

public interface IFinanceAiService {
    List<AiGeneratedTransaction> generateTransactions(String prompt);
}