package com.example.companieshouse.client.exception;

/**
 * Exception thrown when the client configuration is invalid or incomplete.
 *
 * <p>This exception is thrown during application startup or initialization
 * when required configuration properties are missing, invalid, or improperly formatted.
 *
 * <p>Common causes:
 * <ul>
 *   <li>Missing or empty API key</li>
 *   <li>Invalid base URL format</li>
 *   <li>Invalid timeout values (negative or zero)</li>
 *   <li>API key is a placeholder value</li>
 * </ul>
 *
 * <p>Note: This exception extends {@link RuntimeException} directly (not
 * {@link CompaniesHouseApiException}) because it represents a configuration
 * error rather than an API interaction error.
 *
 * <p>Example usage:
 * <pre>
 * @PostConstruct
 * public void validate() {
 *     if (apiKey == null || apiKey.isBlank()) {
 *         throw new InvalidConfigurationException("API key must be provided");
 *     }
 * }
 * </pre>
 *
 * @see com.example.companieshouse.config.CompaniesHouseProperties
 */
public class InvalidConfigurationException extends RuntimeException {

    /**
     * Constructs a new configuration exception with the specified detail message.
     *
     * @param message details about the configuration problem
     */
    public InvalidConfigurationException(String message) {
        super("Invalid configuration: " + message);
    }
}
