package com.example.companieshouse.dto.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for DTO deserialization from Companies House API JSON responses.
 * Verifies that DTOs correctly map snake_case JSON to camelCase Java fields.
 */
@JsonTest
@DisplayName("RegisteredAddressResponse DTO Tests")
class RegisteredAddressResponseTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should deserialize Companies House JSON response with all address fields")
    void shouldDeserializeFullCompanyProfile() throws Exception {
        // Given: Real JSON response from Companies House API
        String json = loadResource("__files/company-profile-success.json");

        // When: Deserialize to DTO
        CompanyProfileResponse response = objectMapper.readValue(json, CompanyProfileResponse.class);

        // Then: Verify company fields
        assertThat(response).isNotNull();
        assertThat(response.getCompanyNumber()).isEqualTo("09370669");
        assertThat(response.getCompanyName()).isEqualTo("ANTHROPIC UK LTD");
        assertThat(response.getCompanyStatus()).isEqualTo("active");

        // Then: Verify address fields are deserialized correctly
        RegisteredAddressResponse address = response.getRegisteredOfficeAddress();
        assertThat(address).isNotNull();
        assertThat(address.getAddressLine1()).isEqualTo("123 High Street");
        assertThat(address.getAddressLine2()).isEqualTo("Floor 2");
        assertThat(address.getLocality()).isEqualTo("London");
        assertThat(address.getPostalCode()).isEqualTo("SW1A 1AA");
        assertThat(address.getCountry()).isEqualTo("United Kingdom");
        assertThat(address.getPremises()).isEqualTo("Building A");
        assertThat(address.getRegion()).isEqualTo("Greater London");
    }

    @Test
    @DisplayName("Should deserialize address with care_of and po_box fields")
    void shouldDeserializeAddressWithCareOfAndPoBox() throws Exception {
        // Given: JSON with care_of and po_box
        String json = loadResource("__files/company-profile-with-care-of.json");

        // When: Deserialize to DTO
        CompanyProfileResponse response = objectMapper.readValue(json, CompanyProfileResponse.class);

        // Then: Verify care_of and po_box fields
        RegisteredAddressResponse address = response.getRegisteredOfficeAddress();
        assertThat(address).isNotNull();
        assertThat(address.getCareOf()).isEqualTo("John Smith");
        assertThat(address.getPoBox()).isEqualTo("PO Box 123");
        assertThat(address.getPremises()).isEqualTo("Unit 5");
        assertThat(address.getAddressLine1()).isEqualTo("Industrial Estate");
        assertThat(address.getPostalCode()).isEqualTo("M1 1AA");
    }

    @Test
    @DisplayName("Should handle missing optional address fields")
    void shouldHandleMissingOptionalFields() throws Exception {
        // Given: Minimal JSON with only required fields
        String json = """
            {
              "company_number": "99999999",
              "company_name": "MINIMAL COMPANY",
              "company_status": "active",
              "type": "ltd",
              "registered_office_address": {
                "address_line_1": "Main Street",
                "postal_code": "AB1 2CD"
              },
              "jurisdiction": "england-wales"
            }
            """;

        // When: Deserialize to DTO
        CompanyProfileResponse response = objectMapper.readValue(json, CompanyProfileResponse.class);

        // Then: Verify required fields present
        RegisteredAddressResponse address = response.getRegisteredOfficeAddress();
        assertThat(address).isNotNull();
        assertThat(address.getAddressLine1()).isEqualTo("Main Street");
        assertThat(address.getPostalCode()).isEqualTo("AB1 2CD");

        // Then: Verify optional fields are null or absent
        assertThat(address.getAddressLine2()).isNullOrEmpty();
        assertThat(address.getLocality()).isNullOrEmpty();
        assertThat(address.getCareOf()).isNullOrEmpty();
        assertThat(address.getPoBox()).isNullOrEmpty();
    }

    @Test
    @DisplayName("Should serialize DTO back to JSON")
    void shouldSerializeToJson() throws Exception {
        // Given: A DTO instance
        RegisteredAddressResponse address = RegisteredAddressResponse.builder()
                .addressLine1("Test Street")
                .addressLine2("Suite 100")
                .locality("Test City")
                .postalCode("TE1 1ST")
                .country("England")
                .build();

        CompanyProfileResponse profile = CompanyProfileResponse.builder()
                .companyNumber("00000000")
                .companyName("TEST LTD")
                .companyStatus("active")
                .registeredOfficeAddress(address)
                .build();

        // When: Serialize to JSON
        String json = objectMapper.writeValueAsString(profile);

        // Then: Verify JSON contains snake_case fields
        assertThat(json).contains("company_number");
        assertThat(json).contains("company_name");
        assertThat(json).contains("registered_office_address");
        assertThat(json).contains("address_line_1");
        assertThat(json).contains("postal_code");
    }

    @Test
    @DisplayName("Should round-trip serialize and deserialize")
    void shouldRoundTripSerializeDeserialize() throws Exception {
        // Given: Original JSON
        String originalJson = loadResource("__files/company-profile-success.json");
        CompanyProfileResponse original = objectMapper.readValue(originalJson, CompanyProfileResponse.class);

        // When: Serialize and deserialize
        String serialized = objectMapper.writeValueAsString(original);
        CompanyProfileResponse roundTripped = objectMapper.readValue(serialized, CompanyProfileResponse.class);

        // Then: Verify data integrity
        assertThat(roundTripped.getCompanyNumber()).isEqualTo(original.getCompanyNumber());
        assertThat(roundTripped.getRegisteredOfficeAddress().getAddressLine1())
                .isEqualTo(original.getRegisteredOfficeAddress().getAddressLine1());
        assertThat(roundTripped.getRegisteredOfficeAddress().getPostalCode())
                .isEqualTo(original.getRegisteredOfficeAddress().getPostalCode());
    }

    /**
     * Helper method to load test resource files.
     */
    private String loadResource(String path) throws IOException {
        ClassPathResource resource = new ClassPathResource(path);
        return Files.readString(resource.getFile().toPath());
    }
}
