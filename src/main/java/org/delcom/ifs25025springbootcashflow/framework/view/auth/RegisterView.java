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

@Route("register")
@PageTitle("Daftar | Cashflow")
public class RegisterView extends VerticalLayout implements BeforeEnterObserver {
    private final AuthUseCase authUseCase;
    private final AuthSession authSession;

    public RegisterView(
            AuthUseCase authUseCase,
            AuthSession authSession) {
        this.authUseCase = authUseCase;
        this.authSession = authSession;

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        TextField nameField = new TextField("Nama");
        nameField.setRequired(true);

        TextField usernameField = new TextField("Username");
        usernameField.setRequired(true);

        PasswordField passwordField = new PasswordField("Password");
        passwordField.setRequired(true);

        PasswordField confirmPasswordField = new PasswordField("Konfirmasi password");
        confirmPasswordField.setRequired(true);

        Button registerButton = new Button("Daftar");
        registerButton.addClickListener(event -> register(
                nameField.getValue(),
                usernameField.getValue(),
                passwordField.getValue(),
                confirmPasswordField.getValue()));

        Anchor loginLink = new Anchor("login", "Sudah punya akun? Masuk");

        add(
                new H1("Buat Akun Cashflow"),
                new Paragraph("Daftar untuk mulai mengelola keuangan."),
                nameField,
                usernameField,
                passwordField,
                confirmPasswordField,
                registerButton,
                loginLink);
    }

    private void register(
            String name,
            String username,
            String password,
            String confirmPassword) {
        if (!password.equals(confirmPassword)) {
            Notification.show("Konfirmasi password tidak cocok");
            return;
        }

        try {
            authUseCase.register(username, name, password);
            Notification.show("Akun berhasil dibuat. Silakan masuk.");
            UI.getCurrent().navigate("login");
        } catch (DomainException exception) {
            Notification.show(exception.getMessage());
        }
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (authSession.currentUser().isPresent()) {
            event.forwardTo("");
        }
    }
}