package com.example.companieshouse.client;

import com.example.companieshouse.client.exception.CompaniesHouseApiException;
import com.example.companieshouse.client.exception.CompaniesHouseAuthenticationException;
import com.example.companieshouse.client.exception.CompanyNotFoundException;
import com.example.companieshouse.client.exception.InvalidResponseException;
import com.example.companieshouse.client.exception.RateLimitExceededException;
import com.example.companieshouse.dto.response.CompanyProfileResponse;
import com.example.companieshouse.dto.response.RegisteredAddressResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link CompaniesHouseClientImpl}.
 * <p>
 * Tests use Mockito to mock RestClient interactions and verify the client
 * correctly calls the Companies House API and handles responses.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CompaniesHouseClientImpl Unit Tests")
class CompaniesHouseClientImplTest {

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @InjectMocks
    private CompaniesHouseClientImpl client;

    @BeforeEach
    void setUp() {
        // Set up the mock chain: restClient.get().uri().retrieve().body()
        // Use lenient() to avoid unnecessary stubbing warnings for tests that don't use mocks
        lenient().when(restClient.get()).thenReturn(requestHeadersUriSpec);
        lenient().when(requestHeadersUriSpec.uri(anyString(), any(Object[].class)))
            .thenReturn(requestHeadersUriSpec);
        lenient().when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
    }

    @Test
    @DisplayName("Should return registered address for valid company number")
    void shouldReturnRegisteredAddressForValidCompany() {
        // Arrange
        String companyNumber = "09370669";
        RegisteredAddressResponse expectedAddress = RegisteredAddressResponse.builder()
            .addressLine1("123 High Street")
            .postalCode("SW1A 1AA")
            .locality("London")
            .country("United Kingdom")
            .build();

        CompanyProfileResponse profileResponse = CompanyProfileResponse.builder()
            .companyNumber(companyNumber)
            .companyName("Test Company Ltd")
            .registeredOfficeAddress(expectedAddress)
            .build();

        when(responseSpec.body(CompanyProfileResponse.class)).thenReturn(profileResponse);

        // Act
        RegisteredAddressResponse result = client.getRegisteredAddress(companyNumber);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getAddressLine1()).isEqualTo("123 High Street");
        assertThat(result.getPostalCode()).isEqualTo("SW1A 1AA");
        assertThat(result.getLocality()).isEqualTo("London");
        assertThat(result.getCountry()).isEqualTo("United Kingdom");
    }

    @Test
    @DisplayName("Should make GET request to correct endpoint with company number")
    void shouldMakeCorrectApiCall() {
        // Arrange
        String companyNumber = "09370669";
        RegisteredAddressResponse address = RegisteredAddressResponse.builder()
            .addressLine1("Test Street")
            .build();

        CompanyProfileResponse profileResponse = CompanyProfileResponse.builder()
            .companyNumber(companyNumber)
            .registeredOfficeAddress(address)
            .build();

        when(responseSpec.body(CompanyProfileResponse.class)).thenReturn(profileResponse);

        // Act
        client.getRegisteredAddress(companyNumber);

        // Assert
        verify(restClient).get();
        verify(requestHeadersUriSpec).uri("/company/{companyNumber}", companyNumber);
        verify(requestHeadersUriSpec).retrieve();
        verify(responseSpec).body(CompanyProfileResponse.class);
    }

    @Test
    @DisplayName("Should extract registered office address from company profile")
    void shouldExtractAddressFromProfile() {
        // Arrange
        String companyNumber = "09370669";
        RegisteredAddressResponse expectedAddress = RegisteredAddressResponse.builder()
            .addressLine1("456 Business Park")
            .addressLine2("Suite 100")
            .postalCode("EC1A 1BB")
            .locality("Manchester")
            .country("England")
            .premises("Building A")
            .region("Greater Manchester")
            .build();

        CompanyProfileResponse profileResponse = CompanyProfileResponse.builder()
            .companyNumber(companyNumber)
            .companyName("Another Company")
            .registeredOfficeAddress(expectedAddress)
            .build();

        when(responseSpec.body(CompanyProfileResponse.class)).thenReturn(profileResponse);

        // Act
        RegisteredAddressResponse result = client.getRegisteredAddress(companyNumber);

        // Assert
        assertThat(result).isSameAs(expectedAddress);
        assertThat(result.getAddressLine1()).isEqualTo("456 Business Park");
        assertThat(result.getAddressLine2()).isEqualTo("Suite 100");
        assertThat(result.getPremises()).isEqualTo("Building A");
        assertThat(result.getRegion()).isEqualTo("Greater Manchester");
    }

    @Test
    @DisplayName("Should throw InvalidResponseException when profile response is null")
    void shouldThrowWhenProfileIsNull() {
        // Arrange
        String companyNumber = "09370669";
        when(responseSpec.body(CompanyProfileResponse.class)).thenReturn(null);

        // Act & Assert
        InvalidResponseException exception = assertThrows(
            InvalidResponseException.class,
            () -> client.getRegisteredAddress(companyNumber)
        );

        assertThat(exception.getMessage()).contains("09370669");
        assertThat(exception.getMessage()).contains("null response");
    }

    @Test
    @DisplayName("Should throw InvalidResponseException when registered address is null")
    void shouldThrowWhenAddressIsNull() {
        // Arrange
        String companyNumber = "09370669";
        CompanyProfileResponse profileResponse = CompanyProfileResponse.builder()
            .companyNumber(companyNumber)
            .companyName("Test Company")
            .registeredOfficeAddress(null)
            .build();

        when(responseSpec.body(CompanyProfileResponse.class)).thenReturn(profileResponse);

        // Act & Assert
        InvalidResponseException exception = assertThrows(
            InvalidResponseException.class,
            () -> client.getRegisteredAddress(companyNumber)
        );

        assertThat(exception.getMessage()).contains("09370669");
        assertThat(exception.getMessage()).contains("No registered address");
    }

    // ==================== Error Handling Tests ====================

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Should throw IllegalArgumentException for null or blank company number")
    void shouldThrowForInvalidCompanyNumber(String invalidNumber) {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> client.getRegisteredAddress(invalidNumber)
        );

        assertThat(exception.getMessage()).contains("Company number");
        assertThat(exception.getMessage()).containsAnyOf("null", "blank");
    }

    @Test
    @DisplayName("Should throw CompanyNotFoundException for HTTP 404")
    void shouldThrowCompanyNotFoundFor404() {
        // Arrange
        String companyNumber = "99999999";
        HttpClientErrorException notFoundException = new HttpClientErrorException(
            HttpStatus.NOT_FOUND, "Not Found");

        when(responseSpec.body(CompanyProfileResponse.class))
            .thenThrow(notFoundException);

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
    void shouldThrowRateLimitExceededFor429() {
        // Arrange
        String companyNumber = "09370669";
        HttpHeaders headers = new HttpHeaders();
        headers.add("Retry-After", "60");

        HttpClientErrorException rateLimitException = HttpClientErrorException.create(
            HttpStatus.TOO_MANY_REQUESTS, "Too Many Requests", headers, null, null);

        when(responseSpec.body(CompanyProfileResponse.class))
            .thenThrow(rateLimitException);

        // Act & Assert
        RateLimitExceededException exception = assertThrows(
            RateLimitExceededException.class,
            () -> client.getRegisteredAddress(companyNumber)
        );

        assertThat(exception.getMessage()).containsIgnoringCase("rate limit");
        assertThat(exception.getRetryAfter()).isEqualTo(60L);
    }

    @Test
    @DisplayName("Should handle HTTP 429 with missing Retry-After header")
    void shouldHandleRateLimitWithoutRetryAfter() {
        // Arrange
        String companyNumber = "09370669";
        HttpClientErrorException rateLimitException = new HttpClientErrorException(
            HttpStatus.TOO_MANY_REQUESTS, "Too Many Requests");

        when(responseSpec.body(CompanyProfileResponse.class))
            .thenThrow(rateLimitException);

        // Act & Assert
        RateLimitExceededException exception = assertThrows(
            RateLimitExceededException.class,
            () -> client.getRegisteredAddress(companyNumber)
        );

        assertThat(exception.getRetryAfter()).isNull();
    }

    @Test
    @DisplayName("Should handle malformed Retry-After header gracefully")
    void shouldHandleMalformedRetryAfter() {
        // Arrange
        String companyNumber = "09370669";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Retry-After", "not-a-number");  // Non-numeric value triggers NumberFormatException

        HttpClientErrorException exception = new HttpClientErrorException(
            HttpStatus.TOO_MANY_REQUESTS, "Rate Limited", headers, null, null
        );
        when(responseSpec.body(CompanyProfileResponse.class)).thenThrow(exception);

        // Act & Assert
        RateLimitExceededException rateLimitException = assertThrows(
            RateLimitExceededException.class,
            () -> client.getRegisteredAddress(companyNumber)
        );

        assertThat(rateLimitException.getMessage()).containsIgnoringCase("rate limit");
        assertThat(rateLimitException.getRetryAfter()).isNull(); // Graceful degradation
    }

    @Test
    @DisplayName("Should throw CompaniesHouseAuthenticationException for HTTP 401")
    void shouldThrowAuthenticationExceptionFor401() {
        // Arrange
        String companyNumber = "09370669";
        HttpClientErrorException authException = new HttpClientErrorException(
            HttpStatus.UNAUTHORIZED, "Unauthorized");

        when(responseSpec.body(CompanyProfileResponse.class))
            .thenThrow(authException);

        // Act & Assert
        CompaniesHouseAuthenticationException exception = assertThrows(
            CompaniesHouseAuthenticationException.class,
            () -> client.getRegisteredAddress(companyNumber)
        );

        assertThat(exception.getMessage()).containsIgnoringCase("authentication");
    }

    @Test
    @DisplayName("Should throw CompaniesHouseApiException for HTTP 500")
    void shouldThrowApiExceptionFor500() {
        // Arrange
        String companyNumber = "09370669";
        HttpServerErrorException serverException = new HttpServerErrorException(
            HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error");

        when(responseSpec.body(CompanyProfileResponse.class))
            .thenThrow(serverException);

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
    void shouldThrowApiExceptionFor502() {
        // Arrange
        String companyNumber = "09370669";
        HttpServerErrorException serverException = new HttpServerErrorException(
            HttpStatus.BAD_GATEWAY, "Bad Gateway");

        when(responseSpec.body(CompanyProfileResponse.class))
            .thenThrow(serverException);

        // Act & Assert
        CompaniesHouseApiException exception = assertThrows(
            CompaniesHouseApiException.class,
            () -> client.getRegisteredAddress(companyNumber)
        );

        assertThat(exception.getMessage()).contains("502");
    }

    @Test
    @DisplayName("Should throw API exception for HTTP 503 Service Unavailable")
    void shouldThrowApiExceptionFor503() {
        // Arrange
        String companyNumber = "09370669";

        HttpServerErrorException exception = new HttpServerErrorException(
            HttpStatus.SERVICE_UNAVAILABLE, "Service Unavailable"
        );
        when(responseSpec.body(CompanyProfileResponse.class)).thenThrow(exception);

        // Act & Assert
        CompaniesHouseApiException apiException = assertThrows(
            CompaniesHouseApiException.class,
            () -> client.getRegisteredAddress(companyNumber)
        );

        assertThat(apiException.getMessage()).contains("503");
        assertThat(apiException.getMessage()).containsIgnoringCase("server error");
    }

    @Test
    @DisplayName("Should throw CompaniesHouseApiException for timeout")
    void shouldThrowApiExceptionForTimeout() {
        // Arrange
        String companyNumber = "09370669";
        ResourceAccessException timeoutException = new ResourceAccessException(
            "I/O error: Connection timed out");

        when(responseSpec.body(CompanyProfileResponse.class))
            .thenThrow(timeoutException);

        // Act & Assert
        CompaniesHouseApiException exception = assertThrows(
            CompaniesHouseApiException.class,
            () -> client.getRegisteredAddress(companyNumber)
        );

        assertThat(exception.getMessage()).containsAnyOf("Connection", "timed out", "Failed to connect");
        assertThat(exception.getCause()).isInstanceOf(ResourceAccessException.class);
    }

    @Test
    @DisplayName("Should throw InvalidResponseException for parse errors")
    void shouldThrowInvalidResponseForParseError() {
        // Arrange
        String companyNumber = "09370669";
        RestClientException parseException = new RestClientException(
            "Could not extract response: JSON parse error");

        when(responseSpec.body(CompanyProfileResponse.class))
            .thenThrow(parseException);

        // Act & Assert
        InvalidResponseException exception = assertThrows(
            InvalidResponseException.class,
            () -> client.getRegisteredAddress(companyNumber)
        );

        assertThat(exception.getMessage()).containsIgnoringCase("parse");
        assertThat(exception.getCause()).isInstanceOf(RestClientException.class);
    }

    @Test
    @DisplayName("Should throw CompaniesHouseApiException for other 4xx errors")
    void shouldThrowApiExceptionForOther4xxErrors() {
        // Arrange
        String companyNumber = "09370669";
        HttpClientErrorException clientException = new HttpClientErrorException(
            HttpStatus.BAD_REQUEST, "Bad Request");

        when(responseSpec.body(CompanyProfileResponse.class))
            .thenThrow(clientException);

        // Act & Assert
        CompaniesHouseApiException exception = assertThrows(
            CompaniesHouseApiException.class,
            () -> client.getRegisteredAddress(companyNumber)
        );

        assertThat(exception.getMessage()).containsAnyOf("400", "Client error");
    }
}
