package com.example.companieshouse.client;

import com.example.companieshouse.client.exception.InvalidResponseException;
import com.example.companieshouse.dto.response.CompanyProfileResponse;
import com.example.companieshouse.dto.response.RegisteredAddressResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

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
        log.debug("Fetching registered address for company: {}", companyNumber);

        CompanyProfileResponse profile = restClient.get()
            .uri("/company/{companyNumber}", companyNumber)
            .retrieve()
            .body(CompanyProfileResponse.class);

        return extractAddress(profile, companyNumber);
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
