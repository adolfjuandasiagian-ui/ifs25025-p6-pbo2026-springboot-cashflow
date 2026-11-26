package org.delcom.ifs25025springbootcashflow.domain.repository;

import java.util.Optional;

import org.delcom.ifs25025springbootcashflow.domain.entity.User;

public interface IUserRepository extends IRepository<User, Integer> {
    Optional<User> findByUsername(String username);
}