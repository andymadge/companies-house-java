package com.example.companieshouse.client.exception;

/**
 * Exception thrown when authentication with the Companies House API fails.
 *
 * <p>This exception is thrown when the API returns a 401 HTTP status,
 * indicating that the provided API key is invalid, missing, or has
 * insufficient permissions for the requested operation.
 *
 * <p>Common causes:
 * <ul>
 *   <li>Invalid or expired API key</li>
 *   <li>API key not included in request headers</li>
 *   <li>API key lacks required permissions</li>
 * </ul>
 *
 * <p>Example usage:
 * <pre>
 * if (response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
 *     throw new CompaniesHouseAuthenticationException("Invalid API key");
 * }
 * </pre>
 *
 * @see CompaniesHouseApiException
 */
public class CompaniesHouseAuthenticationException extends CompaniesHouseApiException {

    /**
     * Constructs a new authentication exception with the specified detail message.
     *
     * @param message details about the authentication failure
     */
    public CompaniesHouseAuthenticationException(String message) {
        super("Authentication failed: " + message);
    }
}
