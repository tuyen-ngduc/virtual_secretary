package com.virtualsecretary.virtual_secretary.config;

import com.virtualsecretary.virtual_secretary.entity.User;
import com.virtualsecretary.virtual_secretary.enums.Degree;
import com.virtualsecretary.virtual_secretary.enums.Role;
import com.virtualsecretary.virtual_secretary.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;


@Slf4j
@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ApplicationInitConfig {
    PasswordEncoder passwordEncoder;

    @Bean
    ApplicationRunner applicationRunner(UserRepository userRepository) {
        return args -> {
            if (userRepository.findByEmployeeCode("admin").isEmpty()) {
                User user = User.builder()
                        .employeeCode("admin")
                        .name("tuyenngduc")
                        .dob(LocalDate.of(2003, 2, 12))
                        .degree(Degree.OTHER)
                        .password(passwordEncoder.encode("admin"))
                        .email("admin@gmail.com")
                        .role(Role.ADMIN)
                        .build();
                userRepository.save(user);
            }
        };
    }
}
