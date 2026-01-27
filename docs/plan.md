# Companies House API Integration - Implementation Plan

## Overview

This document provides a comprehensive implementation plan for building a Companies House API client library using Test-Driven Development (TDD). The plan breaks work into 11 clearly defined tasks (T1-T11), each with specific TDD workflows, file specifications, and quality gates.

**Estimated Total Effort**: 3-5 hours across multiple sessions
**Methodology**: Test-Driven Development (Red-Green-Refactor)
**Context Compaction**: Progress tracked in `.work/implementation/` for session resumption

### Key Architecture Decisions

| Decision | Value | Rationale |
|----------|-------|-----------|
| HTTP Client | RestClient (Spring 6.1+) | Modern API, recommended for Spring Boot 3.2+, RestTemplate in maintenance mode |
| API Endpoint | `/company/{companyNumber}` | Returns complete address fields including care_of and po_box |
| Package Structure | `com.example.companieshouse` | Self-contained for portability |
| Build Tool | Maven | Standard for Spring Boot ecosystem |
| Testing | JUnit 5 + Mockito + WireMock | Industry standard, excellent Spring integration |

---

## Project Setup

### Maven Configuration

**File**: `pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
        <relativePath/>
    </parent>

    <groupId>com.example</groupId>
    <artifactId>companies-house-client</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <name>Companies House API Client</name>
    <description>Client library for UK Companies House Public Data API</description>

    <properties>
        <java.version>17</java.version>
        <wiremock.version>3.3.1</wiremock.version>
    </properties>

    <dependencies>
        <!-- Spring Boot Starters -->
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

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.jacoco</groupId>
                <artifactId>jacoco-maven-plugin</artifactId>
                <version>0.8.11</version>
                <executions>
                    <execution>
                        <goals>
                            <goal>prepare-agent</goal>
                        </goals>
                    </execution>
                    <execution>
                        <id>report</id>
                        <phase>test</phase>
                        <goals>
                            <goal>report</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

### Directory Structure

```
company-house-api/
├── src/
│   ├── main/
│   │   ├── java/com/example/companieshouse/
│   │   │   ├── CompaniesHouseApplication.java
│   │   │   ├── client/
│   │   │   │   ├── CompaniesHouseClient.java
│   │   │   │   ├── CompaniesHouseClientImpl.java
│   │   │   │   └── exception/
│   │   │   │       ├── CompaniesHouseApiException.java
│   │   │   │       ├── CompanyNotFoundException.java
│   │   │   │       ├── RateLimitExceededException.java
│   │   │   │       ├── CompaniesHouseAuthenticationException.java
│   │   │   │       ├── InvalidResponseException.java
│   │   │   │       └── InvalidConfigurationException.java
│   │   │   ├── dto/
│   │   │   │   └── response/
│   │   │   │       ├── RegisteredAddressResponse.java
│   │   │   │       └── CompanyProfileResponse.java
│   │   │   └── config/
│   │   │       ├── CompaniesHouseConfig.java
│   │   │       └── CompaniesHouseProperties.java
│   │   └── resources/
│   │       ├── application.yml
│   │       └── application-local.yml.example
│   │
│   └── test/
│       ├── java/com/example/companieshouse/
│       │   ├── client/
│       │   │   ├── CompaniesHouseClientImplTest.java
│       │   │   ├── CompaniesHouseClientIntegrationTest.java
│       │   │   └── exception/
│       │   │       └── ExceptionTests.java
│       │   ├── dto/
│       │   │   └── response/
│       │   │       └── RegisteredAddressResponseTest.java
│       │   └── config/
│       │       └── CompaniesHousePropertiesTest.java
│       └── resources/
│           ├── application-test.yml
│           └── __files/
│               ├── company-profile-success.json
│               └── company-profile-error.json
│
├── .work/
│   └── implementation/
│       ├── progress.yaml
│       ├── task-status.yaml
│       └── files-created.yaml
│
├── docs/
│   ├── requirements.md
│   ├── architecture.md
│   └── plan.md (this file)
│
├── pom.xml
├── .gitignore
└── README.md
```

### Configuration Files

**application.yml**:
```yaml
spring:
  application:
    name: companies-house-client

companies-house:
  api:
    base-url: https://api.company-information.service.gov.uk
    api-key: ${COMPANIES_HOUSE_API_KEY:REPLACE_WITH_YOUR_API_KEY}
    connect-timeout-ms: 5000
    read-timeout-ms: 10000
```

**application-test.yml**:
```yaml
companies-house:
  api:
    base-url: http://localhost:${wiremock.server.port}
    api-key: test-api-key
    connect-timeout-ms: 1000
    read-timeout-ms: 2000
```

**.gitignore**:
```
# IDE
.idea/
.vscode/
*.swp
*.iml

# Build
target/
*.class
*.jar

# Configuration (never commit API keys!)
application-local.yml
application-dev.yml
*.env

# OS
.DS_Store
Thumbs.db

# Work directory (implementation tracking)
.work/
```

---

## Task Breakdown (T1-T11)

### T1: Project Setup and Build Configuration

**Objective**: Initialize Maven project with all dependencies and directory structure.

**Files to Create**:
| File | Purpose |
|------|---------|
| `pom.xml` | Maven configuration with Spring Boot 3.2+, dependencies |
| `src/main/java/.../CompaniesHouseApplication.java` | Spring Boot entry point |
| `src/main/resources/application.yml` | Default configuration |
| `.gitignore` | Exclude sensitive files and build artifacts |
| `README.md` | Project description (skeleton) |

**TDD Workflow**:
1. **RED**: Create `CompaniesHouseApplication.java` with `@SpringBootApplication`
2. **GREEN**: Configure `pom.xml` with all dependencies, verify `mvn clean install` passes
3. **REFACTOR**: Organize pom.xml (properties section, dependency versions)
4. **COMMIT**: `git commit -m "T1: Project setup with Maven and Spring Boot 3.2"`

**Test Verification**:
```bash
mvn clean install
# Should succeed with no errors
```

**Definition of Done**:
- [ ] `mvn clean install` succeeds without warnings
- [ ] `mvn test` runs (no tests yet, but framework works)
- [ ] Directory structure created per architecture.md
- [ ] .gitignore properly configured
- [ ] Spring Boot application context loads

**Estimated Complexity**: LOW

---

### T2: Configuration Properties Class

**Objective**: Create type-safe configuration properties with validation.

**Files to Create**:
| File | Purpose |
|------|---------|
| `src/main/java/.../config/CompaniesHouseProperties.java` | Configuration properties |
| `src/test/java/.../config/CompaniesHousePropertiesTest.java` | Unit tests |
| `src/test/resources/application-test.yml` | Test configuration |

**TDD Workflow**:
1. **RED**: Write test that expects properties to load from `application-test.yml`
   ```java
   @Test
   @DisplayName("Should load configuration from application-test.yml")
   void testPropertiesLoad() {
       assertThat(properties.getBaseUrl()).isNotBlank();
       assertThat(properties.getApiKey()).isEqualTo("test-api-key");
   }
   ```
2. **GREEN**: Create `CompaniesHouseProperties` with `@ConfigurationProperties`
3. **REFACTOR**: Add Lombok `@Data`, validation annotations (`@NotBlank`, `@Positive`)
4. **COMMIT**: `git commit -m "T2: Configuration properties with validation"`

**Test Scenarios**:
- Properties load from application-test.yml
- `@NotBlank` validation fails for empty baseUrl
- `@NotBlank` validation fails for empty apiKey
- `@Positive` validation fails for zero/negative timeouts
- Default timeout values are applied
- `@PostConstruct` rejects placeholder API key value

**Definition of Done**:
- [ ] `CompaniesHouseProperties` loads from application-test.yml
- [ ] All fields have validation annotations
- [ ] Placeholder API key detection in `@PostConstruct`
- [ ] Tests pass with 100% coverage
- [ ] JavaDoc on class level

**Estimated Complexity**: LOW

---

### T3: RestClient Bean Configuration

**Objective**: Configure RestClient bean with timeouts and API key interceptor.

**Files to Create**:
| File | Purpose |
|------|---------|
| `src/main/java/.../config/CompaniesHouseConfig.java` | RestClient bean configuration |
| `src/test/java/.../config/CompaniesHouseConfigTest.java` | Configuration tests |

**TDD Workflow**:
1. **RED**: Write test that RestClient bean is created and injectable
   ```java
   @Test
   @DisplayName("Should create RestClient bean with base URL")
   void testRestClientBeanCreated() {
       assertThat(restClient).isNotNull();
   }
   ```
2. **GREEN**: Create `@Configuration` class with `@Bean` method returning `RestClient`
3. **REFACTOR**: Configure timeouts from properties, add API key interceptor
4. **COMMIT**: `git commit -m "T3: RestClient bean configuration with timeouts"`

**RestClient Configuration**:
```java
@Bean
public RestClient restClient(CompaniesHouseProperties properties) {
    return RestClient.builder()
        .baseUrl(properties.getBaseUrl())
        .defaultHeader("Authorization", "Basic " +
            Base64.getEncoder().encodeToString(
                (properties.getApiKey() + ":").getBytes()))
        .requestFactory(clientHttpRequestFactory(properties))
        .build();
}
```

**Test Scenarios**:
- RestClient bean is created
- Base URL from properties is used
- Timeouts configured correctly
- API key added via Authorization header (Basic auth per API docs)
- Bean injectable into other components

**Definition of Done**:
- [ ] RestClient bean available for injection
- [ ] Timeouts configured from properties
- [ ] API key included in requests (Basic auth)
- [ ] Tests pass
- [ ] JavaDoc explains configuration choices

**Estimated Complexity**: LOW

---

### T4: Response DTOs

**Objective**: Create DTOs for deserializing Companies House API responses.

**Files to Create**:
| File | Purpose |
|------|---------|
| `src/main/java/.../dto/response/RegisteredAddressResponse.java` | Address data DTO |
| `src/main/java/.../dto/response/CompanyProfileResponse.java` | Full API response wrapper |
| `src/test/java/.../dto/response/RegisteredAddressResponseTest.java` | DTO tests |
| `src/test/resources/__files/company-profile-success.json` | Sample API response |

**TDD Workflow**:
1. **RED**: Write test deserializing sample JSON into DTOs
   ```java
   @Test
   @DisplayName("Should deserialize Companies House JSON response")
   void testDeserialize() throws Exception {
       String json = loadResource("company-profile-success.json");
       CompanyProfileResponse response = objectMapper.readValue(json, CompanyProfileResponse.class);

       assertThat(response.getRegisteredOfficeAddress()).isNotNull();
       assertThat(response.getRegisteredOfficeAddress().getAddressLine1()).isNotBlank();
   }
   ```
2. **GREEN**: Create DTOs with `@JsonProperty` for snake_case mapping
3. **REFACTOR**: Use Lombok `@Data`, `@Builder`; add Optional for nullable fields
4. **COMMIT**: `git commit -m "T4: Response DTOs with JSON deserialization"`

**RegisteredAddressResponse Fields**:
| Field | Type | JSON Property | Required |
|-------|------|---------------|----------|
| addressLine1 | String | address_line_1 | Yes |
| addressLine2 | String | address_line_2 | No |
| postalCode | String | postal_code | Yes |
| locality | String | locality | No |
| country | String | country | No |
| careOf | String | care_of | No |
| poBox | String | po_box | No |
| premises | String | premises | No |
| region | String | region | No |

**Test Scenarios**:
- Deserialize full company profile JSON (real format from API)
- Extract `registered_office_address` to `RegisteredAddressResponse`
- Handle null/missing optional fields
- Round-trip serialization works
- All address fields correctly mapped

**Definition of Done**:
- [ ] `RegisteredAddressResponse` deserializes valid JSON
- [ ] `CompanyProfileResponse` wraps full API response
- [ ] Optional fields handled (null-safe)
- [ ] Tests include real JSON examples from API docs
- [ ] Coverage: 100%

**Estimated Complexity**: LOW

---

### T5: Custom Exceptions

**Objective**: Create exception hierarchy for API error scenarios.

**Files to Create**:
| File | Purpose |
|------|---------|
| `src/main/java/.../client/exception/CompaniesHouseApiException.java` | Base exception |
| `src/main/java/.../client/exception/CompanyNotFoundException.java` | 404 error |
| `src/main/java/.../client/exception/RateLimitExceededException.java` | 429 error |
| `src/main/java/.../client/exception/CompaniesHouseAuthenticationException.java` | 401 error |
| `src/main/java/.../client/exception/InvalidResponseException.java` | Parse error |
| `src/main/java/.../client/exception/InvalidConfigurationException.java` | Config error |
| `src/test/java/.../client/exception/ExceptionTests.java` | Tests for all exceptions |

**Exception Hierarchy**:
```
RuntimeException
└── CompaniesHouseApiException (base)
    ├── CompanyNotFoundException (404)
    ├── RateLimitExceededException (429)
    ├── CompaniesHouseAuthenticationException (401)
    └── InvalidResponseException (parse error)
└── InvalidConfigurationException (config validation)
```

**TDD Workflow**:
1. **RED**: Write tests for each exception type
   ```java
   @Test
   @DisplayName("Should create CompanyNotFoundException with company number")
   void testCompanyNotFoundException() {
       var exception = new CompanyNotFoundException("09370669");
       assertThat(exception.getMessage()).contains("09370669");
       assertThat(exception.getCompanyNumber()).isEqualTo("09370669");
   }
   ```
2. **GREEN**: Create exception classes extending appropriate base
3. **REFACTOR**: Add factory methods, ensure context in messages
4. **COMMIT**: `git commit -m "T5: Custom exceptions with context"`

**Exception Details**:

| Exception | HTTP Status | Constructor | Message Format |
|-----------|-------------|-------------|----------------|
| `CompanyNotFoundException` | 404 | `(String companyNumber)` | "Company not found: {companyNumber}" |
| `RateLimitExceededException` | 429 | `(String message, Long retryAfter)` | "Rate limit exceeded. Retry after: {n} seconds" |
| `CompaniesHouseAuthenticationException` | 401 | `(String message)` | "Authentication failed: {message}" |
| `InvalidResponseException` | N/A | `(String message, Throwable cause)` | "Failed to parse response: {message}" |
| `InvalidConfigurationException` | N/A | `(String message)` | "Invalid configuration: {message}" |

**Test Scenarios**:
- Each exception created with appropriate constructor
- Exception messages include context
- `RateLimitExceededException` stores retryAfter value
- Base exception captures HTTP status when applicable
- Exceptions are `RuntimeException` (unchecked)

**Definition of Done**:
- [ ] All 6 exception types created
- [ ] Each includes relevant context in message
- [ ] `RateLimitExceededException` has `getRetryAfter()` method
- [ ] Tests pass with 100% coverage
- [ ] JavaDoc explains when each should be thrown

**Estimated Complexity**: LOW

---

### T6: Client Interface

**Objective**: Define the public API contract for the client library.

**File to Create**:
| File | Purpose |
|------|---------|
| `src/main/java/.../client/CompaniesHouseClient.java` | Public interface |

**Interface Definition**:
```java
package com.example.companieshouse.client;

import com.example.companieshouse.dto.response.RegisteredAddressResponse;

/**
 * Client for Companies House Public Data API.
 *
 * <p>Provides methods to retrieve company information from the
 * UK Companies House API. All methods throw specific exceptions
 * for error scenarios, enabling precise error handling.</p>
 */
public interface CompaniesHouseClient {

    /**
     * Retrieve the registered office address for a company.
     *
     * @param companyNumber the UK company number (e.g., "09370669")
     * @return the registered address
     * @throws CompanyNotFoundException if company does not exist (HTTP 404)
     * @throws RateLimitExceededException if rate limit exceeded (HTTP 429)
     * @throws CompaniesHouseAuthenticationException if authentication fails (HTTP 401)
     * @throws CompaniesHouseApiException for other API errors (HTTP 5xx, timeouts)
     * @throws IllegalArgumentException if companyNumber is null or invalid format
     */
    RegisteredAddressResponse getRegisteredAddress(String companyNumber);
}
```

**TDD Workflow**:
1. No test needed for interface
2. Create interface with full JavaDoc
3. **COMMIT**: `git commit -m "T6: Client interface definition"`

**Definition of Done**:
- [ ] Interface clearly documented with JavaDoc
- [ ] All exception types documented in `@throws`
- [ ] Parameter requirements documented
- [ ] Return type is direct (throws on not found, not Optional)
- [ ] Follows STD-003 naming (no "I" prefix)

**Estimated Complexity**: TRIVIAL

---

### T7: Client Implementation - Success Path

**Objective**: Implement the client for successful API calls.

**Files to Create**:
| File | Purpose |
|------|---------|
| `src/main/java/.../client/CompaniesHouseClientImpl.java` | Implementation |
| `src/test/java/.../client/CompaniesHouseClientImplTest.java` | Unit tests (Part 1) |

**TDD Workflow**:
1. **RED**: Write test for successful API call
   ```java
   @Test
   @DisplayName("Should return address for valid company number")
   void testGetRegisteredAddress_Success() {
       // Arrange: Configure mock to return successful response
       when(restClient.get()
           .uri(anyString(), anyString())
           .retrieve()
           .body(CompanyProfileResponse.class))
           .thenReturn(createSuccessResponse());

       // Act
       RegisteredAddressResponse result = client.getRegisteredAddress("09370669");

       // Assert
       assertThat(result).isNotNull();
       assertThat(result.getAddressLine1()).isEqualTo("123 Test Street");
   }
   ```
2. **GREEN**: Implement RestClient GET call with response mapping
3. **REFACTOR**: Extract address from `CompanyProfileResponse`
4. **COMMIT**: `git commit -m "T7: Client implementation - success path"`

**Implementation Skeleton**:
```java
@Component
@RequiredArgsConstructor
public class CompaniesHouseClientImpl implements CompaniesHouseClient {

    private final RestClient restClient;

    @Override
    public RegisteredAddressResponse getRegisteredAddress(String companyNumber) {
        CompanyProfileResponse response = restClient.get()
            .uri("/company/{companyNumber}", companyNumber)
            .retrieve()
            .body(CompanyProfileResponse.class);

        return response.getRegisteredOfficeAddress();
    }
}
```

**Test Scenarios**:
- Makes GET to `/company/{companyNumber}`
- Deserializes response to `CompanyProfileResponse`
- Extracts `registered_office_address` from response
- Returns `RegisteredAddressResponse` with all fields populated

**Definition of Done**:
- [ ] RestClient GET call made to correct URL
- [ ] Response deserialized correctly
- [ ] Address extracted from company profile
- [ ] Tests pass
- [ ] Code follows STD-003 (constructor injection)
- [ ] Coverage: 80%+ on success path

**Estimated Complexity**: MEDIUM

---

### T8: Client Implementation - Error Handling

**Objective**: Add comprehensive error handling to the client implementation.

**Files to Modify**:
| File | Changes |
|------|---------|
| `src/main/java/.../client/CompaniesHouseClientImpl.java` | Add error handling |
| `src/test/java/.../client/CompaniesHouseClientImplTest.java` | Add error tests |

**TDD Workflow**:
1. **RED**: Write tests for each error scenario
   ```java
   @Test
   @DisplayName("Should throw CompanyNotFoundException for 404 response")
   void testGetRegisteredAddress_NotFound() {
       // Arrange: Mock returns 404
       // Act & Assert
       assertThrows(CompanyNotFoundException.class,
           () -> client.getRegisteredAddress("99999999"));
   }
   ```
2. **GREEN**: Add `.onStatus()` handlers for each HTTP error code
3. **REFACTOR**: Add input validation, clean error handling
4. **COMMIT**: `git commit -m "T8: Client implementation - error handling"`

**Error Mapping**:

| HTTP Status | Exception | Test Case |
|-------------|-----------|-----------|
| 404 | `CompanyNotFoundException` | Company doesn't exist |
| 429 | `RateLimitExceededException` | Rate limit exceeded |
| 401 | `CompaniesHouseAuthenticationException` | Invalid API key |
| 500/502/503 | `CompaniesHouseApiException` | Server error |
| Timeout | `CompaniesHouseApiException` | Connection/read timeout |
| Parse error | `InvalidResponseException` | Malformed JSON |
| null input | `IllegalArgumentException` | Null company number |
| empty input | `IllegalArgumentException` | Empty company number |

**Implementation with Error Handling**:
```java
@Override
public RegisteredAddressResponse getRegisteredAddress(String companyNumber) {
    validateCompanyNumber(companyNumber);

    try {
        CompanyProfileResponse response = restClient.get()
            .uri("/company/{companyNumber}", companyNumber)
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError, (request, clientResponse) -> {
                if (clientResponse.getStatusCode() == HttpStatus.NOT_FOUND) {
                    throw new CompanyNotFoundException(companyNumber);
                }
                if (clientResponse.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                    Long retryAfter = extractRetryAfter(clientResponse);
                    throw new RateLimitExceededException("Rate limit exceeded", retryAfter);
                }
                if (clientResponse.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                    throw new CompaniesHouseAuthenticationException(
                        "Invalid API key - check configuration");
                }
                throw new CompaniesHouseApiException(
                    "Client error: HTTP " + clientResponse.getStatusCode().value());
            })
            .onStatus(HttpStatusCode::is5xxServerError, (request, clientResponse) -> {
                throw new CompaniesHouseApiException(
                    "Server error: HTTP " + clientResponse.getStatusCode().value());
            })
            .body(CompanyProfileResponse.class);

        return extractAddress(response, companyNumber);
    } catch (RestClientException e) {
        throw new CompaniesHouseApiException("API call failed: " + e.getMessage(), e);
    }
}

private void validateCompanyNumber(String companyNumber) {
    if (companyNumber == null || companyNumber.isBlank()) {
        throw new IllegalArgumentException("Company number must not be null or empty");
    }
}
```

**Test Scenarios**:
- HTTP 404 → throws `CompanyNotFoundException` with company number
- HTTP 429 → throws `RateLimitExceededException` with retry-after
- HTTP 401 → throws `CompaniesHouseAuthenticationException`
- HTTP 500 → throws `CompaniesHouseApiException` with status
- Connection timeout → throws `CompaniesHouseApiException`
- Malformed JSON → throws `InvalidResponseException`
- null company number → throws `IllegalArgumentException`
- empty company number → throws `IllegalArgumentException`

**Definition of Done**:
- [ ] All error scenarios tested
- [ ] Correct exception type thrown for each scenario
- [ ] Exception messages include context
- [ ] Input validation for company number
- [ ] Tests pass
- [ ] Coverage: 90%+ on client implementation

**Estimated Complexity**: MEDIUM

---

### T9: Global Exception Handler (SKIPPED)

**Note**: This task is **not needed** for a client library. Global exception handlers (`@RestControllerAdvice`) are only needed when building a REST API wrapper. The client library throws exceptions that callers handle directly.

If a REST wrapper is added later, this task can be implemented.

---

### T10: Integration Tests with WireMock

**Objective**: Test the full stack with realistic API simulation.

**Files to Create**:
| File | Purpose |
|------|---------|
| `src/test/java/.../client/CompaniesHouseClientIntegrationTest.java` | Integration tests |
| `src/test/resources/__files/company-profile-success.json` | Success stub response |
| `src/test/resources/__files/company-profile-error-404.json` | 404 stub response |

**TDD Workflow**:
1. **RED**: Write `@SpringBootTest` with WireMock stubs
   ```java
   @SpringBootTest
   @AutoConfigureWireMock(port = 0)
   @ActiveProfiles("test")
   class CompaniesHouseClientIntegrationTest {

       @Autowired
       private CompaniesHouseClient client;

       @Test
       @DisplayName("Should retrieve address via stubbed API")
       void testGetRegisteredAddress_Integration() {
           // Arrange: WireMock stub
           stubFor(get(urlPathEqualTo("/company/09370669"))
               .willReturn(aResponse()
                   .withStatus(200)
                   .withHeader("Content-Type", "application/json")
                   .withBodyFile("company-profile-success.json")));

           // Act
           RegisteredAddressResponse result = client.getRegisteredAddress("09370669");

           // Assert
           assertThat(result.getAddressLine1()).isNotBlank();
           assertThat(result.getPostalCode()).isNotBlank();
       }
   }
   ```
2. **GREEN**: Configure WireMock stubs, run tests
3. **REFACTOR**: Extract test data to JSON files
4. **COMMIT**: `git commit -m "T10: Integration tests with WireMock"`

**WireMock Setup**:
- Use `@AutoConfigureWireMock(port = 0)` for dynamic port
- Configure `application-test.yml` with `${wiremock.server.port}`
- Store stub responses in `src/test/resources/__files/`

**Test Scenarios**:
- Spring context starts successfully
- Configuration loads from application-test.yml
- Dependency injection works (can inject `CompaniesHouseClient`)
- End-to-end call to stubbed API returns correct data
- Error responses (404, 429, 401, 500) handled correctly
- JSON parsing matches real API response format

**Sample WireMock Stub Response** (`company-profile-success.json`):
```json
{
  "company_number": "09370669",
  "company_name": "TEST COMPANY LIMITED",
  "registered_office_address": {
    "address_line_1": "123 Test Street",
    "address_line_2": "Suite 100",
    "postal_code": "SW1A 1AA",
    "locality": "London",
    "country": "United Kingdom",
    "care_of": null,
    "po_box": null,
    "premises": "Office 1",
    "region": "Greater London"
  }
}
```

**Definition of Done**:
- [ ] Spring context test passes
- [ ] WireMock stubbing works
- [ ] Full end-to-end scenario tested
- [ ] Configuration loaded correctly from test profile
- [ ] Both success and error scenarios tested
- [ ] Tests pass reliably (no flakiness)

**Estimated Complexity**: MEDIUM

---

### T11: Documentation and README

**Objective**: Ensure developers can use and maintain the library.

**Files to Create/Update**:
| File | Changes |
|------|---------|
| `README.md` | Complete documentation |
| All source files | Verify JavaDoc complete |

**README Sections**:

1. **Overview**: What the library does
2. **Prerequisites**: Java 17+, Maven, API key
3. **Quick Start**: Basic usage example
4. **Configuration**: application.yml structure, environment variables
5. **Usage Examples**: Success and error handling
6. **Building**: `mvn clean install`
7. **Testing**: `mvn test`, `mvn clean test jacoco:report`
8. **Coverage**: How to view JaCoCo report
9. **Error Handling**: Exception types and when they occur
10. **Architecture**: Link to architecture.md
11. **Contributing**: Link to STD-003 rubric

**TDD Workflow**:
1. Update README.md with all sections
2. Verify all public classes have JavaDoc
3. Run `mvn javadoc:javadoc` to verify no errors
4. **COMMIT**: `git commit -m "T11: Documentation and README complete"`

**Definition of Done**:
- [ ] README.md complete with all sections
- [ ] Setup instructions clear for new developer
- [ ] Code examples compile and work
- [ ] All public classes have JavaDoc
- [ ] All public methods have `@param`, `@return`, `@throws`
- [ ] `mvn javadoc:javadoc` generates without errors
- [ ] README links to architecture and requirements docs

**Estimated Complexity**: LOW

---

## Task Dependencies

### Dependency Graph

```
T1 (Project Setup)
│
├──────────────────┐
│                  │
v                  v
T2 (Properties)    T4 (DTOs)
│                  │
v                  v
T3 (Config)        T5 (Exceptions)
│                  │
│                  v
│                  T6 (Interface)
│                  │
└─────────┬────────┘
          │
          v
T7 (Client Success)
          │
          v
T8 (Client Errors)
          │
          v
T10 (Integration Tests)
          │
          v
T11 (Documentation)
```

### Critical Path

`T1 → T2 → T3 → T4 → T5 → T6 → T7 → T8 → T10 → T11`

### Parallel Opportunities

| Parallel Set | Tasks | Notes |
|--------------|-------|-------|
| After T1 | T2 + T4 | Properties and DTOs are independent |
| After T4 | T5 | Exceptions depend on nothing else |
| After T3, T5, T6 | T7 | Client needs config, exceptions, and interface |

---

## File-Level Planning

### Complete File List

| Task | Source Files | Test Files | Other |
|------|-------------|-----------|-------|
| T1 | CompaniesHouseApplication.java | - | pom.xml, .gitignore, README.md |
| T2 | CompaniesHouseProperties.java | CompaniesHousePropertiesTest.java | application-test.yml |
| T3 | CompaniesHouseConfig.java | CompaniesHouseConfigTest.java | - |
| T4 | RegisteredAddressResponse.java, CompanyProfileResponse.java | RegisteredAddressResponseTest.java | __files/*.json |
| T5 | 6 exception classes | ExceptionTests.java | - |
| T6 | CompaniesHouseClient.java | - | - |
| T7 | CompaniesHouseClientImpl.java (Part 1) | CompaniesHouseClientImplTest.java (Part 1) | - |
| T8 | CompaniesHouseClientImpl.java (Part 2) | CompaniesHouseClientImplTest.java (Part 2) | - |
| T10 | - | CompaniesHouseClientIntegrationTest.java | WireMock stubs |
| T11 | - | - | README.md |

### Files by Package

```
com.example.companieshouse/
├── CompaniesHouseApplication.java           [T1]
├── client/
│   ├── CompaniesHouseClient.java            [T6]
│   ├── CompaniesHouseClientImpl.java        [T7, T8]
│   └── exception/
│       ├── CompaniesHouseApiException.java  [T5]
│       ├── CompanyNotFoundException.java    [T5]
│       ├── RateLimitExceededException.java  [T5]
│       ├── CompaniesHouseAuthenticationException.java [T5]
│       ├── InvalidResponseException.java    [T5]
│       └── InvalidConfigurationException.java [T5]
├── dto/
│   └── response/
│       ├── RegisteredAddressResponse.java   [T4]
│       └── CompanyProfileResponse.java      [T4]
└── config/
    ├── CompaniesHouseConfig.java            [T3]
    └── CompaniesHouseProperties.java        [T2]
```

---

## Quality Gates

### Coverage Targets

| Component | Target | Justification |
|-----------|--------|---------------|
| CompaniesHouseClientImpl | 90%+ | Core business logic, must be thoroughly tested |
| CompaniesHouseProperties | 100% | Validation logic, all paths testable |
| CompaniesHouseConfig | 80%+ | Configuration code, most paths testable |
| DTOs (all) | 100% | Simple classes, 100% achievable |
| Exceptions (all) | 100% | Simple classes, 100% achievable |
| **Overall** | **80%+** | Industry standard for production code |

### Per-Task Quality Gate

Every task must satisfy before commit:
- [ ] Tests written first (RED phase)
- [ ] All tests pass (GREEN phase)
- [ ] Code reviewed for STD-003 compliance
- [ ] Coverage meets target (check with `mvn jacoco:report`)
- [ ] JavaDoc on public methods
- [ ] No hardcoded configuration or secrets
- [ ] Commit message follows format: `"T{n}: {description}"`

### Pre-Commit Checklist

Before every commit:
```bash
# 1. Run tests
mvn clean test

# 2. Check coverage (after running tests)
open target/site/jacoco/index.html

# 3. Verify no warnings
mvn clean install -q  # Should show no warnings

# 4. Self-assessment
# - Constructor injection used?
# - Proper generics (no raw types)?
# - Optional used for nullable returns?
# - Custom exceptions used?
# - No hardcoded config?
```

### STD-003 Compliance Checklist

Per STD-003 rubric, verify:
- [ ] All beans use constructor injection (`@RequiredArgsConstructor`)
- [ ] No `@Autowired` on fields
- [ ] Proper generics (no raw types like `List` without `<T>`)
- [ ] `Optional` for potentially null values
- [ ] Custom exceptions extend `RuntimeException`
- [ ] Configuration externalized via `application.yml`
- [ ] No hardcoded configuration values or secrets

---

## TDD Workflow Reference

### Red-Green-Refactor Cycle

```
┌─────────────────────────────────────────────────────────────────┐
│                          TDD Cycle                               │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│    ┌───────┐         ┌───────┐         ┌──────────┐             │
│    │  RED  │ ──────> │ GREEN │ ──────> │ REFACTOR │ ──────┐     │
│    └───────┘         └───────┘         └──────────┘       │     │
│         │                                                  │     │
│         │                                                  │     │
│         └──────────────────────────────────────────────────┘     │
│                                                                  │
│    RED:      Write a failing test first                          │
│    GREEN:    Write minimum code to pass the test                 │
│    REFACTOR: Clean up code while keeping tests green             │
│    REPEAT:   Next test case                                      │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### Example TDD Cycle

**Task**: Implement `CompanyNotFoundException`

**RED Phase** (write failing test):
```java
@Test
@DisplayName("Should create exception with company number in message")
void testCompanyNotFoundException() {
    var exception = new CompanyNotFoundException("09370669");
    assertThat(exception.getMessage()).contains("09370669");
}
```
Run test → **FAILS** (class doesn't exist)

**GREEN Phase** (write minimum code to pass):
```java
public class CompanyNotFoundException extends CompaniesHouseApiException {
    public CompanyNotFoundException(String companyNumber) {
        super("Company not found: " + companyNumber);
    }
}
```
Run test → **PASSES**

**REFACTOR Phase** (improve code quality):
```java
public class CompanyNotFoundException extends CompaniesHouseApiException {
    private final String companyNumber;

    public CompanyNotFoundException(String companyNumber) {
        super("Company not found: " + companyNumber);
        this.companyNumber = companyNumber;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }
}
```
Run test → **STILL PASSES**

**COMMIT**: `git commit -m "T5: Add CompanyNotFoundException"`

### Commit Strategy

- **One commit per task** (T1, T2, etc.) when task is complete
- **All tests must pass** before commit
- **Commit message format**: `"T{n}: {brief description}"`
- **Examples**:
  - `"T1: Project setup with Maven and Spring Boot 3.2"`
  - `"T5: Custom exceptions with context"`
  - `"T8: Client error handling for all HTTP status codes"`

---

## Tools and Commands

### Building

```bash
# Full build with tests
mvn clean install

# Build without tests (not recommended)
mvn clean install -DskipTests
```

### Testing

```bash
# Run all tests
mvn clean test

# Run specific test class
mvn test -Dtest=CompaniesHouseClientImplTest

# Run specific test method
mvn test -Dtest=CompaniesHouseClientImplTest#testGetRegisteredAddress_Success

# Run tests with coverage report
mvn clean test jacoco:report
```

### Viewing Coverage

```bash
# Generate coverage report
mvn clean test jacoco:report

# Open report in browser (macOS)
open target/site/jacoco/index.html

# Open report in browser (Linux)
xdg-open target/site/jacoco/index.html

# Open report in browser (Windows)
start target/site/jacoco/index.html
```

### JavaDoc

```bash
# Generate JavaDoc
mvn javadoc:javadoc

# View JavaDoc
open target/site/apidocs/index.html
```

### Progress Tracking

During implementation (Prompt 04), progress is tracked in `.work/implementation/`:

```bash
# Check current task
cat .work/implementation/progress.yaml

# See all task statuses
cat .work/implementation/task-status.yaml

# View implementation commits
git log --oneline | grep "T[0-9]:"
```

---

## Context Compaction Survival

If context is compacted during implementation (Prompt 04):

1. **Progress is saved** in `.work/implementation/progress.yaml`
2. **Read `next_action`** to know what to do next
3. **Resume from that point** in the TDD cycle
4. **Commit frequently** to preserve work

### Progress File Format

```yaml
# .work/implementation/progress.yaml
current_task: T7
status: in_progress
phase: GREEN
next_action: "Implement RestClient GET call to pass test"
last_file_created: "CompaniesHouseClientImplTest.java"
```

### Task Status File Format

```yaml
# .work/implementation/task-status.yaml
T1: completed
T2: completed
T3: completed
T4: completed
T5: completed
T6: completed
T7: in_progress
T8: pending
T10: pending
T11: pending
```

---

## Next Steps

This implementation plan feeds into **Prompt 04: TDD Implementation**, which will:

1. Initialize project structure (T1)
2. Execute T1-T11 following Red-Green-Refactor cycle
3. Write tests first, then implementation
4. Commit working code after each task
5. Track progress in `.work/implementation/` for context compaction survival
6. Update progress.yaml constantly for session resumption

**SUCCESS CRITERIA**: When Prompt 04 completes:
- All tests pass (`mvn clean test`)
- Coverage meets targets (`mvn clean test jacoco:report`)
- All 11 tasks committed with proper messages
- Library is ready for integration

---

**Document Version**: 1.0
**Last Updated**: 2026-01-27
**Status**: Complete - Ready for TDD Implementation phase
