package org.delcom.ifs25025springbootcashflow.framework.view;

import org.delcom.ifs25025springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25025springbootcashflow.framework.view.auth.LoginView;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;

public class MainLayout extends AppLayout implements BeforeEnterObserver {
    private final AuthSession authSession;

    public MainLayout(AuthSession authSession) {
        this.authSession = authSession;

        Button logoutButton = new Button("Keluar", event -> {
            authSession.logout();
            UI.getCurrent().navigate("login");
        });

        addToNavbar(new H1("Cashflow"), logoutButton);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (authSession.currentUser().isEmpty()) {
            event.rerouteTo(LoginView.class);
        }
    }
}