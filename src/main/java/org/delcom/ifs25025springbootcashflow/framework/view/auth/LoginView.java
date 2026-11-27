package org.delcom.ifs25025springbootcashflow.framework.view.auth;

import org.delcom.ifs25025springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25025springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25025springbootcashflow.usecase.AuthUseCase;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;

@Route("login")
@PageTitle("Masuk | Cashflow")
public class LoginView extends VerticalLayout implements BeforeEnterObserver {
    private final AuthUseCase authUseCase;
    private final AuthSession authSession;

    public LoginView(AuthUseCase authUseCase, AuthSession authSession) {
        this.authUseCase = authUseCase;
        this.authSession = authSession;

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        TextField usernameField = new TextField("Username");
        usernameField.setRequired(true);
        usernameField.setId("login-username");

        PasswordField passwordField = new PasswordField("Password");
        passwordField.setRequired(true);
        passwordField.setId("login-password");

        Button loginButton = new Button("Masuk");
        loginButton.setId("login-submit");
        loginButton.addClickListener(event ->
                login(usernameField.getValue(), passwordField.getValue()));

        Anchor registerLink = new Anchor("register", "Belum punya akun? Daftar");

        add(
                new H1("Cashflow"),
                new Paragraph("Masuk untuk mengelola keuangan Anda."),
                usernameField,
                passwordField,
                loginButton,
                registerLink);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (authSession.currentUser().isPresent()) {
            event.forwardTo("");
        }
    }

    private void login(String username, String password) {
        try {
            authSession.login(authUseCase.login(username, password));
            UI.getCurrent().navigate("");
        } catch (DomainException exception) {
            Notification.show(exception.getMessage());
        }
    }
}