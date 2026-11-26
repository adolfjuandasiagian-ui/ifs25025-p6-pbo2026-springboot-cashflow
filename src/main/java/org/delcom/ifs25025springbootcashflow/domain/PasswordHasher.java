package org.delcom.ifs25025springbootcashflow.domain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import org.delcom.ifs25025springbootcashflow.domain.exception.ValidationException;

public final class PasswordHasher {

    public String hash(String password) {
        if (password == null || password.isBlank()) {
            throw new ValidationException("Password tidak boleh kosong");
        }

        return HexFormat.of().formatHex(digest(password));
    }

    public boolean matches(String password, String passwordHash) {
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new ValidationException("Hash password tidak boleh kosong");
        }

        byte[] candidateHash = hash(password).getBytes(StandardCharsets.US_ASCII);
        byte[] storedHash = passwordHash.getBytes(StandardCharsets.US_ASCII);
        return MessageDigest.isEqual(candidateHash, storedHash);
    }

    private byte[] digest(String password) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            return messageDigest.digest(password.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 tidak tersedia", e);
        }
    }
}