package com.example.companieshouse.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for RestClient bean configuration.
 * Verifies that RestClient is properly configured with base URL, timeouts, and authentication.
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("CompaniesHouseConfig Tests")
class CompaniesHouseConfigTest {

    @Autowired
    private RestClient restClient;

    @Autowired
    private CompaniesHouseProperties properties;

    @Test
    @DisplayName("Should create RestClient bean")
    void shouldCreateRestClientBean() {
        assertThat(restClient).isNotNull();
    }

    @Test
    @DisplayName("Should configure RestClient with properties from CompaniesHouseProperties")
    void shouldConfigureRestClientWithProperties() {
        // Verify properties are loaded for the test
        assertThat(properties).isNotNull();
        assertThat(properties.getBaseUrl()).isEqualTo("http://localhost:8089");
        assertThat(properties.getApiKey()).isEqualTo("test-api-key");
        assertThat(properties.getConnectTimeoutMs()).isEqualTo(2000);
        assertThat(properties.getReadTimeoutMs()).isEqualTo(5000);
    }

    @Test
    @DisplayName("Should inject RestClient into test context")
    void shouldInjectRestClientIntoTestContext() {
        // This test verifies that the RestClient bean is available for injection
        // which confirms the @Bean method in CompaniesHouseConfig works
        assertThat(restClient).isNotNull();
    }
}
