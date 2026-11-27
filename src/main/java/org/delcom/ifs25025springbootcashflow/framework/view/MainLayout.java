package org.delcom.ifs25025springbootcashflow.framework.view;

import org.delcom.ifs25025springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25025springbootcashflow.framework.view.auth.LoginView;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.RouterLink;

import org.delcom.ifs25025springbootcashflow.domain.entity.Role;

public class MainLayout extends AppLayout implements BeforeEnterObserver {
    private final AuthSession authSession;

    public MainLayout(AuthSession authSession) {
        this.authSession = authSession;

        Button logoutButton = new Button(VaadinIcon.SIGN_OUT.create(), event -> {
            authSession.logout();
            UI.getCurrent().navigate("login");
        });
        logoutButton.setText("Keluar");
        logoutButton.addClassName("cursor-pointer");

        HorizontalLayout nav = new HorizontalLayout(
                new RouterLink("Ringkasan", FinanceView.class),
                new RouterLink("Profil", ProfileView.class));
        nav.setDefaultVerticalComponentAlignment(HorizontalLayout.Alignment.CENTER);

        authSession.currentUser().ifPresent(user -> {
            if (user.getRole() == Role.ADMIN) {
                nav.add(
                        new RouterLink("Pengguna", UsersView.class),
                        new RouterLink("Transaksi pengguna", UserTransactionsView.class));
            }
        });

        addToNavbar(new H1("Cashflow"));
        addToNavbar(nav);
        addToNavbar(logoutButton);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (authSession.currentUser().isEmpty()) {
            event.rerouteTo(LoginView.class);
        }
    }
}