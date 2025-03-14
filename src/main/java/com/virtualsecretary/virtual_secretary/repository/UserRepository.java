package com.virtualsecretary.virtual_secretary.repository;

import com.virtualsecretary.virtual_secretary.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    boolean existsByEmployeeCode(String employeeCode);
    Optional<User> findByEmployeeCode(String employeeCode);
    boolean existsByEmailAndIdNot(String email, long id);
    boolean existsByEmployeeCodeAndIdNot(String employeeCode, long id);

}
