package com.example.companieshouse.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object representing a registered office address from the Companies House API.
 *
 * <p>Maps the snake_case JSON fields from the Companies House API to camelCase Java fields.
 * This DTO corresponds to the {@code registered_office_address} object in the company profile response.
 *
 * <p>Example JSON:
 * <pre>
 * {
 *   "address_line_1": "123 High Street",
 *   "address_line_2": "Floor 2",
 *   "locality": "London",
 *   "postal_code": "SW1A 1AA",
 *   "country": "United Kingdom",
 *   "premises": "Building A",
 *   "region": "Greater London",
 *   "care_of": "John Smith",
 *   "po_box": "PO Box 123"
 * }
 * </pre>
 *
 * @see CompanyProfileResponse
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisteredAddressResponse {

    /**
     * Primary address line (typically street name and number).
     * This field is required by the Companies House API.
     */
    @JsonProperty("address_line_1")
    private String addressLine1;

    /**
     * Secondary address line (e.g., suite, floor, building name).
     * Optional field.
     */
    @JsonProperty("address_line_2")
    private String addressLine2;

    /**
     * Town or city name.
     * Optional field.
     */
    @JsonProperty("locality")
    private String locality;

    /**
     * Postal code or ZIP code.
     * This field is typically required for UK addresses.
     */
    @JsonProperty("postal_code")
    private String postalCode;

    /**
     * Country name.
     * Optional field.
     */
    @JsonProperty("country")
    private String country;

    /**
     * Region or county name.
     * Optional field.
     */
    @JsonProperty("region")
    private String region;

    /**
     * Premises identifier (e.g., building name, unit number).
     * Optional field.
     */
    @JsonProperty("premises")
    private String premises;

    /**
     * Name of the person or organization to whom mail should be addressed.
     * Used when the registered office is care of another party.
     * Optional field.
     */
    @JsonProperty("care_of")
    private String careOf;

    /**
     * Post Office box number.
     * Optional field.
     */
    @JsonProperty("po_box")
    private String poBox;
}
