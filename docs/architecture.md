# Companies House API Integration - Architecture Design

## Overview

This document defines the architecture for a Java Spring Boot client library that retrieves registered office addresses from the UK Companies House Public Data API. The library is designed as a portable, reusable component that can be integrated into larger Spring Boot applications.

**Key Characteristics:**
- **Type**: Client library (no REST controllers)
- **HTTP Client**: RestClient (Spring 6.1+)
- **API Style**: Synchronous
- **Portability**: Self-contained package structure for easy extraction
- **Testing**: Unit tests with mocks, integration tests with WireMock

---

## Component Architecture

### Components

#### 1. CompaniesHouseConfig (@Configuration)
**Responsibility**: Configure the RestClient bean for Companies House API calls.

- Creates `RestClient` bean with proper timeouts
- Configures base URL from properties
- Sets up request interceptor for API key injection
- Enforces HTTPS

```java
@Configuration
@EnableConfigurationProperties(CompaniesHouseProperties.class)
public class CompaniesHouseConfig {
    // RestClient bean with timeout and base URL configuration
}
```

#### 2. CompaniesHouseProperties (@ConfigurationProperties)
**Responsibility**: Hold externalized configuration values.

- API base URL
- API key (from environment or application-local.yml)
- Connection timeout (default: 5000ms)
- Read timeout (default: 10000ms)
- Validation annotations ensure valid configuration at startup

```java
@ConfigurationProperties(prefix = "companies-house.api")
@Data
@Validated
public class CompaniesHouseProperties {
    @NotBlank private String baseUrl;
    @NotBlank private String apiKey;
    @Positive private int connectTimeoutMs = 5000;
    @Positive private int readTimeoutMs = 10000;
}
```

#### 3. CompaniesHouseClient (Interface)
**Responsibility**: Define the public API for Companies House integration.

```java
public interface CompaniesHouseClient {
    /**
     * Retrieve the registered office address for a company.
     *
     * @param companyNumber the UK company number (8 digits)
     * @return the registered address, or empty if not found
     * @throws CompanyNotFoundException if company does not exist (404)
     * @throws RateLimitExceededException if rate limit exceeded (429)
     * @throws CompaniesHouseAuthenticationException if auth fails (401)
     * @throws CompaniesHouseApiException for other API errors
     */
    RegisteredAddressResponse getRegisteredAddress(String companyNumber);
}
```

#### 4. CompaniesHouseClientImpl (@Component)
**Responsibility**: Implement HTTP calls to Companies House API using RestClient.

- Uses RestClient for HTTP communication
- Validates company number format
- Maps HTTP errors to specific exceptions using `.onStatus()` handlers
- Parses JSON response into DTOs

```java
@Component
@RequiredArgsConstructor
public class CompaniesHouseClientImpl implements CompaniesHouseClient {
    private final RestClient restClient;
    private final CompaniesHouseProperties properties;

    @Override
    public RegisteredAddressResponse getRegisteredAddress(String companyNumber) {
        // Validation, HTTP call with error handling, response mapping
    }
}
```

#### 5. DTOs (Data Transfer Objects)
**Responsibility**: Map API responses to Java objects.

**RegisteredAddressResponse** - Success response containing address fields:
- addressLine1 (required)
- addressLine2 (optional)
- postalCode (required)
- locality, country, careOf, poBox, premises, region (all optional)

**ApiErrorResponse** - Error structure from Companies House API.

#### 6. Custom Exceptions
**Responsibility**: Provide specific, informative exceptions for each error scenario.

All exceptions extend `RuntimeException` (unchecked) for clean API usage.

### Component Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              User Code                                       │
└─────────────────────────────────────────────────────────────────────────────┘
                                      │
                                      ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                     CompaniesHouseClient (interface)                         │
│                                                                              │
│   + getRegisteredAddress(companyNumber: String): RegisteredAddressResponse   │
└─────────────────────────────────────────────────────────────────────────────┘
                                      │
                                      ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                    CompaniesHouseClientImpl (@Component)                     │
│                                                                              │
│   Dependencies:                                                              │
│   ├── RestClient (from CompaniesHouseConfig)                                │
│   └── CompaniesHouseProperties (@ConfigurationProperties)                   │
│                                                                              │
│   Throws:                                                                    │
│   ├── CompanyNotFoundException (404)                                        │
│   ├── RateLimitExceededException (429)                                      │
│   ├── CompaniesHouseAuthenticationException (401)                           │
│   ├── InvalidResponseException (parse error)                                │
│   └── CompaniesHouseApiException (other errors)                             │
└─────────────────────────────────────────────────────────────────────────────┘
                                      │
                                      ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                              DTOs                                            │
│                                                                              │
│   RegisteredAddressResponse    ApiErrorResponse                              │
│   └── addressLine1             └── errorCode                                │
│   └── addressLine2             └── message                                  │
│   └── postalCode               └── type                                     │
│   └── locality                                                              │
│   └── country                                                               │
│   └── careOf                                                                │
│   └── poBox                                                                 │
│   └── premises                                                              │
│   └── region                                                                │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Component Responsibilities

| Component | Purpose | Key Methods | Dependencies |
|-----------|---------|-------------|--------------|
| CompaniesHouseClient | Public API interface | getRegisteredAddress() | (none - interface) |
| CompaniesHouseClientImpl | HTTP call handler | getRegisteredAddress() | RestClient, CompaniesHouseProperties |
| CompaniesHouseConfig | Spring configuration | restClient() | CompaniesHouseProperties |
| CompaniesHouseProperties | Config properties | getters/setters | (Spring) |
| RegisteredAddressResponse | Success DTO | getters | (none) |
| ApiErrorResponse | Error DTO | getters | (none) |
| CompaniesHouseApiException | Base exception | constructor | (none) |
| CompanyNotFoundException | 404 exception | constructor | CompaniesHouseApiException |
| RateLimitExceededException | 429 exception | constructor | CompaniesHouseApiException |
| CompaniesHouseAuthenticationException | 401 exception | constructor | CompaniesHouseApiException |
| InvalidResponseException | Parse error | constructor | CompaniesHouseApiException |
| InvalidConfigurationException | Config error | constructor | RuntimeException |

---

## Architecture Decision Records (ADRs)

### ADR-001: HTTP Client Library

**Context:**
We need to make HTTP calls to the Companies House API. Spring Boot offers multiple options for HTTP communication.

**Decision:**
Use **RestClient** (Spring Framework 6.1+, Spring Boot 3.2+)

**Rationale:**
- RestClient is the modern, recommended HTTP client for synchronous operations in Spring 6.1+
- Fluent API makes code more readable than RestTemplate
- Built-in error handling via `.onStatus()` maps cleanly to our exception requirements
- RestTemplate is in maintenance mode and no longer recommended for new projects
- RestClient shares the same underlying infrastructure as RestTemplate but with improved API
- Simpler to configure and test than WebClient (reactive)

**Consequences:**
- Requires Spring Boot 3.2+ (Spring Framework 6.1+)
- Synchronous calls (blocks until response)
- Modern API patterns, future-proof choice
- Some developers may be less familiar (newer than RestTemplate)

**Alternatives Considered:**
- **RestTemplate**: Legacy, in maintenance mode. Not recommended for new projects
- **WebClient**: Reactive/async, adds complexity for synchronous use case
- **Apache HttpClient**: Lower level, requires more configuration
- **OkHttp**: Third-party dependency, unnecessary when Spring provides RestClient

### ADR-002: Synchronous vs Asynchronous API

**Context:**
Client applications need to retrieve company addresses. Should the API be blocking or non-blocking?

**Decision:**
Use **synchronous (blocking) API**

**Rationale:**
- Most applications need the address immediately before proceeding (blocking is natural)
- Synchronous is simpler to understand, use, and debug
- Matches typical REST client usage patterns
- Can be wrapped in async later if needed (CompletableFuture)
- Requirements specify RestClient, which is synchronous by design

**Consequences:**
- Caller's thread blocks until response received
- Simple programming model
- Not suitable for extreme high-concurrency scenarios (but adequate for most use cases)
- Easy to integrate into existing synchronous codebases

**Alternatives Considered:**
- **CompletableFuture return types**: Adds complexity, not needed for typical use
- **Reactive streams (WebClient)**: Overkill for simple synchronous lookups

### ADR-003: Error Handling Strategy

**Context:**
Companies House API returns various HTTP error codes (404, 429, 401, 500, etc.). How should we handle and communicate these to callers?

**Decision:**
Use **custom exceptions extending RuntimeException**, with specific exception types for each error category. Leverage RestClient's `.onStatus()` handlers for clean error mapping.

**Rationale:**
- RuntimeException (unchecked) means callers can choose whether to handle
- Specific exception types (CompanyNotFoundException, RateLimitExceededException) enable precise handling
- Each exception includes context (company number, HTTP status, retry-after header)
- RestClient's `.onStatus()` fluent API maps HTTP status codes directly to exceptions
- Clean separation: HTTP errors become domain exceptions

**Exception Mapping:**
| HTTP Status | Exception | Retryable |
|-------------|-----------|-----------|
| 404 | CompanyNotFoundException | No |
| 429 | RateLimitExceededException | Yes |
| 401 | CompaniesHouseAuthenticationException | No |
| 500/502/503 | CompaniesHouseApiException | Maybe |
| Timeout | CompaniesHouseApiException | Yes |
| Parse error | InvalidResponseException | No |

**Consequences:**
- Caller must understand exception hierarchy
- Clear contract about what can go wrong
- Enables retry logic based on exception type
- RestClient error handling code is clean and readable

**Alternatives Considered:**
- **Return error codes in response object**: Verbose, ambiguous, forces null checks
- **Throw generic Exception**: Loses specificity, poor developer experience
- **Checked exceptions**: Verbose try-catch everywhere, against modern Java practices

### ADR-004: Retry and Resilience Strategy

**Context:**
API calls can fail temporarily (network hiccup, API temporarily down). Should we implement automatic retries?

**Decision:**
**No built-in retry logic** in the client. Throw exceptions and let the caller decide retry strategy.

**Rationale:**
- Different callers have different retry needs (some want fast failure, some want persistence)
- Keeps the library simple and predictable
- Exceptions clearly indicate whether retry makes sense (RateLimitExceededException: yes, CompanyNotFoundException: no)
- Callers can use Spring Retry, Resilience4j, or custom retry logic as needed
- Avoids hiding transient failures from callers

**Consequences:**
- Caller responsible for retry logic
- Library is stateless (no circuit breaker state)
- Simpler library code
- More flexibility for integrating applications

**Alternatives Considered:**
- **Built-in retry with exponential backoff**: Adds complexity, may not match caller needs
- **Spring Retry integration**: Adds dependency, opinionated
- **Resilience4j circuit breaker**: Overkill for simple client library

### ADR-005: API Key Configuration

**Context:**
API key must be provided to authenticate with Companies House API. How should this be securely configured?

**Decision:**
Externalize via `application.yml` with `application-local.yml` override for local development. Support environment variable override.

**Rationale:**
- `application.yml` has placeholder/template values (safe to commit)
- `application-local.yml` (gitignored) has actual API key for local development
- Production uses environment variable: `COMPANIES_HOUSE_API_KEY`
- Never hardcoded in source code
- Follows 12-factor app principles
- Standard Spring Boot configuration patterns

**Configuration Hierarchy (precedence order):**
1. Environment variable (highest priority)
2. application-local.yml (for local dev, gitignored)
3. application.yml (default placeholder)

**Consequences:**
- Requires `application-local.yml` creation per developer (documented in README)
- Clear separation of committed defaults vs local secrets
- Secure by default (no secrets in git)

**Alternatives Considered:**
- **Hardcoded in code**: WRONG - security risk, never acceptable
- **Environment variables only**: Inflexible for local development
- **Spring Vault**: Overkill for simple API key

### ADR-006: DTO Mapping Strategy

**Context:**
Companies House API returns JSON. How should we map JSON responses to Java objects?

**Decision:**
Use **Jackson automatic mapping** with Lombok @Data for DTOs. No MapStruct or manual mapping.

**Rationale:**
- Jackson is included with Spring Boot, no extra dependencies
- @JsonProperty annotations handle API field name mapping (snake_case to camelCase)
- Simple DTOs don't need complex mapping frameworks
- Easy to debug - JSON directly maps to fields
- Lombok @Data reduces boilerplate (getters, setters, equals, hashCode, toString)

**DTO Design:**
- Use Lombok `@Data` for all DTOs
- Use `@JsonProperty` for field name mapping
- Optional fields use Java `Optional<T>` type
- Required fields use primitive wrappers (String, not Optional<String>)
- Immutable after creation (all fields final, use @Builder)

**Consequences:**
- Simple, explicit field mapping
- Easy to maintain and understand
- Slight verbosity for optional field handling

**Alternatives Considered:**
- **MapStruct**: Overkill for simple 1:1 mapping
- **Manual mapping**: More code, error-prone
- **Records**: Good option, but Lombok @Data offers more flexibility for Jackson

### ADR-007: Package Structure for Portability

**Context:**
This library will eventually be integrated into larger projects. How should packages be organized for easy extraction?

**Decision:**
Feature-based organization within `com.example.companieshouse` package, with all code self-contained.

**Rationale:**
- Single root package (`com.example.companieshouse`) contains everything
- Sub-packages organized by concern (client, dto, config, exception)
- No dependencies on external application packages
- Easy to copy entire package to another project
- Easy to rename root package when integrating

**Consequences:**
- Must rename entire package when integrating (simple find-replace)
- Clear boundaries between library and application code
- Self-contained, no external coupling

**Alternatives Considered:**
- **Layer-based at top level** (com.example.client, com.example.service): Harder to extract
- **No sub-packages**: Flat structure becomes unwieldy

---

## Package Structure

### Directory Layout

```
src/main/java/com/example/companieshouse/
│
├── client/
│   ├── CompaniesHouseClient.java          # Public interface
│   ├── CompaniesHouseClientImpl.java      # Implementation
│   └── exception/
│       ├── CompaniesHouseApiException.java        # Base exception
│       ├── CompanyNotFoundException.java          # 404 error
│       ├── RateLimitExceededException.java        # 429 error
│       ├── CompaniesHouseAuthenticationException.java  # 401 error
│       ├── InvalidResponseException.java          # Parse error
│       └── InvalidConfigurationException.java     # Config error
│
├── dto/
│   └── response/
│       ├── RegisteredAddressResponse.java  # Address data
│       └── CompanyProfileResponse.java     # Full API response (internal)
│
└── config/
    ├── CompaniesHouseConfig.java           # RestClient bean
    └── CompaniesHouseProperties.java       # Configuration properties

src/test/java/com/example/companieshouse/
│
├── client/
│   ├── CompaniesHouseClientImplTest.java          # Unit tests
│   └── CompaniesHouseClientIntegrationTest.java   # WireMock tests
│
├── dto/
│   └── response/
│       └── RegisteredAddressResponseTest.java     # DTO tests
│
└── config/
    └── CompaniesHousePropertiesTest.java          # Config validation tests
```

### File Organization

| File | Layer | Purpose |
|------|-------|---------|
| CompaniesHouseClient.java | client | Public API interface |
| CompaniesHouseClientImpl.java | client | HTTP implementation |
| CompaniesHouseApiException.java | client.exception | Base exception for all API errors |
| CompanyNotFoundException.java | client.exception | Company not found (404) |
| RateLimitExceededException.java | client.exception | Rate limit exceeded (429) |
| CompaniesHouseAuthenticationException.java | client.exception | Authentication failed (401) |
| InvalidResponseException.java | client.exception | Response parse error |
| InvalidConfigurationException.java | client.exception | Invalid configuration |
| RegisteredAddressResponse.java | dto.response | Address data DTO |
| CompanyProfileResponse.java | dto.response | Internal response wrapper |
| CompaniesHouseConfig.java | config | RestClient bean configuration |
| CompaniesHouseProperties.java | config | Configuration properties |

### Module Boundaries

**Public API** (what consuming applications use):
- `CompaniesHouseClient` - interface for all operations
- `RegisteredAddressResponse` - success response DTO
- All exceptions in `client.exception` package

**Internal** (implementation details, may change):
- `CompaniesHouseClientImpl` - HTTP implementation
- `CompanyProfileResponse` - internal response structure
- `CompaniesHouseConfig` - bean configuration

---

## Configuration Design

### application.yml Structure

```yaml
companies-house:
  api:
    base-url: https://api.company-information.service.gov.uk
    api-key: ${COMPANIES_HOUSE_API_KEY:REPLACE_WITH_YOUR_API_KEY}
    connect-timeout-ms: 5000
    read-timeout-ms: 10000
```

### application-local.yml (gitignored)

```yaml
companies-house:
  api:
    api-key: YOUR_ACTUAL_API_KEY_HERE
```

### Environment-Specific Configuration

| Environment | Configuration Method |
|-------------|---------------------|
| Local Development | `application-local.yml` with actual API key |
| CI/CD Testing | Environment variable or test properties |
| Production | Environment variable: `COMPANIES_HOUSE_API_KEY` |

### API Key Management

1. **Never commit real API keys** - `.gitignore` includes `application-local.yml`
2. **Placeholder in main config** - `application.yml` has readable placeholder
3. **Environment variable precedence** - `${COMPANIES_HOUSE_API_KEY:default}` syntax
4. **Validation at startup** - Properties class validates key is not placeholder

### Configuration Properties Class

```java
@ConfigurationProperties(prefix = "companies-house.api")
@Data
@Validated
public class CompaniesHouseProperties {

    @NotBlank(message = "Base URL is required")
    private String baseUrl = "https://api.company-information.service.gov.uk";

    @NotBlank(message = "API key is required")
    private String apiKey;

    @Positive(message = "Connect timeout must be positive")
    private int connectTimeoutMs = 5000;

    @Positive(message = "Read timeout must be positive")
    private int readTimeoutMs = 10000;

    @PostConstruct
    public void validate() {
        if ("REPLACE_WITH_YOUR_API_KEY".equals(apiKey)) {
            throw new InvalidConfigurationException(
                "API key not configured. Set COMPANIES_HOUSE_API_KEY environment variable " +
                "or create application-local.yml with actual key."
            );
        }
    }
}
```

---

## Testing Strategy

### Unit Testing Approach

**Tools**: JUnit 5, Mockito, AssertJ

**What to Test:**
- Company number validation logic
- Response mapping from JSON to DTOs
- Exception mapping from HTTP status codes
- Configuration validation

**Mocking Strategy:**
- Mock RestClient or use MockRestServiceServer
- Test error scenarios by simulating HTTP responses
- No actual network calls in unit tests

**Example Test Structure:**
```java
@ExtendWith(MockitoExtension.class)
class CompaniesHouseClientImplTest {

    @Mock
    private RestClient restClient;

    @InjectMocks
    private CompaniesHouseClientImpl client;

    @Test
    @DisplayName("Should return address for valid company number")
    void testGetRegisteredAddress_Success() {
        // Arrange: Mock successful response
        // Act: Call client
        // Assert: Verify address fields
    }

    @Test
    @DisplayName("Should throw CompanyNotFoundException for 404")
    void testGetRegisteredAddress_NotFound() {
        // Arrange: Mock 404 response
        // Act & Assert: Verify exception thrown
    }
}
```

### Integration Testing Approach

**Tools**: WireMock, Spring Boot Test

**What to Test:**
- Full HTTP communication
- JSON parsing with actual response format
- Spring configuration loading
- RestClient configuration (timeouts, base URL)

**WireMock Setup:**
```java
@SpringBootTest
@AutoConfigureWireMock(port = 0)
class CompaniesHouseClientIntegrationTest {

    @Autowired
    private CompaniesHouseClient client;

    @Test
    @DisplayName("Should parse actual API response format")
    void testGetRegisteredAddress_RealFormat() {
        // Arrange: WireMock stub with real API response format
        stubFor(get(urlPathMatching("/company/.*"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(loadTestResponse("company-profile.json"))));

        // Act
        RegisteredAddressResponse response = client.getRegisteredAddress("09370669");

        // Assert
        assertThat(response.getAddressLine1()).isNotBlank();
        assertThat(response.getPostalCode()).isNotBlank();
    }
}
```

### Test Doubles

| Type | Tool | Purpose |
|------|------|---------|
| Mock | Mockito | Unit test RestClient behavior |
| Stub | WireMock | Integration test HTTP responses |
| Fake | In-memory | Test data factories |

### Coverage Targets

| Component | Target Coverage |
|-----------|-----------------|
| CompaniesHouseClientImpl | 90%+ |
| DTOs | 100% |
| Exceptions | 100% |
| Configuration | 80%+ |
| **Overall** | **80%+** |

---

## Dependencies

### Spring Boot Starters

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

### Testing Dependencies

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>

<dependency>
    <groupId>org.wiremock</groupId>
    <artifactId>wiremock-standalone</artifactId>
    <version>3.3.1</version>
    <scope>test</scope>
</dependency>
```

### Utilities

```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <scope>provided</scope>
</dependency>
```

### Version Recommendations

| Dependency | Version | Notes |
|------------|---------|-------|
| Spring Boot | 3.2.0+ | Required for RestClient |
| Java | 17+ | LTS version, required by Spring Boot 3 |
| WireMock | 3.3.1+ | Latest standalone version |
| Lombok | 1.18.30+ | Latest stable |
| JaCoCo | 0.8.11+ | Code coverage |

### Complete pom.xml Dependencies Section

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.2.0</version>
    <relativePath/>
</parent>

<properties>
    <java.version>17</java.version>
    <wiremock.version>3.3.1</wiremock.version>
</properties>

<dependencies>
    <!-- Spring Boot -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>

    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <scope>provided</scope>
    </dependency>

    <!-- Testing -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.wiremock</groupId>
        <artifactId>wiremock-standalone</artifactId>
        <version>${wiremock.version}</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

---

## Security Considerations

### API Key Protection

- **Never hardcoded** in source code
- **Never committed** to git repository
- Externalized via `application-local.yml` (gitignored) or environment variable
- Validated at startup - fails fast if placeholder value detected
- Uses Spring Boot's property placeholder syntax: `${COMPANIES_HOUSE_API_KEY:default}`

### HTTPS Enforcement

- Base URL defaults to HTTPS: `https://api.company-information.service.gov.uk`
- RestClient validates SSL certificates by default
- No options to disable SSL verification (secure by default)
- Configuration validation rejects HTTP URLs

### Error Messages

- **No sensitive data in exceptions**: API key never included in error messages
- **No API key in logs**: Logging configuration redacts sensitive headers
- **Company numbers may be logged**: Not considered sensitive PII
- **Stack traces**: Available in debug mode, stripped in production

### Input Validation

- Company number validated before API call (format: 8 alphanumeric characters)
- Reject obviously invalid input early (fail fast)
- No SQL injection risk (no database layer)
- No XSS risk (no HTML output)

---

## Extensibility Considerations

### Adding New Endpoints

If additional Companies House API endpoints are needed:

1. **Add method to interface**:
   ```java
   public interface CompaniesHouseClient {
       RegisteredAddressResponse getRegisteredAddress(String companyNumber);
       CompanyOfficersResponse getOfficers(String companyNumber);  // NEW
   }
   ```

2. **Implement in CompaniesHouseClientImpl**:
   ```java
   @Override
   public CompanyOfficersResponse getOfficers(String companyNumber) {
       return restClient.get()
           .uri("/company/{companyNumber}/officers", companyNumber)
           .retrieve()
           .onStatus(...)
           .body(CompanyOfficersResponse.class);
   }
   ```

3. **Create new response DTO** in `dto.response` package

4. **Add specific exceptions** if needed (e.g., `OfficersNotFoundException`)

### Switching HTTP Clients

If need to switch from RestClient to WebClient (for reactive):

- Only `CompaniesHouseClientImpl` changes
- `CompaniesHouseClient` interface remains unchanged
- Callers completely unaffected
- Update `CompaniesHouseConfig` to create WebClient bean

### Adding Caching

If caching is needed:

1. Add `spring-boot-starter-cache` dependency
2. Add `@EnableCaching` to config
3. Add `@Cacheable` to client methods
4. Consider cache key (company number) and TTL

### Adding Retry Logic

If built-in retry is needed later:

1. Add `spring-retry` dependency
2. Add `@EnableRetry` to config
3. Add `@Retryable` to appropriate methods with specific exception types
4. Configure backoff and max attempts

---

## Next Steps

This architecture document feeds into **Prompt 03: Implementation Planning**, which will:

1. Read this architecture document
2. Break work into 11 specific tasks (T1-T11)
3. Define the TDD workflow for each task
4. Create dependency graph showing task order
5. Produce `docs/plan.md`

The implementation will follow TDD (Test-Driven Development):
1. **RED**: Write failing test
2. **GREEN**: Write minimal code to pass
3. **REFACTOR**: Clean up while keeping tests green
4. **COMMIT**: Commit working code

---

**Document Version**: 1.0
**Last Updated**: 2026-01-27
**Status**: Complete - Ready for Implementation Planning phase
