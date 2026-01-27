package com.example.companieshouse.client.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for custom exception hierarchy.
 * Verifies that all exceptions include proper context and extend correct base classes.
 */
@DisplayName("Custom Exception Tests")
class ExceptionTests {

    @Test
    @DisplayName("CompanyNotFoundException should include company number in message")
    void testCompanyNotFoundException() {
        // Given: A company number
        String companyNumber = "09370669";

        // When: Exception is created
        CompanyNotFoundException exception = new CompanyNotFoundException(companyNumber);

        // Then: Message includes company number and accessor works
        assertThat(exception.getMessage()).contains("09370669");
        assertThat(exception.getMessage()).contains("Company not found");
        assertThat(exception.getCompanyNumber()).isEqualTo("09370669");
        assertThat(exception).isInstanceOf(CompaniesHouseApiException.class);
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("RateLimitExceededException should store retryAfter value")
    void testRateLimitExceededException() {
        // Given: Rate limit retry duration
        Long retryAfter = 60L;
        String message = "Rate limit exceeded";

        // When: Exception is created
        RateLimitExceededException exception = new RateLimitExceededException(message, retryAfter);

        // Then: Message and retry value are accessible
        assertThat(exception.getMessage()).contains("Rate limit exceeded");
        assertThat(exception.getMessage()).contains("60");
        assertThat(exception.getRetryAfter()).isEqualTo(60L);
        assertThat(exception).isInstanceOf(CompaniesHouseApiException.class);
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("RateLimitExceededException should handle null retryAfter")
    void testRateLimitExceededExceptionWithNullRetryAfter() {
        // Given: No retry duration provided
        String message = "Rate limit exceeded";

        // When: Exception is created without retryAfter
        RateLimitExceededException exception = new RateLimitExceededException(message, null);

        // Then: Exception still works correctly
        assertThat(exception.getMessage()).contains("Rate limit exceeded");
        assertThat(exception.getRetryAfter()).isNull();
    }

    @Test
    @DisplayName("CompaniesHouseAuthenticationException should include auth error details")
    void testCompaniesHouseAuthenticationException() {
        // Given: An authentication error message
        String message = "Invalid API key";

        // When: Exception is created
        CompaniesHouseAuthenticationException exception = new CompaniesHouseAuthenticationException(message);

        // Then: Message includes authentication context
        assertThat(exception.getMessage()).contains("Authentication failed");
        assertThat(exception.getMessage()).contains("Invalid API key");
        assertThat(exception).isInstanceOf(CompaniesHouseApiException.class);
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("InvalidResponseException should include cause and message")
    void testInvalidResponseException() {
        // Given: A parse error with cause
        String message = "Failed to parse JSON response";
        Throwable cause = new IllegalArgumentException("Invalid JSON format");

        // When: Exception is created
        InvalidResponseException exception = new InvalidResponseException(message, cause);

        // Then: Message and cause are accessible
        assertThat(exception.getMessage()).contains("Failed to parse response");
        assertThat(exception.getMessage()).contains("Failed to parse JSON response");
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception).isInstanceOf(CompaniesHouseApiException.class);
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("InvalidResponseException should work without cause")
    void testInvalidResponseExceptionWithoutCause() {
        // Given: A parse error without cause
        String message = "Empty response body";

        // When: Exception is created without cause
        InvalidResponseException exception = new InvalidResponseException(message, null);

        // Then: Exception still works correctly
        assertThat(exception.getMessage()).contains("Failed to parse response");
        assertThat(exception.getMessage()).contains("Empty response body");
        assertThat(exception.getCause()).isNull();
    }

    @Test
    @DisplayName("InvalidConfigurationException should include configuration error details")
    void testInvalidConfigurationException() {
        // Given: A configuration error message
        String message = "Base URL is not set";

        // When: Exception is created
        InvalidConfigurationException exception = new InvalidConfigurationException(message);

        // Then: Message includes configuration context
        assertThat(exception.getMessage()).contains("Invalid configuration");
        assertThat(exception.getMessage()).contains("Base URL is not set");
        assertThat(exception).isInstanceOf(RuntimeException.class);
        // Note: InvalidConfigurationException extends RuntimeException directly, not CompaniesHouseApiException
    }

    @Test
    @DisplayName("CompaniesHouseApiException should be base for API-related exceptions")
    void testCompaniesHouseApiExceptionAsBase() {
        // Given: Base exception with message
        String message = "Generic API error";

        // When: Base exception is created
        CompaniesHouseApiException exception = new CompaniesHouseApiException(message);

        // Then: Exception is created correctly
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("CompaniesHouseApiException should support message and cause")
    void testCompaniesHouseApiExceptionWithCause() {
        // Given: Exception with message and cause
        String message = "API call failed";
        Throwable cause = new RuntimeException("Network error");

        // When: Exception is created with cause
        CompaniesHouseApiException exception = new CompaniesHouseApiException(message, cause);

        // Then: Message and cause are preserved
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
    }
}
