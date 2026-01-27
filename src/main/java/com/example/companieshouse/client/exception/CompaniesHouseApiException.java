package com.example.companieshouse.client.exception;

/**
 * Base exception for all Companies House API-related errors.
 *
 * <p>This unchecked exception serves as the parent for all specific
 * API error scenarios such as company not found, rate limiting, and
 * authentication failures.
 *
 * <p>Extends {@link RuntimeException} to avoid forcing calling code
 * to handle checked exceptions, following modern Spring Boot practices.
 *
 * @see CompanyNotFoundException
 * @see RateLimitExceededException
 * @see CompaniesHouseAuthenticationException
 * @see InvalidResponseException
 */
public class CompaniesHouseApiException extends RuntimeException {

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message the detail message explaining the error
     */
    public CompaniesHouseApiException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     *
     * @param message the detail message explaining the error
     * @param cause the cause of the error (null if no cause available)
     */
    public CompaniesHouseApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
