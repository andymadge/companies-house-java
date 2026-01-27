package com.example.companieshouse.client;

import com.example.companieshouse.client.exception.CompaniesHouseApiException;
import com.example.companieshouse.client.exception.CompaniesHouseAuthenticationException;
import com.example.companieshouse.client.exception.CompanyNotFoundException;
import com.example.companieshouse.client.exception.RateLimitExceededException;
import com.example.companieshouse.dto.response.RegisteredAddressResponse;

/**
 * Client for the Companies House Public Data API.
 *
 * <p>Provides methods to retrieve company information from the UK Companies House API.
 * This interface defines the public contract for accessing company data, with a focus
 * on registered office addresses.
 *
 * <p>All methods throw specific exception types for different error scenarios,
 * enabling precise error handling in calling code. Exceptions extend {@link RuntimeException}
 * so they do not need to be declared in method signatures.
 *
 * <p>Example usage:
 * <pre>
 * try {
 *     RegisteredAddressResponse address = client.getRegisteredAddress("09370669");
 *     System.out.println(address.getPostalCode());
 * } catch (CompanyNotFoundException e) {
 *     System.err.println("Company not found: " + e.getCompanyNumber());
 * } catch (RateLimitExceededException e) {
 *     System.err.println("Rate limited. Retry after: " + e.getRetryAfter() + " seconds");
 * }
 * </pre>
 *
 * @see CompaniesHouseApiException
 * @see RegisteredAddressResponse
 */
public interface CompaniesHouseClient {

    /**
     * Retrieves the registered office address for a UK company.
     *
     * <p>Queries the Companies House API using the provided company number
     * and returns the registered office address details. The company number
     * should be in the standard UK format (typically 8 characters, e.g., "09370669").
     *
     * <p>This method makes a synchronous HTTP call to the Companies House API
     * and blocks until the response is received or an error occurs.
     *
     * @param companyNumber the UK company registration number (e.g., "09370669").
     *                      Must not be null or blank.
     * @return the registered office address for the company, never null
     * @throws CompanyNotFoundException if the company does not exist in the
     *         Companies House registry (HTTP 404)
     * @throws RateLimitExceededException if the API rate limit has been exceeded
     *         (HTTP 429). Check {@link RateLimitExceededException#getRetryAfter()}
     *         for retry duration.
     * @throws CompaniesHouseAuthenticationException if authentication fails due to
     *         invalid or missing API key (HTTP 401)
     * @throws CompaniesHouseApiException for other API errors such as server errors
     *         (HTTP 5xx), network timeouts, or unexpected responses
     * @throws IllegalArgumentException if companyNumber is null, blank, or has
     *         an invalid format
     */
    RegisteredAddressResponse getRegisteredAddress(String companyNumber);
}
