package org.delcom.ifs25025springbootcashflow.adapter.storage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.UUID;

public class FileStorage {
    private final Path uploadDirectory;

    public FileStorage(String uploadDirectory) {
        if (uploadDirectory == null || uploadDirectory.isBlank()) {
            throw new IllegalArgumentException("Direktori upload tidak boleh kosong");
        }
        this.uploadDirectory = Path.of(uploadDirectory).toAbsolutePath().normalize();
    }

    public String store(String originalFilename, InputStream content) throws IOException {
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new IllegalArgumentException("Nama file tidak boleh kosong");
        }
        if (content == null) {
            throw new IllegalArgumentException("Konten file tidak boleh kosong");
        }

        String extension = getAllowedExtension(originalFilename);
        Files.createDirectories(uploadDirectory);

        String storedFilename = UUID.randomUUID() + extension;
        Path destination = uploadDirectory.resolve(storedFilename).normalize();
        if (!destination.startsWith(uploadDirectory)) {
            throw new IllegalArgumentException("Lokasi file tidak valid");
        }

        Files.copy(content, destination);
        return storedFilename;
    }

    public void delete(String storedFilename) throws IOException {
        if (storedFilename == null || storedFilename.isBlank()) {
            return;
        }
        if (storedFilename.contains("/") || storedFilename.contains("\\")) {
            throw new IllegalArgumentException("Nama file tersimpan tidak valid");
        }

        Path file = uploadDirectory.resolve(storedFilename).normalize();
        if (!file.startsWith(uploadDirectory) || file.equals(uploadDirectory)) {
            throw new IllegalArgumentException("Lokasi file tidak valid");
        }

        Files.deleteIfExists(file);
    }

    public Path getUploadDirectory() {
        return uploadDirectory;
    }

    private String getAllowedExtension(String filename) {
        int extensionIndex = filename.lastIndexOf('.');
        if (extensionIndex < 0) {
            throw new IllegalArgumentException("File harus berupa gambar");
        }

        String extension = filename.substring(extensionIndex).toLowerCase(Locale.ROOT);
        if (!extension.matches("\\.(jpg|jpeg|png|gif|webp)")) {
            throw new IllegalArgumentException("Format gambar tidak didukung");
        }

        return extension;
    }
}