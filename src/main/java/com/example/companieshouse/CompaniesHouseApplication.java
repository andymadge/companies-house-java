package com.example.companieshouse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Spring Boot application entry point for the Companies House API client.
 * <p>
 * This is a client library designed to be imported into other projects.
 * Running this application directly starts a minimal Spring context for testing.
 * </p>
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class CompaniesHouseApplication {

    public static void main(String[] args) {
        SpringApplication.run(CompaniesHouseApplication.class, args);
    }
}
