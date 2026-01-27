package com.example.companieshouse.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        assertThat(properties.getBaseUrl()).startsWith("http://localhost:");
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

    @Test
    @DisplayName("Should reject placeholder API key in validation")
    void testPlaceholderApiKeyRejected() {
        CompaniesHouseProperties testProps = new CompaniesHouseProperties();
        testProps.setBaseUrl("http://localhost:8080");
        testProps.setApiKey("REPLACE_WITH_YOUR_API_KEY");
        testProps.setConnectTimeoutMs(5000);
        testProps.setReadTimeoutMs(10000);

        assertThatThrownBy(testProps::validateConfiguration)
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("API key appears to be a placeholder");
    }

    @Test
    @DisplayName("Should accept valid API key in validation")
    void testValidApiKeyAccepted() {
        CompaniesHouseProperties testProps = new CompaniesHouseProperties();
        testProps.setBaseUrl("http://localhost:8080");
        testProps.setApiKey("valid-api-key-123");
        testProps.setConnectTimeoutMs(5000);
        testProps.setReadTimeoutMs(10000);

        // Should not throw
        testProps.validateConfiguration();
    }

    @Test
    @DisplayName("Should handle null API key in validation")
    void testNullApiKeyHandled() {
        CompaniesHouseProperties testProps = new CompaniesHouseProperties();
        testProps.setBaseUrl("http://localhost:8080");
        testProps.setApiKey(null);
        testProps.setConnectTimeoutMs(5000);
        testProps.setReadTimeoutMs(10000);

        // Should not throw - null is handled by @NotBlank validation
        testProps.validateConfiguration();
    }
}
