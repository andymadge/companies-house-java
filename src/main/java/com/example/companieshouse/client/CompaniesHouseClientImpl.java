package com.example.companieshouse.client;

import com.example.companieshouse.client.exception.CompaniesHouseApiException;
import com.example.companieshouse.client.exception.CompaniesHouseAuthenticationException;
import com.example.companieshouse.client.exception.CompanyNotFoundException;
import com.example.companieshouse.client.exception.InvalidResponseException;
import com.example.companieshouse.client.exception.RateLimitExceededException;
import com.example.companieshouse.dto.response.CompanyProfileResponse;
import com.example.companieshouse.dto.response.RegisteredAddressResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Implementation of {@link CompaniesHouseClient} using Spring's RestClient.
 *
 * <p>This implementation makes synchronous HTTP calls to the Companies House
 * Public Data API. The RestClient is pre-configured with the base URL, API key
 * authentication, and timeout settings via {@link com.example.companieshouse.config.CompaniesHouseConfig}.
 *
 * <p>Thread-safety: This class is thread-safe. The injected RestClient is immutable
 * and can be safely shared across multiple threads.
 *
 * @see CompaniesHouseClient
 * @see com.example.companieshouse.config.CompaniesHouseConfig
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CompaniesHouseClientImpl implements CompaniesHouseClient {

    private final RestClient restClient;

    /**
     * {@inheritDoc}
     */
    @Override
    public RegisteredAddressResponse getRegisteredAddress(String companyNumber) {
        validateCompanyNumber(companyNumber);

        log.debug("Fetching registered address for company: {}", companyNumber);

        try {
            CompanyProfileResponse profile = restClient.get()
                .uri("/company/{companyNumber}", companyNumber)
                .retrieve()
                .body(CompanyProfileResponse.class);

            return extractAddress(profile, companyNumber);

        } catch (HttpClientErrorException e) {
            handleClientError(e, companyNumber);
            throw new CompaniesHouseApiException(
                "Client error: HTTP " + e.getStatusCode().value(), e);
        } catch (HttpServerErrorException e) {
            throw new CompaniesHouseApiException(
                "Companies House API server error (HTTP " + e.getStatusCode().value() + ")", e);
        } catch (ResourceAccessException e) {
            throw new CompaniesHouseApiException(
                "Failed to connect to Companies House API: " + e.getMessage(), e);
        } catch (RestClientException e) {
            throw new InvalidResponseException(
                "Failed to parse API response for company: " + companyNumber, e);
        }
    }

    /**
     * Validates the company number parameter.
     *
     * @param companyNumber the company number to validate
     * @throws IllegalArgumentException if the company number is null or blank
     */
    private void validateCompanyNumber(String companyNumber) {
        if (companyNumber == null || companyNumber.isBlank()) {
            throw new IllegalArgumentException(
                "Company number must not be null or blank");
        }
    }

    /**
     * Handles HTTP client errors (4xx status codes) by throwing appropriate exceptions.
     *
     * @param exception     the HTTP client error exception
     * @param companyNumber the company number (for error context)
     * @throws CompanyNotFoundException if the status is 404
     * @throws RateLimitExceededException if the status is 429
     * @throws CompaniesHouseAuthenticationException if the status is 401
     */
    private void handleClientError(HttpClientErrorException exception, String companyNumber) {
        HttpStatus status = (HttpStatus) exception.getStatusCode();

        if (status == HttpStatus.NOT_FOUND) {
            throw new CompanyNotFoundException(companyNumber);
        }

        if (status == HttpStatus.TOO_MANY_REQUESTS) {
            Long retryAfter = extractRetryAfter(exception);
            throw new RateLimitExceededException("Rate limit exceeded", retryAfter);
        }

        if (status == HttpStatus.UNAUTHORIZED) {
            throw new CompaniesHouseAuthenticationException(
                "Authentication failed - check API key configuration");
        }

        // For other 4xx errors, let it fall through to the generic handler
        log.warn("Unexpected client error (HTTP {}): {}", status.value(), exception.getMessage());
    }

    /**
     * Extracts the Retry-After header value from an HTTP exception.
     *
     * @param exception the HTTP exception
     * @return the retry-after value in seconds, or null if not present or unparseable
     */
    private Long extractRetryAfter(HttpClientErrorException exception) {
        try {
            String retryAfterHeader = exception.getResponseHeaders() != null
                ? exception.getResponseHeaders().getFirst("Retry-After")
                : null;

            return retryAfterHeader != null ? Long.parseLong(retryAfterHeader) : null;
        } catch (NumberFormatException e) {
            log.warn("Failed to parse Retry-After header: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Extracts the registered office address from the company profile response.
     *
     * @param profile       the company profile response (may be null)
     * @param companyNumber the company number (for error messages)
     * @return the registered office address
     * @throws InvalidResponseException if the profile or address is null
     */
    private RegisteredAddressResponse extractAddress(
            CompanyProfileResponse profile, String companyNumber) {

        if (profile == null) {
            throw new InvalidResponseException(
                "API returned null response for company: " + companyNumber, null);
        }

        RegisteredAddressResponse address = profile.getRegisteredOfficeAddress();
        if (address == null) {
            throw new InvalidResponseException(
                "No registered address found for company: " + companyNumber, null);
        }

        log.debug("Successfully retrieved address for company: {}", companyNumber);
        return address;
    }
}
