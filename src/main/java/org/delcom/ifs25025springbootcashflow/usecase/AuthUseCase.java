package org.delcom.ifs25025springbootcashflow.usecase;

import org.delcom.ifs25025springbootcashflow.domain.PasswordHasher;
import org.delcom.ifs25025springbootcashflow.domain.entity.Role;
import org.delcom.ifs25025springbootcashflow.domain.entity.User;
import org.delcom.ifs25025springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25025springbootcashflow.domain.repository.IUserRepository;

public class AuthUseCase {
    private final IUserRepository userRepository;
    private final PasswordHasher passwordHasher;

    public AuthUseCase(IUserRepository userRepository, PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    public User register(String username, String name, String password) {
        if (username == null || username.isBlank()) {
            throw new ValidationException("Username tidak boleh kosong");
        }
        if (name == null || name.isBlank()) {
            throw new ValidationException("Nama tidak boleh kosong");
        }
        if (userRepository.findByUsername(username.trim()).isPresent()) {
            throw new ValidationException("Username sudah digunakan");
        }

        User user = new User(
                0,
                username.trim(),
                name.trim(),
                passwordHasher.hash(password),
                null,
                Role.USER);
        return userRepository.save(user);
    }

    public User login(String username, String password) {
        if (username == null || username.isBlank()
                || password == null || password.isBlank()) {
            throw new ValidationException("Username dan password wajib diisi");
        }

        User user = userRepository.findByUsername(username.trim())
                .orElseThrow(() -> new ValidationException("Username atau password salah"));

        if (!passwordHasher.matches(password, user.getPasswordHash())) {
            throw new ValidationException("Username atau password salah");
        }

        return user;
    }
}