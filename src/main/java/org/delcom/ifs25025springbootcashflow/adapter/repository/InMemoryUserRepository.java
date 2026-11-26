package org.delcom.ifs25025springbootcashflow.adapter.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.delcom.ifs25025springbootcashflow.domain.entity.User;
import org.delcom.ifs25025springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25025springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25025springbootcashflow.domain.repository.IUserRepository;

public class InMemoryUserRepository implements IUserRepository {
    private final Map<Integer, User> users = new HashMap<>();
    private int nextId;

    @Override
    public synchronized User save(User user) {
        int id = user.getId();

        if (id == 0) {
            id = ++nextId;
        } else if (!users.containsKey(id)) {
            throw new EntityNotFoundException("Pengguna tidak ditemukan");
        } else {
            nextId = Math.max(nextId, id);
        }

        final int targetId = id;
        boolean usernameAlreadyUsed = users.values().stream()
                .anyMatch(existingUser -> existingUser.getId() != targetId
                        && existingUser.getUsername().equals(user.getUsername()));
        if (usernameAlreadyUsed) {
            throw new ValidationException("Username sudah digunakan");
        }

        

        User saved = new User(
                id,
                user.getUsername(),
                user.getName(),
                user.getPasswordHash(),
                user.getProfilePhotoPath(),
                user.getRole());

        users.put(id, saved);
        return saved;
    }

    @Override
    public synchronized Optional<User> findById(Integer id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public synchronized List<User> findAll() {
        return users.values().stream()
                .sorted((first, second) -> Integer.compare(first.getId(), second.getId()))
                .toList();
    }

    @Override
    public synchronized void deleteById(Integer id) {
        if (users.remove(id) == null) {
            throw new EntityNotFoundException("Pengguna tidak ditemukan");
        }
    }

    @Override
    public synchronized Optional<User> findByUsername(String username) {
        return users.values().stream()
                .filter(user -> user.getUsername().equals(username))
                .findFirst();
    }
}