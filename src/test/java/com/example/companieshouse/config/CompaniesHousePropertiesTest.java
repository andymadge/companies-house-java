package com.example.companieshouse.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("CompaniesHouseProperties Tests")
class CompaniesHousePropertiesTest {

    @Autowired
    private CompaniesHouseProperties properties;

    @Test
    @DisplayName("Should bind properties from application-test.yml")
    void testPropertiesBinding() {
        assertThat(properties).isNotNull();
        assertThat(properties.getBaseUrl()).isEqualTo("http://localhost:8089");
        assertThat(properties.getApiKey()).isEqualTo("test-api-key");
        assertThat(properties.getConnectTimeoutMs()).isEqualTo(2000);
        assertThat(properties.getReadTimeoutMs()).isEqualTo(5000);
    }

    @Test
    @DisplayName("Should have non-blank base URL")
    void testBaseUrlNotBlank() {
        assertThat(properties.getBaseUrl())
            .isNotNull()
            .isNotBlank();
    }

    @Test
    @DisplayName("Should have non-blank API key")
    void testApiKeyNotBlank() {
        assertThat(properties.getApiKey())
            .isNotNull()
            .isNotBlank();
    }

    @Test
    @DisplayName("Should have positive connect timeout")
    void testConnectTimeoutPositive() {
        assertThat(properties.getConnectTimeoutMs()).isPositive();
    }

    @Test
    @DisplayName("Should have positive read timeout")
    void testReadTimeoutPositive() {
        assertThat(properties.getReadTimeoutMs()).isPositive();
    }
}
