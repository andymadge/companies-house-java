package com.example.companieshouse.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.Base64;

/**
 * Spring configuration for Companies House API client.
 *
 * <p>Configures the RestClient bean with:
 * <ul>
 *   <li>Base URL pointing to Companies House API</li>
 *   <li>Basic authentication using API key (per Companies House API requirements)</li>
 *   <li>Connection and read timeouts from properties</li>
 * </ul>
 *
 * <p>The Companies House API requires Basic authentication with the API key
 * as the username and an empty password. This is automatically configured
 * via the Authorization header.
 */
@Configuration
@RequiredArgsConstructor
public class CompaniesHouseConfig {

    private final CompaniesHouseProperties properties;

    /**
     * Creates a RestClient bean configured for Companies House API access.
     *
     * <p>The RestClient is configured with:
     * <ul>
     *   <li>Base URL from properties (companies-house.api.base-url)</li>
     *   <li>Basic authentication header with API key</li>
     *   <li>Connection timeout from properties (companies-house.api.connect-timeout-ms)</li>
     *   <li>Read timeout from properties (companies-house.api.read-timeout-ms)</li>
     * </ul>
     *
     * @return configured RestClient instance for Companies House API calls
     */
    @Bean
    public RestClient restClient() {
        return RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .defaultHeader("Authorization", createBasicAuthHeader(properties.getApiKey()))
                .requestFactory(clientHttpRequestFactory())
                .build();
    }

    /**
     * Creates a ClientHttpRequestFactory with configured timeouts.
     *
     * <p>Timeouts are read from CompaniesHouseProperties:
     * <ul>
     *   <li>connectTimeout: Time to establish connection</li>
     *   <li>readTimeout: Time to read response after connection established</li>
     * </ul>
     *
     * @return ClientHttpRequestFactory with timeout configuration
     */
    private ClientHttpRequestFactory clientHttpRequestFactory() {
        ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.DEFAULTS
                .withConnectTimeout(Duration.ofMillis(properties.getConnectTimeoutMs()))
                .withReadTimeout(Duration.ofMillis(properties.getReadTimeoutMs()));

        return ClientHttpRequestFactories.get(settings);
    }

    /**
     * Creates Basic authentication header value.
     *
     * <p>Companies House API requires Basic auth with the API key as username
     * and empty password. Format: "Basic base64(apiKey:)"
     *
     * @param apiKey the Companies House API key
     * @return Basic authentication header value
     */
    private String createBasicAuthHeader(String apiKey) {
        String credentials = apiKey + ":";
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());
        return "Basic " + encodedCredentials;
    }
}
