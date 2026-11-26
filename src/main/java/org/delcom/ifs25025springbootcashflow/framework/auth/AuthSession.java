package org.delcom.ifs25025springbootcashflow.framework.auth;

import java.util.Optional;

import org.delcom.ifs25025springbootcashflow.domain.entity.User;
import org.delcom.ifs25025springbootcashflow.domain.repository.IUserRepository;
import com.vaadin.flow.server.VaadinSession;

public class AuthSession {
    public static final String LOGGED_OUT_KEY = "cashflow.loggedOut";
    private static final String USER_ID_KEY = "cashflow.userId";

    private final IUserRepository userRepository;

    public AuthSession(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void login(User user) {
        if (user == null || user.getId() <= 0) {
            throw new IllegalArgumentException("Pengguna login tidak valid");
        }

        VaadinSession session = VaadinSession.getCurrent();
        if (session == null) {
            throw new IllegalStateException("Sesi Vaadin tidak tersedia");
        }

        session.setAttribute(USER_ID_KEY, user.getId());
        session.setAttribute(LOGGED_OUT_KEY, Boolean.FALSE);
    }

    public Optional<User> currentUser() {
        VaadinSession session = VaadinSession.getCurrent();
        if (session == null
                || Boolean.TRUE.equals(session.getAttribute(LOGGED_OUT_KEY))) {
            return Optional.empty();
        }

        Object userId = session.getAttribute(USER_ID_KEY);
        if (!(userId instanceof Integer id)) {
            return Optional.empty();
        }

        return userRepository.findById(id);
    }

    public void logout() {
        VaadinSession session = VaadinSession.getCurrent();
        if (session == null) {
            return;
        }

        session.setAttribute(USER_ID_KEY, null);
        session.setAttribute(LOGGED_OUT_KEY, Boolean.TRUE);
    }
}