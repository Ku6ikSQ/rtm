package com.ttkhnvv.rtm.repository;

import com.ttkhnvv.rtm.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    User findUserByUsername(String username);
}
