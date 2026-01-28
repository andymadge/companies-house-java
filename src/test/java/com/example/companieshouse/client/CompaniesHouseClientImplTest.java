package com.example.companieshouse.client;

import com.example.companieshouse.client.exception.InvalidResponseException;
import com.example.companieshouse.dto.response.CompanyProfileResponse;
import com.example.companieshouse.dto.response.RegisteredAddressResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), any(Object[].class)))
            .thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
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
}
