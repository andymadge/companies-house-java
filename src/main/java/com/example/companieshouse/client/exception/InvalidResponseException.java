package com.example.companieshouse.client.exception;

/**
 * Exception thrown when the API response cannot be parsed or is malformed.
 *
 * <p>This exception is thrown when the client receives a response from
 * the Companies House API that cannot be deserialized into the expected
 * DTO objects, or when the response format is unexpected.
 *
 * <p>Common causes:
 * <ul>
 *   <li>Invalid JSON format in response</li>
 *   <li>Missing required fields in response</li>
 *   <li>Unexpected response structure</li>
 *   <li>Empty response body when data is expected</li>
 * </ul>
 *
 * <p>The exception typically includes the underlying parse error as the cause.
 *
 * <p>Example usage:
 * <pre>
 * try {
 *     return objectMapper.readValue(json, CompanyProfileResponse.class);
 * } catch (JsonProcessingException e) {
 *     throw new InvalidResponseException("Failed to parse company profile", e);
 * }
 * </pre>
 *
 * @see CompaniesHouseApiException
 */
public class InvalidResponseException extends CompaniesHouseApiException {

    /**
     * Constructs a new exception for an invalid or unparseable response.
     *
     * @param message details about what went wrong during parsing
     * @param cause the underlying parse exception (may be null)
     */
    public InvalidResponseException(String message, Throwable cause) {
        super("Failed to parse response: " + message, cause);
    }
}
