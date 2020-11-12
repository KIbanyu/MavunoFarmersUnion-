package com.mavuno.famers.union.mavuno.repository;

import com.mavuno.famers.union.mavuno.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    UserEntity findByEmail(String email);
    UserEntity findById(long id);

    Optional<UserEntity> findDistinctByEmail(String email);

    Optional<UserEntity> findDistinctByEmailAndPassword(String email, String password);

}
