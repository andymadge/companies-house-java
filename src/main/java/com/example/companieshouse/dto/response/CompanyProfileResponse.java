package com.example.companieshouse.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object representing a company profile from the Companies House API.
 *
 * <p>Maps the snake_case JSON fields from the Companies House API to camelCase Java fields.
 * This DTO represents the response from the {@code /company/{companyNumber}} endpoint.
 *
 * <p>Example JSON:
 * <pre>
 * {
 *   "company_number": "09370669",
 *   "company_name": "ANTHROPIC UK LTD",
 *   "company_status": "active",
 *   "type": "ltd",
 *   "date_of_creation": "2014-12-18",
 *   "registered_office_address": { ... },
 *   "jurisdiction": "england-wales"
 * }
 * </pre>
 *
 * <p>Note: This DTO currently includes only the fields needed for the client library.
 * The Companies House API returns additional fields which are omitted here but can be
 * added as needed.
 *
 * @see RegisteredAddressResponse
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyProfileResponse {

    /**
     * The company registration number (e.g., "09370669").
     * This is the unique identifier for the company.
     */
    @JsonProperty("company_number")
    private String companyNumber;

    /**
     * The registered name of the company.
     */
    @JsonProperty("company_name")
    private String companyName;

    /**
     * The current status of the company (e.g., "active", "dissolved").
     */
    @JsonProperty("company_status")
    private String companyStatus;

    /**
     * The type of company (e.g., "ltd", "plc", "llp").
     */
    @JsonProperty("type")
    private String type;

    /**
     * The date the company was created/incorporated (ISO 8601 format).
     */
    @JsonProperty("date_of_creation")
    private String dateOfCreation;

    /**
     * The registered office address of the company.
     * This is the primary data we need from the API.
     */
    @JsonProperty("registered_office_address")
    private RegisteredAddressResponse registeredOfficeAddress;

    /**
     * The jurisdiction where the company is registered (e.g., "england-wales", "scotland").
     */
    @JsonProperty("jurisdiction")
    private String jurisdiction;
}
