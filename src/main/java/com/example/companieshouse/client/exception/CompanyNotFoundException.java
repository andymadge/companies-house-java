package com.example.companieshouse.client.exception;

import lombok.Getter;

/**
 * Exception thrown when a company is not found in the Companies House registry.
 *
 * <p>This exception is thrown when the API returns a 404 HTTP status,
 * indicating that the requested company number does not exist or is
 * not available in the public registry.
 *
 * <p>The exception stores the company number for logging and error handling purposes.
 *
 * <p>Example usage:
 * <pre>
 * if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
 *     throw new CompanyNotFoundException(companyNumber);
 * }
 * </pre>
 *
 * @see CompaniesHouseApiException
 */
@Getter
public class CompanyNotFoundException extends CompaniesHouseApiException {

    /**
     * The company number that was not found.
     */
    private final String companyNumber;

    /**
     * Constructs a new exception for a company that was not found.
     *
     * @param companyNumber the UK company number that was not found (e.g., "09370669")
     */
    public CompanyNotFoundException(String companyNumber) {
        super("Company not found: " + companyNumber);
        this.companyNumber = companyNumber;
    }
}
