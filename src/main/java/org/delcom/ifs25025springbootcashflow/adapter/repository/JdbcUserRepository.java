package org.delcom.ifs25025springbootcashflow.adapter.repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

import org.delcom.ifs25025springbootcashflow.domain.entity.Role;
import org.delcom.ifs25025springbootcashflow.domain.entity.User;
import org.delcom.ifs25025springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25025springbootcashflow.domain.repository.IUserRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

public class JdbcUserRepository implements IUserRepository {
    private static final String SELECT_COLUMNS = """
            SELECT id, username, name, password_hash, profile_photo_path, role
            FROM users
            """;

    private static final RowMapper<User> ROW_MAPPER = JdbcUserRepository::mapRow;

    private final JdbcTemplate jdbcTemplate;

    public JdbcUserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public User save(User user) {
        if (user.getId() > 0) {
            int updated = jdbcTemplate.update(
                    """
                    UPDATE users
                    SET username = ?, name = ?, password_hash = ?,
                        profile_photo_path = ?, role = ?
                    WHERE id = ?
                    """,
                    user.getUsername(),
                    user.getName(),
                    user.getPasswordHash(),
                    user.getProfilePhotoPath(),
                    user.getRole().name(),
                    user.getId());

            if (updated == 0) {
                throw new EntityNotFoundException("Pengguna tidak ditemukan");
            }
            return user;
        }

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    """
                    INSERT INTO users (username, name, password_hash, profile_photo_path, role)
                    VALUES (?, ?, ?, ?, ?)
                    """,
                    Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getName());
            statement.setString(3, user.getPasswordHash());
            statement.setString(4, user.getProfilePhotoPath());
            statement.setString(5, user.getRole().name());
            return statement;
        }, keyHolder);

        Number generatedId = keyHolder.getKey();
        if (generatedId == null) {
            throw new IllegalStateException("Database tidak mengembalikan ID pengguna");
        }

        return new User(
                generatedId.intValue(),
                user.getUsername(),
                user.getName(),
                user.getPasswordHash(),
                user.getProfilePhotoPath(),
                user.getRole());
    }

    @Override
    public Optional<User> findById(Integer id) {
        return jdbcTemplate.query(SELECT_COLUMNS + " WHERE id = ?", ROW_MAPPER, id)
                .stream()
                .findFirst();
    }

    @Override
    public List<User> findAll() {
        return jdbcTemplate.query(SELECT_COLUMNS + " ORDER BY id", ROW_MAPPER);
    }

    @Override
    public void deleteById(Integer id) {
        int deleted = jdbcTemplate.update("DELETE FROM users WHERE id = ?", id);
        if (deleted == 0) {
            throw new EntityNotFoundException("Pengguna tidak ditemukan");
        }
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return jdbcTemplate.query(
                        SELECT_COLUMNS + " WHERE username = ?",
                        ROW_MAPPER,
                        username)
                .stream()
                .findFirst();
    }

    private static User mapRow(ResultSet resultSet, int rowNumber) throws SQLException {
        return new User(
                resultSet.getInt("id"),
                resultSet.getString("username"),
                resultSet.getString("name"),
                resultSet.getString("password_hash"),
                resultSet.getString("profile_photo_path"),
                Role.valueOf(resultSet.getString("role")));
    }
}