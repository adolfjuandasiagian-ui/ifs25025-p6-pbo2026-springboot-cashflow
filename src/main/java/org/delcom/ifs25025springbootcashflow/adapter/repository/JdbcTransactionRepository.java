package org.delcom.ifs25025springbootcashflow.adapter.repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import org.delcom.ifs25025springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25025springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25025springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25025springbootcashflow.domain.repository.ITransactionRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

public class JdbcTransactionRepository implements ITransactionRepository {
    private static final String SELECT_COLUMNS = """
            SELECT id, user_id, description, amount, type, proof_path, created_at
            FROM transactions
            """;

    private static final RowMapper<Transaction> ROW_MAPPER = JdbcTransactionRepository::mapRow;

    private final JdbcTemplate jdbcTemplate;

    public JdbcTransactionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Transaction save(Transaction transaction) {
        if (transaction.getId() > 0) {
            int updated = jdbcTemplate.update(
                    """
                    UPDATE transactions
                    SET user_id = ?, description = ?, amount = ?, type = ?,
                        proof_path = ?, created_at = ?
                    WHERE id = ?
                    """,
                    transaction.getUserId(),
                    transaction.getDescription(),
                    transaction.getAmount(),
                    transaction.getType().name(),
                    transaction.getProofPath(),
                    Timestamp.valueOf(transaction.getCreatedAt()),
                    transaction.getId());

            if (updated == 0) {
                throw new EntityNotFoundException("Transaksi tidak ditemukan");
            }
            return transaction;
        }

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    """
                    INSERT INTO transactions
                        (user_id, description, amount, type, proof_path, created_at)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """,
                    Statement.RETURN_GENERATED_KEYS);
            statement.setInt(1, transaction.getUserId());
            statement.setString(2, transaction.getDescription());
            statement.setLong(3, transaction.getAmount());
            statement.setString(4, transaction.getType().name());
            statement.setString(5, transaction.getProofPath());
            statement.setTimestamp(6, Timestamp.valueOf(transaction.getCreatedAt()));
            return statement;
        }, keyHolder);

        Number generatedId = keyHolder.getKey();
        if (generatedId == null) {
            throw new IllegalStateException("Database tidak mengembalikan ID transaksi");
        }

        return new Transaction(
                generatedId.intValue(),
                transaction.getUserId(),
                transaction.getDescription(),
                transaction.getAmount(),
                transaction.getType(),
                transaction.getProofPath(),
                transaction.getCreatedAt());
    }

    @Override
    public Optional<Transaction> findById(Integer id) {
        return jdbcTemplate.query(SELECT_COLUMNS + " WHERE id = ?", ROW_MAPPER, id)
                .stream()
                .findFirst();
    }

    @Override
    public List<Transaction> findAll() {
        return jdbcTemplate.query(
                SELECT_COLUMNS + " ORDER BY created_at DESC, id DESC",
                ROW_MAPPER);
    }

    @Override
    public void deleteById(Integer id) {
        int deleted = jdbcTemplate.update("DELETE FROM transactions WHERE id = ?", id);
        if (deleted == 0) {
            throw new EntityNotFoundException("Transaksi tidak ditemukan");
        }
    }

    @Override
    public List<Transaction> findByUserId(int userId) {
        return jdbcTemplate.query(
                SELECT_COLUMNS + " WHERE user_id = ? ORDER BY created_at DESC, id DESC",
                ROW_MAPPER,
                userId);
    }

    @Override
    public Optional<Transaction> findByIdAndUserId(int transactionId, int userId) {
        return jdbcTemplate.query(
                        SELECT_COLUMNS + " WHERE id = ? AND user_id = ?",
                        ROW_MAPPER,
                        transactionId,
                        userId)
                .stream()
                .findFirst();
    }

    @Override
    public void deleteByIdAndUserId(int transactionId, int userId) {
        int deleted = jdbcTemplate.update(
                "DELETE FROM transactions WHERE id = ? AND user_id = ?",
                transactionId,
                userId);
        if (deleted == 0) {
            throw new EntityNotFoundException("Transaksi tidak ditemukan");
        }
    }

    private static Transaction mapRow(ResultSet resultSet, int rowNumber) throws SQLException {
        return new Transaction(
                resultSet.getInt("id"),
                resultSet.getInt("user_id"),
                resultSet.getString("description"),
                resultSet.getLong("amount"),
                TransactionType.valueOf(resultSet.getString("type")),
                resultSet.getString("proof_path"),
                resultSet.getTimestamp("created_at").toLocalDateTime());
    }
}