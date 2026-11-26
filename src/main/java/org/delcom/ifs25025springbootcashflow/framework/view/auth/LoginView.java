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

@Route("login")
@PageTitle("Masuk | Cashflow")
public class LoginView extends VerticalLayout {
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

        PasswordField passwordField = new PasswordField("Password");
        passwordField.setRequired(true);

        Button loginButton = new Button("Masuk");
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

    private void login(String username, String password) {
        try {
            authSession.login(authUseCase.login(username, password));
            UI.getCurrent().navigate("cashflow");
        } catch (DomainException exception) {
            Notification.show(exception.getMessage());
        }
    }
}