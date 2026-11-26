package org.delcom.ifs25025springbootcashflow.adapter.ai;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.delcom.ifs25025springbootcashflow.domain.entity.AiGeneratedTransaction;
import org.delcom.ifs25025springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25025springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25025springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25025springbootcashflow.domain.repository.IFinanceAiService;

public class OpenAiFinanceService implements IFinanceAiService {
    private final URI endpoint;
    private final String apiKey;
    private final String model;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public OpenAiFinanceService(String endpoint, String apiKey, String model) {
        if (endpoint == null || endpoint.isBlank()
                || apiKey == null || apiKey.isBlank()
                || model == null || model.isBlank()) {
            throw new IllegalArgumentException(
                    "Endpoint, API key, dan model AI wajib diisi");
        }

        String baseUrl = endpoint.trim().replaceAll("/+$", "");
        String completionsUrl = baseUrl.endsWith("/chat/completions")
                ? baseUrl
                : baseUrl + "/chat/completions";

        this.endpoint = URI.create(completionsUrl);
        this.apiKey = apiKey.trim();
        this.model = model.trim();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public List<AiGeneratedTransaction> generateTransactions(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            throw new ValidationException("Prompt tidak boleh kosong");
        }

        HttpRequest request = HttpRequest.newBuilder(endpoint)
                .timeout(Duration.ofSeconds(60))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(createRequestBody(prompt.trim())))
                .build();

        HttpResponse<String> response;
        try {
            response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new DomainException("Permintaan ke layanan AI terhenti");
        } catch (IOException exception) {
            throw new DomainException(
                    "Gagal menghubungi layanan AI: " + exception.getMessage());
        }

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new DomainException(
                    "Layanan AI mengembalikan HTTP " + response.statusCode());
        }

        return parseTransactions(response.body());
    }

    private String createRequestBody(String prompt) {
        ObjectNode body = objectMapper.createObjectNode();
        body.put("model", model);

        ArrayNode messages = body.putArray("messages");
        messages.addObject()
                .put("role", "system")
                .put("content", """
                        Buat catatan transaksi keuangan dari prompt pengguna.
                        Balas hanya dengan JSON array. Setiap item harus memiliki
                        description (string), amount (bilangan bulat positif),
                        dan type yang bernilai INCOME atau EXPENSE.
                        Jangan sertakan Markdown atau teks di luar JSON.
                        """);
        messages.addObject()
                .put("role", "user")
                .put("content", prompt);

        try {
            return objectMapper.writeValueAsString(body);
        } catch (JsonProcessingException exception) {
            throw new DomainException("Gagal menyiapkan permintaan AI");
        }
    }

    private List<AiGeneratedTransaction> parseTransactions(String responseBody) {
        try {
            JsonNode response = objectMapper.readTree(responseBody);
            JsonNode content = response.path("choices")
                    .path(0)
                    .path("message")
                    .path("content");

            if (!content.isTextual()) {
                throw new DomainException("Respons AI tidak berisi konten transaksi");
            }

            JsonNode transactions = objectMapper.readTree(stripMarkdownFence(content.asText()));
            if (transactions == null || !transactions.isArray()) {
                throw new DomainException("Respons AI harus berupa JSON array");
            }

            List<AiGeneratedTransaction> result = new ArrayList<>();
            for (JsonNode item : transactions) {
                JsonNode description = item.path("description");
                JsonNode amount = item.path("amount");
                JsonNode type = item.path("type");

                if (!description.isTextual()
                        || !amount.isIntegralNumber()
                        || !amount.canConvertToLong()
                        || !type.isTextual()) {
                    throw new DomainException("Format transaksi dari AI tidak valid");
                }

                TransactionType transactionType;
                try {
                    transactionType = TransactionType.valueOf(
                            type.asText().trim().toUpperCase(Locale.ROOT));
                } catch (IllegalArgumentException exception) {
                    throw new DomainException("Tipe transaksi dari AI tidak valid");
                }

                result.add(new AiGeneratedTransaction(
                        description.asText(),
                        amount.longValue(),
                        transactionType));
            }

            if (result.isEmpty()) {
                throw new DomainException("AI tidak menghasilkan transaksi");
            }

            return List.copyOf(result);
        } catch (IOException exception) {
            throw new DomainException("Respons layanan AI bukan JSON yang valid");
        }
    }

    private String stripMarkdownFence(String content) {
        String trimmed = content.trim();
        if (!trimmed.startsWith("```")) {
            return trimmed;
        }

        int firstLineEnd = trimmed.indexOf('\n');
        int closingFence = trimmed.lastIndexOf("```");
        if (firstLineEnd < 0 || closingFence <= firstLineEnd) {
            throw new DomainException("Format respons AI tidak valid");
        }

        return trimmed.substring(firstLineEnd + 1, closingFence).trim();
    }
}