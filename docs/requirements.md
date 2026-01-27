# Companies House API Integration - Requirements Document

## Executive Summary

This integration provides a reusable Java Spring Boot client library for retrieving registered office addresses from UK companies using the Companies House Public Data API. The library encapsulates API communication, error handling, and data transformation, enabling clean integration into larger Spring Boot applications.

**Primary Use Case**: Given a valid UK company number, retrieve the company's registered office address with full address details.

**Scope**: Client library only (no REST controllers, no persistence layer). Designed for portability and integration into larger projects.

---

## API Analysis

### Endpoint Selection

**Selected Endpoint**: `GET /company/{companyNumber}`

**Rationale**:
- Provides **complete address fields** including `care_of` and `po_box` (not available in dedicated registered-office-address endpoint)
- Single endpoint call retrieves all needed address information
- Address data nested in `registered_office_address` object within response
- Enables potential future expansion to other company data without additional API calls

**Comparison**:
| Aspect | Company Profile | Registered Office Address |
|--------|-----------------|---------------------------|
| URL | `/company/{companyNumber}` | `/company/{companyNumber}/registered-office-address` |
| care_of field | ✅ | ❌ |
| po_box field | ✅ | ❌ |
| premises field | ✅ | ✅ |
| region field | ✅ | ✅ |
| Response Size | Larger (full company data) | Smaller (address only) |
| Extensibility | High (other company data available) | Limited (address only) |
| **Selected** | ✅ | ❌ |

### Endpoint Specification

**Endpoint**: Company Profile

**HTTP Method**: `GET`

**Base URL**: `https://api.company-information.service.gov.uk`

**Full URL Pattern**: `https://api.company-information.service.gov.uk/company/{companyNumber}`

**Path Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| companyNumber | string | Yes | The UK company number (e.g., "09370669") |

**Query Parameters**: None

**Success Response**:
- **Status Code**: 200 OK
- **Content-Type**: application/json
- **Body**: JSON object conforming to `companyProfile` resource schema
- **Address Data**: Nested in `registered_office_address` object
- **Address Fields** (from API documentation):
  - `address_line_1`: First line of street address (required)
  - `address_line_2`: Second line of street address (optional)
  - `postal_code`: UK postal code (required)
  - `locality`: City or locality name (optional)
  - `country`: Country name (optional)
  - `care_of`: Care of name for shared addresses (optional)
  - `po_box`: PO Box number (optional)
  - `premises`: Premises descriptor (optional)
  - `region`: Region/county name (optional)

**Error Responses**:
| Status Code | Error Type | Description | Handling |
|-------------|-----------|-------------|----------|
| 401 | Unauthorized | Invalid/missing API credentials | Throw `CompaniesHouseAuthenticationException`, check config |
| 404 | Not Found | Company number does not exist | Throw `CompanyNotFoundException`, company lookup failed |
| 429 | Too Many Requests | Rate limit exceeded | Throw `RateLimitExceededException`, implement backoff |
| 500 | Internal Server Error | Server-side error | Throw `CompaniesHouseApiException`, transient failure |

### Authentication

**Method**: API Key Authentication

**Supported Approaches** (from API docs):
1. **API Key as Query Parameter** (recommended for this implementation)
   - Parameter name: `key`
   - Example: `?key=YOUR_API_KEY`

2. **Bearer Token** (alternative)
   - Header: `Authorization: Bearer YOUR_TOKEN`

3. **Basic Authentication** (alternative)
   - Header: `Authorization: Basic base64(username:password)`

**Selected Approach**: API Key as query parameter with fallback to bearer token for flexibility

**Configuration**: API key provided via Spring configuration (`application.yml`), never hardcoded in source

### Rate Limits

**Rate Limit Details** (from API documentation):
- Standard rate limits apply (exact values not specified in fetched documentation)
- Rate limit status signaled via HTTP 429 status code
- Typically 600 requests per 5-minute window for public data (industry standard)

**Handling Strategy**:
- Detect 429 responses
- Throw `RateLimitExceededException`
- Caller can implement exponential backoff retry logic
- Consider rate limit headers in response for future enhancement

---

## Functional Requirements

### FR-1: Retrieve Registered Office Address for Valid Company

**Objective**: Client can successfully retrieve the complete registered office address for a valid UK company.

**Input**:
- Company number as string (e.g., "09370669")
- Format: 8 digits (standard UK company number format)

**Output**:
- `RegisteredAddressResponse` object containing:
  - Street address line 1 (required)
  - Street address line 2 (optional)
  - Postal code (required)
  - City/locality (optional)
  - Country (optional)
  - Care of name (optional)
  - PO Box (optional)

**Success Path**:
1. Client calls `getRegisteredAddress(companyNumber)`
2. Library validates company number format
3. HTTP GET request made to Companies House API
4. 200 response received
5. Response JSON parsed into `RegisteredAddressResponse` DTO
6. DTO returned to caller

**Error Handling**: See FR-3 for failure scenarios

**Example**:
```java
RegisteredAddressResponse address = client.getRegisteredAddress("09370669");
System.out.println(address.getAddressLine1()); // "Flat 8..."
System.out.println(address.getPostalCode()); // "SW1A 1AA"
```

### FR-2: Handle Non-Existent Companies

**Objective**: Gracefully handle requests for companies that don't exist or are not registered.

**Input**: Company number that does not exist in Companies House records

**Expected Behavior**:
1. HTTP request made to API
2. 404 Not Found response received
3. `CompanyNotFoundException` thrown with clear error message
4. Message includes: company number requested, that company was not found
5. Exception is RuntimeException (unchecked), not checked exception

**Specifics**:
- 404 is a permanent failure (not retryable)
- Caller should validate company number before retry
- Exception message: "Company not found: {companyNumber}"

**Example**:
```java
try {
    client.getRegisteredAddress("99999999");
} catch (CompanyNotFoundException e) {
    System.out.println(e.getMessage()); // "Company not found: 99999999"
}
```

### FR-3: Handle API Errors and Rate Limiting

**Objective**: Properly handle various API error scenarios and signal appropriate error conditions to caller.

**Error Scenarios**:

**3a. Rate Limit Exceeded (HTTP 429)**
- Throw `RateLimitExceededException`
- Message: "Rate limit exceeded. Retry after: {retryAfter} seconds" (if header available)
- Retryable error: Caller can implement exponential backoff
- Extract `Retry-After` header if present

**3b. Authentication Failure (HTTP 401)**
- Throw `CompaniesHouseAuthenticationException`
- Message: "Unauthorized - check API key configuration"
- Not retryable: Caller must fix API key configuration
- Check that API key is valid and not expired

**3c. Server Error (HTTP 500, 502, 503)**
- Throw `CompaniesHouseApiException` with HTTP status
- Message: "Companies House API error: HTTP {status}"
- Potentially retryable: Caller can decide retry strategy
- Log error with context but don't expose API key

**3d. Malformed Response**
- Throw `InvalidResponseException`
- Message: "Failed to parse API response: {reason}"
- Include HTTP status and response body in debug logs (no sensitive data)

### FR-4: Handle Network Failures and Timeouts

**Objective**: Handle network-level failures gracefully.

**Timeout Configuration**:
- Connection timeout: 5 seconds (time to establish TCP connection)
- Read timeout: 10 seconds (time to receive response from server)
- Configurable via `application.yml`

**Network Failure Scenarios**:

**4a. Connection Timeout**
- Throw `CompaniesHouseApiException` with "Connection timeout"
- Potentially retryable
- Indicates network issue or API server slow to respond

**4b. Read Timeout**
- Throw `CompaniesHouseApiException` with "Read timeout"
- Potentially retryable
- Indicates API server is slow or unresponsive

**4c. Connection Refused**
- Throw `CompaniesHouseApiException` with "Connection refused"
- Indicates API server is down
- Potentially retryable after delay

**4d. DNS Resolution Failure**
- Throw `CompaniesHouseApiException` with "Unable to resolve API host"
- Indicates network/DNS issue
- Potentially retryable

### FR-5: API Key Configuration

**Objective**: Support secure API key management without hardcoding credentials.

**Configuration Method**:
- API key configured in `application.yml` (template with placeholder)
- Actual key in `application-local.yml` (excluded from git via .gitignore)
- Can also be provided via environment variable: `COMPANIES_HOUSE_API_KEY`
- Environment variable takes precedence over application.yml

**Configuration Example** (`application.yml`):
```yaml
companies-house:
  api:
    base-url: https://api.company-information.service.gov.uk
    api-key: ${COMPANIES_HOUSE_API_KEY:PLACEHOLDER}
    connection-timeout-ms: 5000
    read-timeout-ms: 10000
```

**Configuration Example** (`application-local.yml`):
```yaml
companies-house:
  api:
    api-key: YOUR_ACTUAL_API_KEY_HERE
```

**Validation**:
- At startup, verify API key is not the placeholder value
- Log warning if API key appears to be invalid format
- Throw `InvalidConfigurationException` if API key is missing

### FR-6: Return Properly Formatted Data Transfer Objects

**Objective**: Ensure response data is returned in well-structured DTOs that are easy to work with.

**Requirements**:
- Response wrapped in `RegisteredAddressResponse` DTO
- All address fields accessible via simple getter methods
- Optional fields handled with `Optional<T>` (never null)
- DTO immutable (final fields, no setters)
- DTOs include sensible `equals()` and `hashCode()` implementations
- DTOs include `toString()` for debugging

**Example Usage**:
```java
RegisteredAddressResponse address = client.getRegisteredAddress("09370669");

// Required fields
String line1 = address.getAddressLine1();
String postcode = address.getPostalCode();

// Optional fields
Optional<String> line2 = address.getAddressLine2();
Optional<String> careOf = address.getCareOf();

// Never null, use Optional
line2.ifPresent(l2 -> System.out.println(l2));
```

---

## Non-Functional Requirements

### Performance Requirements

**Response Time Target**: Single address lookup should complete in < 2 seconds under normal network conditions
- Includes network latency to Companies House API
- Excludes initial configuration/application startup

**Throughput**: No specific requirement, but architecture should support reasonable concurrent usage
- No explicit throughput minimum
- RestTemplate connection pooling for efficiency
- No single-threaded blocking that would limit concurrency

**Optimization Considerations**:
- HTTP connection pooling (RestTemplate with PoolingHttpClientConnectionManager)
- Configurable timeouts to prevent indefinite hangs
- No unnecessary object creation or garbage collection pauses

### Security Requirements

**No Hardcoded Credentials**:
- ❌ API key never in source code
- ❌ API key never in git repository
- ✅ API key in environment variable or external config file (application-local.yml, .gitignored)
- ✅ Placeholder in application.yml with clear instructions

**HTTPS Enforcement**:
- All API calls must use HTTPS (not HTTP)
- Verify SSL certificates in production
- Certificate validation enabled by default in RestTemplate

**Sensitive Data Handling**:
- API key never logged to console or log files
- Company numbers may be logged (not sensitive PII)
- Error messages don't expose API key or full addresses in logs
- Debug logs stripped in production builds

**Authentication**:
- Supports standard Spring Security patterns for future integration
- No dependency on Spring Security in current implementation
- Can be integrated with Spring Security filters if needed
- Constructor injection of config (not static singletons)

### Reliability Requirements

**Error Handling Approach**:
- Specific exception types for different failure scenarios
- Exception hierarchy with `CompaniesHouseApiException` as base
- Exceptions include context (company number, HTTP status, cause)
- Stack traces captured for debugging

**Exception Type Mapping**:
| Exception Type | HTTP Status | Cause | Retryable |
|---|---|---|---|
| `CompanyNotFoundException` | 404 | Company not found | No |
| `RateLimitExceededException` | 429 | Rate limit hit | Yes |
| `CompaniesHouseAuthenticationException` | 401 | Auth failed | No (fix config) |
| `CompaniesHouseApiException` | 500+ | Server error | Yes (maybe) |
| `InvalidResponseException` | 200 (bad JSON) | Parse error | No |
| `CompaniesHouseApiException` | Timeout | Network timeout | Yes |

**Retry Strategy**:
- Responsibility of caller (library throws exceptions, caller decides retry)
- Exceptions clearly indicate whether retry makes sense
- Library doesn't implement automatic retry loops (keeps it simple)
- Caller can use Spring Retry, Resilience4j, or custom retry logic

**Circuit Breaker**:
- Not implemented in initial version
- Can be added at caller level using Resilience4j or similar
- Library is stateless (no circuit breaker state to maintain)

**Logging**:
- INFO level: Successful API calls (no sensitive data)
- WARN level: Retryable errors (timeouts, 429)
- ERROR level: Non-retryable errors (404, 401)
- DEBUG level: Request/response details (headers, status, no body data)
- Sensitive data (API key) never logged

### Testability Requirements

**Standalone Testing**:
- Library can be tested in isolation during development
- No external Companies House API calls needed in tests
- WireMock used to simulate API responses

**Mock-Friendly Design**:
- RestTemplate injected (not created with new)
- All dependencies constructor-injected
- No static state or singletons
- No @Autowired on fields
- Easy to mock for unit tests

**Unit Test Approach**:
- Mock RestTemplate
- Test business logic in isolation
- Fast test execution (< 50ms per test)
- No network calls in unit tests
- Test both success and error paths

**Integration Test Approach**:
- WireMock server simulates Companies House API
- Tests actual HTTP communication and JSON parsing
- Uses Spring Boot TestContext for configuration
- Runs locally without external API key
- Test data uses public company numbers (if any available)

**Test Data**:
- Use hardcoded company numbers in tests (e.g., "09370669")
- WireMock mocks response regardless of number
- No dependency on test data existing in real API

### Maintainability Requirements

**Code Organization**:
- Layered architecture: Config → Client → DTOs → Exceptions
- Single public interface: `CompaniesHouseClient`
- Clear separation of concerns
- Feature-based packages (not layer-based at root)

**Package Structure**:
```
com.example.companieshouse
├── client/               # API client interface and implementation
│   └── CompaniesHouseClient (interface)
│   └── CompaniesHouseClientImpl (implementation)
├── dto/                  # Data transfer objects
│   └── RegisteredAddressResponse
├── exception/            # Custom exception types
│   └── CompaniesHouseApiException
│   └── CompanyNotFoundException
│   └── RateLimitExceededException
│   └── CompaniesHouseAuthenticationException
│   └── InvalidResponseException
│   └── InvalidConfigurationException
└── config/               # Spring configuration
    └── CompaniesHouseClientConfig
```

**Documentation Standards**:
- JavaDoc on all public methods (interface and implementation)
- JavaDoc on all public classes
- No unnecessary comments on obvious code
- Clear error messages in exceptions
- README.md with setup and usage examples

**Extensibility**:
- Easy to add support for other endpoints (company profile, officers, etc.)
- DTOs follow same pattern (immutable, with Optional)
- Exception types can be extended without breaking change
- Configuration structure allows new properties
- Client interface can have new methods added

**Portability**:
- Designed to extract and integrate into other projects
- No dependency on specific Spring Boot beans or application context
- Can work standalone or as library in larger app
- No hardcoded package names
- Minimal external dependencies (only Spring, RestTemplate, Lombok)

### Configuration Requirements

**Externalized Configuration**:
- All configuration in `application.yml` or environment variables
- Configuration properties:
  - `companies-house.api.base-url`: API endpoint
  - `companies-house.api.api-key`: Authentication key
  - `companies-house.api.connection-timeout-ms`: Connection timeout
  - `companies-house.api.read-timeout-ms`: Read timeout

**Environment-Specific Configuration**:
- `application.yml`: Default configuration (dev)
- `application-prod.yml`: Production configuration (if different)
- `application-test.yml`: Test configuration
- Environment variable: `SPRING_PROFILES_ACTIVE=prod` or `SPRING_PROFILES_ACTIVE=test`

**Override Capability**:
- `application-local.yml`: Local development overrides (git-ignored)
- Environment variables: `COMPANIES_HOUSE_API_KEY` overrides config
- Spring Boot property precedence allows flexible override

**Validation**:
- At startup: Verify API key is configured and not placeholder
- At startup: Verify API base URL is valid HTTPS URL
- At startup: Verify timeouts are reasonable (> 0, < 60s)
- Throw exception if configuration invalid, prevent startup

---

## Data Model

### Input

**CompanyNumber (String)**:
- Format: 8-digit UK company number
- Examples: "09370669", "12345678"
- Validation: Must be exactly 8 digits (or leading zeros removed)
- Nullable: No, required parameter

### Output - Success Response

**RegisteredAddressResponse** (Immutable DTO):

```java
public class RegisteredAddressResponse {
    private final String addressLine1;              // Required
    private final Optional<String> addressLine2;   // Optional
    private final String postalCode;               // Required
    private final Optional<String> locality;       // Optional (city/town)
    private final Optional<String> country;        // Optional
    private final Optional<String> careOf;         // Optional
    private final Optional<String> poBox;          // Optional
    private final Optional<String> premises;       // Optional (premises descriptor)
    private final Optional<String> region;         // Optional (county/region)

    // Constructor (all-args)
    // Getters for all fields
    // equals() based on all fields
    // hashCode() based on all fields
    // toString() with all fields
}
```

**Field Definitions**:
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| addressLine1 | String | Yes | First line of street address |
| addressLine2 | Optional<String> | No | Second line of address |
| postalCode | String | Yes | UK postal code (e.g., "SW1A 1AA") |
| locality | Optional<String> | No | City or town name |
| country | Optional<String> | No | Country name |
| careOf | Optional<String> | No | "Care of" name (for shared addresses) |
| poBox | Optional<String> | No | PO Box number if applicable |
| premises | Optional<String> | No | Premises descriptor (e.g., "Suite 1") |
| region | Optional<String> | No | Region or county name |

### Output - Error Response Mapping

Errors are signaled via exceptions, not response objects. See Exception Types below.

### Exception Types

All exceptions extend `RuntimeException` (unchecked exceptions for cleaner API).

**Exception Hierarchy**:
```
RuntimeException
└── CompaniesHouseApiException (base exception for all API-related errors)
    ├── CompanyNotFoundException (404 - company not found)
    ├── RateLimitExceededException (429 - rate limit exceeded)
    ├── CompaniesHouseAuthenticationException (401 - auth failed)
    ├── InvalidResponseException (200 but invalid JSON)
    └── InvalidConfigurationException (config validation failed)
```

**Exception Details**:

**CompaniesHouseApiException** (Base):
- Extends: `RuntimeException`
- Purpose: Catch-all for all API-related errors
- Constructor: `CompaniesHouseApiException(String message, Throwable cause)`
- Usage: Timeouts, 500 errors, connection refused, DNS failure
- Example: `"Companies House API error: Connection timeout"`

**CompanyNotFoundException** (404):
- Extends: `CompaniesHouseApiException`
- Purpose: Company number not found in Companies House records
- Constructor: `CompanyNotFoundException(String companyNumber)`
- Usage: 404 response from API
- Message Format: `"Company not found: {companyNumber}"`
- Retryable: No
- Example: `throw new CompanyNotFoundException("09370669");`

**RateLimitExceededException** (429):
- Extends: `CompaniesHouseApiException`
- Purpose: Rate limit exceeded for API key
- Constructor: `RateLimitExceededException(String message, Long retryAfter)`
- Usage: 429 response from API
- Message Format: `"Rate limit exceeded. Retry after: {retryAfter} seconds"`
- Retryable: Yes
- Includes: Suggested retry delay (from `Retry-After` header)
- Example: `throw new RateLimitExceededException("Rate limit exceeded", 60L);`

**CompaniesHouseAuthenticationException** (401):
- Extends: `CompaniesHouseApiException`
- Purpose: Authentication failed (invalid API key)
- Constructor: `CompaniesHouseAuthenticationException(String message)`
- Usage: 401 response from API
- Message Format: `"Unauthorized - check API key configuration"`
- Retryable: No (must fix config)
- Example: `throw new CompaniesHouseAuthenticationException("API key is invalid");`

**InvalidResponseException**:
- Extends: `CompaniesHouseApiException`
- Purpose: API returned 200 but response is not valid JSON or malformed
- Constructor: `InvalidResponseException(String message, Throwable cause)`
- Usage: JSON parsing error, missing required fields
- Message Format: `"Failed to parse API response: {reason}"`
- Retryable: No
- Example: `throw new InvalidResponseException("Missing required field: address_line_1");`

**InvalidConfigurationException**:
- Extends: `RuntimeException`
- Purpose: Configuration validation failed at startup
- Constructor: `InvalidConfigurationException(String message)`
- Usage: Missing API key, invalid URL, bad timeout value
- Message Format: `"Invalid configuration: {reason}"`
- Prevents: Application startup
- Example: `throw new InvalidConfigurationException("API key not configured");`

---

## Acceptance Criteria

### Functional Acceptance Criteria

- [ ] **AC-F1**: Successfully retrieve registered address for valid company number "09370669"
  - Given a valid company number
  - When `getRegisteredAddress(companyNumber)` is called
  - Then a `RegisteredAddressResponse` is returned
  - And all required fields (addressLine1, postalCode) are non-empty
  - And optional fields are wrapped in `Optional` (never null)

- [ ] **AC-F2**: Throw `CompanyNotFoundException` for non-existent company
  - Given a company number that doesn't exist
  - When `getRegisteredAddress(invalidNumber)` is called
  - Then `CompanyNotFoundException` is thrown
  - And exception message includes the company number that was not found

- [ ] **AC-F3**: Throw `RateLimitExceededException` for HTTP 429
  - Given rate limit is exceeded
  - When API returns 429
  - Then `RateLimitExceededException` is thrown
  - And exception includes retry-after delay if available

- [ ] **AC-F4**: Throw `CompaniesHouseAuthenticationException` for HTTP 401
  - Given authentication credentials are invalid
  - When API returns 401
  - Then `CompaniesHouseAuthenticationException` is thrown
  - And message indicates to check API key configuration

- [ ] **AC-F5**: Throw `CompaniesHouseApiException` for HTTP 500
  - Given server returns 500 error
  - When API returns 500
  - Then `CompaniesHouseApiException` is thrown
  - And message includes "HTTP 500" or similar

- [ ] **AC-F6**: Throw `CompaniesHouseApiException` for connection timeout
  - Given connection times out after 5 seconds
  - When timeout occurs during connection
  - Then `CompaniesHouseApiException` is thrown
  - And message indicates timeout

- [ ] **AC-F7**: Throw `CompaniesHouseApiException` for read timeout
  - Given no response from server after 10 seconds
  - When timeout occurs during read
  - Then `CompaniesHouseApiException` is thrown
  - And message indicates timeout

- [ ] **AC-F8**: Throw `InvalidResponseException` for malformed JSON
  - Given API returns 200 but invalid JSON
  - When response cannot be parsed
  - Then `InvalidResponseException` is thrown
  - And message indicates JSON parsing error

- [ ] **AC-F9**: Reject invalid company numbers with clear error
  - Given invalid company number format (not 8 digits)
  - When `getRegisteredAddress(invalidFormat)` is called
  - Then `IllegalArgumentException` or similar is thrown
  - And message indicates valid format requirement

- [ ] **AC-F10**: API key configurable without hardcoding
  - Given `application.yml` with `companies-house.api-key: PLACEHOLDER`
  - When application reads `application-local.yml` with actual key
  - Then actual key is used (environment variable takes precedence)
  - And source code contains no actual API keys

- [ ] **AC-F11**: API key can be provided via environment variable
  - Given environment variable `COMPANIES_HOUSE_API_KEY=actual_key`
  - When Spring Boot reads properties
  - Then environment variable value is used
  - And takes precedence over `application.yml`

### Non-Functional Acceptance Criteria

- [ ] **AC-NF1**: All API calls use HTTPS
  - Given any API request
  - When HTTP request is made
  - Then only HTTPS is used (not HTTP)
  - And SSL certificates are validated

- [ ] **AC-NF2**: API key never logged
  - Given any log output
  - When application logs messages
  - Then API key is never included in logs
  - And sensitive headers are redacted

- [ ] **AC-NF3**: Response time for single lookup < 2 seconds
  - Given normal network conditions
  - When `getRegisteredAddress(companyNumber)` is called
  - Then call completes in < 2 seconds (on average)
  - And includes network latency to API

- [ ] **AC-NF4**: Proper exception hierarchy
  - Given any error scenario
  - When exception is thrown
  - Then it's appropriate type (`CompanyNotFoundException`, `RateLimitExceededException`, etc.)
  - And base type is `CompaniesHouseApiException` or `RuntimeException`

- [ ] **AC-NF5**: Constructor injection used throughout
  - Given any Spring component
  - When classes are reviewed
  - Then all dependencies are constructor-injected
  - And no @Autowired on fields
  - And @RequiredArgsConstructor used where appropriate

- [ ] **AC-NF6**: Proper generics (no raw types)
  - Given any generic type usage
  - When code is reviewed
  - Then all generics are properly specified (e.g., `List<String>`)
  - And no raw types like `List` without `<T>`

- [ ] **AC-NF7**: No hardcoded configuration values
  - Given any configuration
  - When source code is reviewed
  - Then no hardcoded URLs, keys, timeouts in code
  - And all config is in `application.yml`

- [ ] **AC-NF8**: Custom exceptions are unchecked (RuntimeException)
  - Given any custom exception
  - When exception is defined
  - Then it extends `RuntimeException` (not checked exception)
  - And doesn't require `throws` declarations

### Testing Acceptance Criteria

- [ ] **AC-T1**: 80%+ code coverage on client implementation
  - Given test suite execution
  - When JaCoCo coverage report is generated
  - Then overall coverage is >= 80%
  - And client implementation is >= 80%

- [ ] **AC-T2**: 90%+ coverage on `CompaniesHouseClientImpl`
  - Given unit test suite
  - When coverage report is generated
  - Then `CompaniesHouseClientImpl` has >= 90% coverage
  - And both success and error paths are tested

- [ ] **AC-T3**: 100% coverage on DTOs
  - Given DTO classes
  - When coverage report is generated
  - Then all DTO classes have 100% coverage
  - And equals(), hashCode(), toString() are tested

- [ ] **AC-T4**: 100% coverage on exception classes
  - Given exception classes
  - When coverage report is generated
  - Then all exception classes have 100% coverage
  - And all constructors are tested

- [ ] **AC-T5**: Unit tests mock HTTP calls
  - Given any unit test
  - When test executes
  - Then no real HTTP calls are made
  - And RestTemplate is mocked
  - And test execution is fast (< 50ms per test)

- [ ] **AC-T6**: Integration tests use WireMock
  - Given integration test suite
  - When test executes
  - Then WireMock simulates Companies House API
  - And actual response format is tested
  - And JSON parsing is tested

- [ ] **AC-T7**: All error paths tested
  - Given error scenarios (404, 429, 401, 500, timeout, etc.)
  - When test suite executes
  - Then each error scenario has at least one test
  - And appropriate exception is thrown
  - And error message is validated

- [ ] **AC-T8**: Edge cases tested
  - Given edge cases (empty input, null values, missing fields)
  - When test suite executes
  - Then edge cases are covered
  - And appropriate validation/exception occurs

### Documentation Acceptance Criteria

- [ ] **AC-D1**: JavaDoc on all public methods
  - Given `CompaniesHouseClient` interface
  - When code is reviewed
  - Then all public methods have JavaDoc
  - And description includes what method does
  - And @param and @return documented

- [ ] **AC-D2**: JavaDoc on all public classes
  - Given all public classes
  - When code is reviewed
  - Then all have class-level JavaDoc
  - And purpose is clear from JavaDoc

- [ ] **AC-D3**: README.md with setup instructions
  - Given `README.md`
  - When file is reviewed
  - Then it includes Maven dependency or build instructions
  - And configuration steps are documented
  - And example of setting API key is shown

- [ ] **AC-D4**: README.md with usage examples
  - Given `README.md`
  - When file is reviewed
  - Then it includes code example showing how to use client
  - And both success and error handling examples shown

- [ ] **AC-D5**: Error handling documented
  - Given documentation
  - When error handling is reviewed
  - Then all exception types are documented
  - And when each exception is thrown is clear
  - And how caller should handle each is explained

- [ ] **AC-D6**: Configuration documented
  - Given documentation or README
  - When configuration is reviewed
  - Then all configuration properties are documented
  - And format (application.yml) is shown
  - And environment variables are documented

---

## Next Steps

This requirements document feeds into **Prompt 02: Architecture Design**, which will:

1. Read this requirements document
2. Reference the STD-003 Java Spring Boot Development Rubric
3. Design the component structure and interactions
4. Create 7 Architecture Decision Records (ADRs)
5. Produce `docs/architecture.md`

The architecture document will specify:
- Package structure and component responsibilities
- Class diagrams and interactions
- Dependency injection patterns
- Configuration approach
- Testing strategy
- Architecture Decision Records (ADRs) explaining key choices

---

**Document Version**: 1.0
**Last Updated**: 2026-01-27
**Status**: Complete - Ready for Architecture Design phase
