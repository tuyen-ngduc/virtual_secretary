package com.virtualsecretary.virtual_secretary;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class VirtualSecretaryApplication {

	public static void main(String[] args) {
//		Dotenv dotenv = Dotenv.configure()
//				.directory(".")
//				.filename("SYSTEM32.env")
//				.load();
//		System.setProperty("spring.datasource.url", dotenv.get("SPRING_DATASOURCE_URL", "jdbc:mysql://42.112.213.93:3306/virtual_secretary"));
//		System.setProperty("spring.datasource.username", dotenv.get("SPRING_DATASOURCE_USERNAME", "test"));
//		System.setProperty("spring.datasource.password", dotenv.get("SPRING_DATASOURCE_PASSWORD", "Abc123@!"));
//		System.setProperty("cors.allowed.origins", dotenv.get("CORS_ALLOWED_ORIGINS"));



		SpringApplication.run(VirtualSecretaryApplication.class, args);
	}

}
