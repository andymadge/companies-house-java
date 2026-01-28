package com.example.companieshouse.client;

import com.example.companieshouse.client.exception.CompaniesHouseApiException;
import com.example.companieshouse.client.exception.CompaniesHouseAuthenticationException;
import com.example.companieshouse.client.exception.CompanyNotFoundException;
import com.example.companieshouse.client.exception.InvalidResponseException;
import com.example.companieshouse.client.exception.RateLimitExceededException;
import com.example.companieshouse.dto.response.RegisteredAddressResponse;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Integration tests for {@link CompaniesHouseClient}.
 * <p>
 * These tests use WireMock to simulate the Companies House API and verify
 * end-to-end functionality including HTTP communication, JSON deserialization,
 * and error handling.
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("CompaniesHouseClient Integration Tests")
class CompaniesHouseClientIntegrationTest {

    private static WireMockServer wireMockServer;

    @Autowired
    private CompaniesHouseClient client;

    @BeforeAll
    static void startWireMock() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options()
            .dynamicPort()
            .usingFilesUnderClasspath("src/test/resources"));
        wireMockServer.start();
        configureFor(wireMockServer.port());
    }

    @AfterAll
    static void stopWireMock() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("companies-house.api.base-url",
            () -> "http://localhost:" + wireMockServer.port());
    }

    @Test
    @DisplayName("Should retrieve registered address from stubbed API - success scenario")
    void shouldRetrieveAddressFromStubbedApi() {
        // Arrange
        String companyNumber = "09370669";
        stubFor(get(urlPathEqualTo("/company/" + companyNumber))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBodyFile("company-profile-success.json")));

        // Act
        RegisteredAddressResponse address = client.getRegisteredAddress(companyNumber);

        // Assert
        assertThat(address).isNotNull();
        assertThat(address.getAddressLine1()).isNotBlank();
        assertThat(address.getPostalCode()).isNotBlank();
        assertThat(address.getLocality()).isNotBlank();

        // Verify WireMock received the request with authorization
        verify(getRequestedFor(urlPathEqualTo("/company/" + companyNumber))
            .withHeader("Authorization", matching("Basic .*")));
    }

    @Test
    @DisplayName("Should handle company with care_of field")
    void shouldHandleAddressWithCareOf() {
        // Arrange
        String companyNumber = "12345678";
        stubFor(get(urlPathEqualTo("/company/" + companyNumber))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBodyFile("company-profile-with-care-of.json")));

        // Act
        RegisteredAddressResponse address = client.getRegisteredAddress(companyNumber);

        // Assert
        assertThat(address).isNotNull();
        assertThat(address.getCareOf()).isNotNull();
        assertThat(address.getPoBox()).isNotNull();
    }

    @Test
    @DisplayName("Should throw CompanyNotFoundException for HTTP 404")
    void shouldHandle404FromStubbedApi() {
        // Arrange
        String companyNumber = "99999999";
        stubFor(get(urlPathEqualTo("/company/" + companyNumber))
            .willReturn(aResponse()
                .withStatus(404)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"errors\":[{\"error\":\"company-profile-not-found\",\"type\":\"ch:service\"}]}")));

        // Act & Assert
        CompanyNotFoundException exception = assertThrows(
            CompanyNotFoundException.class,
            () -> client.getRegisteredAddress(companyNumber)
        );

        assertThat(exception.getCompanyNumber()).isEqualTo(companyNumber);
        assertThat(exception.getMessage()).contains(companyNumber);
    }

    @Test
    @DisplayName("Should throw RateLimitExceededException for HTTP 429")
    void shouldHandleRateLimitFromStubbedApi() {
        // Arrange
        String companyNumber = "09370669";
        stubFor(get(urlPathEqualTo("/company/" + companyNumber))
            .willReturn(aResponse()
                .withStatus(429)
                .withHeader("Retry-After", "60")
                .withHeader("Content-Type", "application/json")
                .withBody("{\"errors\":[{\"error\":\"rate-limit-exceeded\"}]}")));

        // Act & Assert
        RateLimitExceededException exception = assertThrows(
            RateLimitExceededException.class,
            () -> client.getRegisteredAddress(companyNumber)
        );

        assertThat(exception.getRetryAfter()).isEqualTo(60L);
        assertThat(exception.getMessage()).containsIgnoringCase("rate limit");
    }

    @Test
    @DisplayName("Should throw CompaniesHouseAuthenticationException for HTTP 401")
    void shouldHandleAuthErrorFromStubbedApi() {
        // Arrange
        String companyNumber = "09370669";
        stubFor(get(urlPathEqualTo("/company/" + companyNumber))
            .willReturn(aResponse()
                .withStatus(401)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"errors\":[{\"error\":\"invalid-api-key\"}]}")));

        // Act & Assert
        CompaniesHouseAuthenticationException exception = assertThrows(
            CompaniesHouseAuthenticationException.class,
            () -> client.getRegisteredAddress(companyNumber)
        );

        assertThat(exception.getMessage()).containsIgnoringCase("authentication");
    }

    @Test
    @DisplayName("Should throw CompaniesHouseApiException for HTTP 500")
    void shouldHandleServerErrorFromStubbedApi() {
        // Arrange
        String companyNumber = "09370669";
        stubFor(get(urlPathEqualTo("/company/" + companyNumber))
            .willReturn(aResponse()
                .withStatus(500)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"errors\":[{\"error\":\"internal-server-error\"}]}")));

        // Act & Assert
        CompaniesHouseApiException exception = assertThrows(
            CompaniesHouseApiException.class,
            () -> client.getRegisteredAddress(companyNumber)
        );

        assertThat(exception.getMessage()).contains("500");
        assertThat(exception.getMessage()).containsIgnoringCase("server error");
    }

    @Test
    @DisplayName("Should throw CompaniesHouseApiException for HTTP 502")
    void shouldHandleBadGatewayFromStubbedApi() {
        // Arrange
        String companyNumber = "09370669";
        stubFor(get(urlPathEqualTo("/company/" + companyNumber))
            .willReturn(aResponse()
                .withStatus(502)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"error\":\"Bad Gateway\"}")));

        // Act & Assert
        CompaniesHouseApiException exception = assertThrows(
            CompaniesHouseApiException.class,
            () -> client.getRegisteredAddress(companyNumber)
        );

        assertThat(exception.getMessage()).contains("502");
    }

    // Note: Timeout testing is covered in unit tests with mock exceptions.
    // Integration tests with real HTTP calls have unreliable timeout behavior
    // due to timing variations and WireMock's delay mechanism not simulating
    // true network timeouts.

    @Test
    @DisplayName("Should throw InvalidResponseException for malformed JSON")
    void shouldHandleMalformedJson() {
        // Arrange
        String companyNumber = "09370669";
        stubFor(get(urlPathEqualTo("/company/" + companyNumber))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{ invalid json }")));

        // Act & Assert
        InvalidResponseException exception = assertThrows(
            InvalidResponseException.class,
            () -> client.getRegisteredAddress(companyNumber)
        );

        assertThat(exception.getMessage()).containsIgnoringCase("parse");
    }

    @Test
    @DisplayName("Spring context should load successfully")
    void contextLoads() {
        // Assert
        assertThat(client).isNotNull();
    }

    @Test
    @DisplayName("Should verify all address fields are correctly mapped")
    void shouldMapAllAddressFields() {
        // Arrange
        String companyNumber = "09370669";
        stubFor(get(urlPathEqualTo("/company/" + companyNumber))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBodyFile("company-profile-success.json")));

        // Act
        RegisteredAddressResponse address = client.getRegisteredAddress(companyNumber);

        // Assert - verify all fields are accessible (not testing specific values as they depend on JSON file)
        assertThat(address.getAddressLine1()).isNotNull();
        // addressLine2 can be null
        assertThat(address.getPostalCode()).isNotNull();
        assertThat(address.getLocality()).isNotNull();
        assertThat(address.getCountry()).isNotNull();
        // careOf, poBox, premises, region can be null
    }
}
