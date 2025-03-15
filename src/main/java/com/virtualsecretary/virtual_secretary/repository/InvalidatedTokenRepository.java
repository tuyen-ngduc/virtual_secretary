package com.virtualsecretary.virtual_secretary.repository;

import com.virtualsecretary.virtual_secretary.entity.InvalidatedToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvalidatedTokenRepository extends JpaRepository<InvalidatedToken, String> {

}
