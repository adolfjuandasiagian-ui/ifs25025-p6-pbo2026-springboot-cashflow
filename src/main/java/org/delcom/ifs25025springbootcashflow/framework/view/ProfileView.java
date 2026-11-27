package org.delcom.ifs25025springbootcashflow.framework.view;

import java.io.IOException;

import org.delcom.ifs25025springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25025springbootcashflow.domain.entity.User;
import org.delcom.ifs25025springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25025springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25025springbootcashflow.framework.view.auth.LoginView;
import org.delcom.ifs25025springbootcashflow.usecase.UserUseCase;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLayout;

@Route(value = "profile", layout = MainLayout.class)
@PageTitle("Profil | Cashflow")
public class ProfileView extends VerticalLayout implements BeforeEnterObserver {
    private final AuthSession authSession;
    private final UserUseCase userUseCase;
    private final FileStorage fileStorage;
    private final TextField nameField = new TextField("Nama");
    private final PasswordField passwordField = new PasswordField("Password baru");
    private final MemoryBuffer photoBuffer = new MemoryBuffer();

    public ProfileView(
            AuthSession authSession,
            UserUseCase userUseCase,
            FileStorage fileStorage) {
        this.authSession = authSession;
        this.userUseCase = userUseCase;
        this.fileStorage = fileStorage;

        Upload photoUpload = new Upload(photoBuffer);
        photoUpload.setAcceptedFileTypes("image/jpeg", "image/png", "image/gif", "image/webp");
        photoUpload.setMaxFiles(1);

        Button save = new Button("Simpan profil", event -> save());
        add(new H1("Profil"), nameField, passwordField, photoUpload, save);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        User user = authSession.currentUser().orElse(null);
        if (user == null) {
            event.rerouteTo(LoginView.class);
            return;
        }
        nameField.setValue(user.getName());
    }

    private void save() {
        User user = authSession.currentUser().orElseThrow();
        try {
            String photoPath = user.getProfilePhotoPath();
            if (photoBuffer.getFileName() != null
                    && !photoBuffer.getFileName().isBlank()) {
                photoPath = fileStorage.store(
                        photoBuffer.getFileName(),
                        photoBuffer.getInputStream());
            }
            userUseCase.updateProfile(
                    user.getId(),
                    nameField.getValue(),
                    passwordField.getValue(),
                    photoPath);
            Notification.show("Profil berhasil diperbarui");
        } catch (IOException | DomainException exception) {
            Notification.show("Gagal memperbarui profil: " + exception.getMessage());
        }
    }
}
