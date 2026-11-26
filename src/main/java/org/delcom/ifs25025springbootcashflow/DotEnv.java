package org.delcom.ifs25025springbootcashflow;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class DotEnv {

    private DotEnv() {
    }

    public static void load() {
        Path envFile = Path.of(".env");
        if (Files.notExists(envFile)) {
            return;
        }

        try {
            int lineNumber = 0;
            for (String line : Files.readAllLines(envFile)) {
                lineNumber++;
                String entry = line.trim();

                if (entry.isEmpty() || entry.startsWith("#")) {
                    continue;
                }

                int separator = entry.indexOf('=');
                if (separator <= 0) {
                    throw new IllegalArgumentException(
                            "Format .env tidak valid pada baris " + lineNumber
                                    + "; gunakan KEY=VALUE");
                }

                String key = entry.substring(0, separator).trim();
                String value = entry.substring(separator + 1).trim();

                if (key.isEmpty()) {
                    throw new IllegalArgumentException(
                            "Nama variabel kosong pada baris " + lineNumber + " di .env");
                }

                if (value.length() >= 2
                        && ((value.startsWith("\"") && value.endsWith("\""))
                        || (value.startsWith("'") && value.endsWith("'")))) {
                    value = value.substring(1, value.length() - 1);
                }

                if (System.getProperty(key) == null && System.getenv(key) == null) {
                    System.setProperty(key, value);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Tidak dapat membaca file .env", e);
        }
    }
}