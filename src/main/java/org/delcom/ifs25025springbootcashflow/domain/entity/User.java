package org.delcom.ifs25025springbootcashflow.domain.entity;

import org.delcom.ifs25025springbootcashflow.domain.exception.ValidationException;

public class User {
    private final int id;
    private final String username;
    private final String name;
    private final String passwordHash;
    private final String profilePhotoPath;
    private final Role role;

    public User(
            int id,
            String username,
            String name,
            String passwordHash,
            String profilePhotoPath,
            Role role) {
        if (id < 0) {
            throw new ValidationException("ID pengguna tidak valid");
        }
        if (username == null || username.isBlank()) {
            throw new ValidationException("Username tidak boleh kosong");
        }
        if (name == null || name.isBlank()) {
            throw new ValidationException("Nama tidak boleh kosong");
        }
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new ValidationException("Password tidak boleh kosong");
        }
        if (role == null) {
            throw new ValidationException("Role pengguna tidak boleh kosong");
        }

        this.id = id;
        this.username = username.trim();
        this.name = name.trim();
        this.passwordHash = passwordHash;
        this.profilePhotoPath = profilePhotoPath == null || profilePhotoPath.isBlank()
                ? null
                : profilePhotoPath.trim();
        this.role = role;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getName() {
        return name;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getProfilePhotoPath() {
        return profilePhotoPath;
    }

    public Role getRole() {
        return role;
    }
}