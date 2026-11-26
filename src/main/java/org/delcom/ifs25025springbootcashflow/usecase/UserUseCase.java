package org.delcom.ifs25025springbootcashflow.usecase;

import java.util.List;

import org.delcom.ifs25025springbootcashflow.domain.PasswordHasher;
import org.delcom.ifs25025springbootcashflow.domain.entity.Role;
import org.delcom.ifs25025springbootcashflow.domain.entity.User;
import org.delcom.ifs25025springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25025springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25025springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25025springbootcashflow.domain.repository.IUserRepository;

public class UserUseCase {
    private final IUserRepository userRepository;
    private final PasswordHasher passwordHasher;

    public UserUseCase(IUserRepository userRepository, PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    public User getProfile(int userId) {
        return findUser(userId);
    }

    public User updateProfile(
            int userId,
            String name,
            String newPassword,
            String profilePhotoPath) {
        User currentUser = findUser(userId);

        if (name == null || name.isBlank()) {
            throw new ValidationException("Nama tidak boleh kosong");
        }

        String passwordHash = newPassword == null || newPassword.isBlank()
                ? currentUser.getPasswordHash()
                : passwordHasher.hash(newPassword);

        String photoPath = profilePhotoPath == null || profilePhotoPath.isBlank()
                ? currentUser.getProfilePhotoPath()
                : profilePhotoPath.trim();

        User updatedUser = new User(
                currentUser.getId(),
                currentUser.getUsername(),
                name.trim(),
                passwordHash,
                photoPath,
                currentUser.getRole());

        return userRepository.save(updatedUser);
    }

    public List<User> listUsers(int requesterId) {
        User requester = findUser(requesterId);
        if (requester.getRole() != Role.ADMIN) {
            throw new DomainException("Hanya admin yang dapat melihat daftar pengguna");
        }

        return userRepository.findAll();
    }

    private User findUser(int userId) {
        if (userId <= 0) {
            throw new ValidationException("ID pengguna tidak valid");
        }

        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Pengguna tidak ditemukan"));
    }
}