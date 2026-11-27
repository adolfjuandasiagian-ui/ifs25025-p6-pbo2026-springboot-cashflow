package org.delcom.ifs25025springbootcashflow.framework.view;

import org.delcom.ifs25025springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25025springbootcashflow.domain.entity.Role;
import org.delcom.ifs25025springbootcashflow.domain.entity.User;
import org.delcom.ifs25025springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25025springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25025springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25025springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25025springbootcashflow.framework.view.auth.LoginView;
import org.delcom.ifs25025springbootcashflow.usecase.FinanceUseCase;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "user-transactions", layout = MainLayout.class)
@PageTitle("Transaksi Pengguna | Cashflow")
public class UserTransactionsView extends VerticalLayout implements BeforeEnterObserver {
    private final AuthSession authSession;
    private final IUserRepository userRepository;
    private final FinanceUseCase financeUseCase;
    private final ComboBox<User> userFilter = new ComboBox<>("Pengguna");
    private final Grid<Transaction> grid = new Grid<>(Transaction.class, false);

    public UserTransactionsView(
            AuthSession authSession,
            IUserRepository userRepository,
            FinanceUseCase financeUseCase,
            FinancePresenter presenter) {
        this.authSession = authSession;
        this.userRepository = userRepository;
        this.financeUseCase = financeUseCase;
        userFilter.setItemLabelGenerator(User::getName);
        userFilter.addValueChangeListener(event -> refresh());
        grid.addColumn(Transaction::getId).setHeader("ID").setAutoWidth(true);
        grid.addColumn(Transaction::getDescription).setHeader("Keterangan").setFlexGrow(1);
        grid.addColumn(t -> presenter.formatMoney(t.getAmount())).setHeader("Jumlah");
        grid.addColumn(t -> t.getType() == TransactionType.INCOME
                ? "Pemasukan" : "Pengeluaran").setHeader("Jenis");
        add(new H1("Transaksi Pengguna (Read-only)"), userFilter, grid);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        User requester = authSession.currentUser().orElse(null);
        if (requester == null) {
            event.rerouteTo(LoginView.class);
            return;
        }
        if (requester.getRole() != Role.ADMIN) {
            event.forwardTo("");
            return;
        }
        userFilter.setItems(userRepository.findAll());
    }

    private void refresh() {
        if (userFilter.getValue() != null) {
            grid.setItems(financeUseCase.listTransactions(userFilter.getValue().getId()));
        }
    }
}
