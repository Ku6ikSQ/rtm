package com.ttkhnvv.rtm.repository;

import com.ttkhnvv.rtm.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findUserByUsername(String username);
}
