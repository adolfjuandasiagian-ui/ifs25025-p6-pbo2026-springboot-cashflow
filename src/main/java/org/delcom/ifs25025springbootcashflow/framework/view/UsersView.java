package org.delcom.ifs25025springbootcashflow.framework.view;

import java.util.List;

import org.delcom.ifs25025springbootcashflow.domain.entity.Role;
import org.delcom.ifs25025springbootcashflow.domain.entity.User;
import org.delcom.ifs25025springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25025springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25025springbootcashflow.framework.view.auth.LoginView;
import org.delcom.ifs25025springbootcashflow.usecase.UserUseCase;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "users", layout = MainLayout.class)
@PageTitle("Pengguna | Cashflow")
public class UsersView extends VerticalLayout implements BeforeEnterObserver {
    private final AuthSession authSession;
    private final UserUseCase userUseCase;
    private final Grid<User> grid = new Grid<>(User.class, false);

    public UsersView(AuthSession authSession, UserUseCase userUseCase) {
        this.authSession = authSession;
        this.userUseCase = userUseCase;
        grid.addColumn(User::getId).setHeader("ID").setAutoWidth(true);
        grid.addColumn(User::getUsername).setHeader("Username").setAutoWidth(true);
        grid.addColumn(User::getName).setHeader("Nama").setAutoWidth(true);
        grid.addColumn(user -> user.getRole().name()).setHeader("Role").setAutoWidth(true);
        add(new H1("Daftar Pengguna"), grid);
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
        try {
            List<User> users = userUseCase.listUsers(requester.getId());
            grid.setItems(users);
        } catch (DomainException exception) {
            Notification.show(exception.getMessage());
        }
    }
}
