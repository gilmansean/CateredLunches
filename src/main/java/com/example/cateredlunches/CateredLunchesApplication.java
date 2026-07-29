package com.example.cateredlunches;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The main application class for the CateredLunches application.
 * This class is annotated with {@code @SpringBootApplication}, which
 * serves as the primary entry point for starting the Spring Boot application.
 * <p>
 * The {@code main} method is responsible for launching the application using
 * the {@link SpringApplication#run(Class, String...)} method.
 */
@SpringBootApplication
public class CateredLunchesApplication {

    public static void main(String[] args) {
        SpringApplication.run(CateredLunchesApplication.class, args);
    }

}
