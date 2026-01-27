package com.example.companieshouse.config;

import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for Companies House API client.
 * <p>
 * Binds to properties with prefix "companies-house.api" from application.yml.
 * All fields are validated at startup to ensure proper configuration.
 * </p>
 *
 * <p>Example configuration:</p>
 * <pre>
 * companies-house:
 *   api:
 *     base-url: https://api.company-information.service.gov.uk
 *     api-key: ${COMPANIES_HOUSE_API_KEY}
 *     connect-timeout-ms: 5000
 *     read-timeout-ms: 10000
 * </pre>
 */
@Component
@ConfigurationProperties(prefix = "companies-house.api")
@Data
@Validated
public class CompaniesHouseProperties {

    /**
     * Base URL of the Companies House API.
     * Default: https://api.company-information.service.gov.uk
     */
    @NotBlank(message = "Base URL must not be blank")
    private String baseUrl;

    /**
     * API key for authentication.
     * Should be provided via environment variable or application-local.yml.
     */
    @NotBlank(message = "API key must not be blank")
    private String apiKey;

    /**
     * Connection timeout in milliseconds.
     * Default: 5000 (5 seconds)
     */
    @Positive(message = "Connect timeout must be positive")
    private int connectTimeoutMs;

    /**
     * Read timeout in milliseconds.
     * Default: 10000 (10 seconds)
     */
    @Positive(message = "Read timeout must be positive")
    private int readTimeoutMs;

    /**
     * Validates that the API key is not a placeholder value.
     * Throws IllegalStateException if configuration is invalid.
     */
    @PostConstruct
    public void validateConfiguration() {
        if (apiKey != null && (apiKey.contains("REPLACE") || apiKey.contains("YOUR_API_KEY"))) {
            throw new IllegalStateException(
                "API key appears to be a placeholder. " +
                "Please set COMPANIES_HOUSE_API_KEY environment variable or " +
                "create application-local.yml with your actual API key."
            );
        }
    }
}
