package com.example.companieshouse.client.exception;

import lombok.Getter;

/**
 * Exception thrown when the API rate limit has been exceeded.
 *
 * <p>This exception is thrown when the API returns a 429 HTTP status,
 * indicating that too many requests have been made in a given time period.
 *
 * <p>The exception may include a {@code retryAfter} value indicating
 * how many seconds the caller should wait before retrying the request.
 * This value comes from the {@code Retry-After} HTTP header if present.
 *
 * <p>Example usage:
 * <pre>
 * if (response.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
 *     Long retryAfter = parseRetryAfterHeader(response);
 *     throw new RateLimitExceededException("Rate limit exceeded", retryAfter);
 * }
 * </pre>
 *
 * @see CompaniesHouseApiException
 */
@Getter
public class RateLimitExceededException extends CompaniesHouseApiException {

    /**
     * Number of seconds to wait before retrying, or null if not specified.
     */
    private final Long retryAfter;

    /**
     * Constructs a new exception for rate limit exceeded.
     *
     * @param message additional context about the rate limit error
     * @param retryAfter number of seconds to wait before retrying (may be null)
     */
    public RateLimitExceededException(String message, Long retryAfter) {
        super(formatMessage(message, retryAfter));
        this.retryAfter = retryAfter;
    }

    /**
     * Formats the exception message including retry-after information if available.
     *
     * @param message the base message
     * @param retryAfter the retry-after duration in seconds (may be null)
     * @return formatted message
     */
    private static String formatMessage(String message, Long retryAfter) {
        if (retryAfter != null) {
            return message + ". Retry after: " + retryAfter + " seconds";
        }
        return message;
    }
}
