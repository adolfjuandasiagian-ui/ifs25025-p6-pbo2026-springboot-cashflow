package org.delcom.ifs25025springbootcashflow.adapter.presenter;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import org.delcom.ifs25025springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25025springbootcashflow.domain.entity.TransactionType;

public class FinancePresenter {

    public String formatMoney(long amount) {
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(Locale.US);
        DecimalFormat formatter = new DecimalFormat("'Rp' #,##0.00", symbols);
        return formatter.format(amount);
    }

    public String formatTransaction(Transaction transaction) {
        String typeLabel = transaction.getType() == TransactionType.INCOME
                ? "Pemasukan"
                : "Pengeluaran";

        return "[" + transaction.getId() + "] "
                + transaction.getDescription()
                + " | " + formatMoney(transaction.getAmount())
                + " | [" + typeLabel + "]";
    }

    public String formatBalance(long balance) {
        return "Saldo: " + formatMoney(balance);
    }
}