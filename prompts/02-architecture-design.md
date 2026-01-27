# 02 - Architecture Design Prompt

> **Status**: Ready for use
> **Purpose**: Design layered architecture with component structure, interfaces, and decisions
> **Input**: `docs/requirements.md`, STD-003 rubric
> **Output**: `docs/architecture.md`
> **Complexity**: MODERATE - Usually single session but could extend

---

<context>
<project>
Companies House API Integration - Client Library

You are designing the architecture for a Java Spring Boot client library that retrieves company registered addresses from the Companies House API. This library will be built using TDD and must be portable to integration into a larger project.
</project>

<role>
Software architect. You will design a layered, testable, extensible architecture that follows Spring Boot best practices and enables the implementation team to build with confidence.
</role>

<objective>
Design a complete system architecture that:
- Defines the layered structure (Client, Service, DTOs, Config, Exceptions)
- Makes key architectural decisions with Architecture Decision Records (ADRs)
- Specifies the complete package and file structure
- Ensures the design is testable, portable, and maintainable
- Aligns with Spring Boot best practices (STD-003)
</objective>

<requirements_document>
Read this FIRST: `/Users/andym/Dropbox/_home/Development/bh/CompanyHouse/company-house-api/docs/requirements.md`

This will give you the context for what the architecture must support.
</requirements_document>

<development_standards>
Reference this throughout: `/Users/andym/Dropbox/_home/Development/bh/CompanyHouse/company-house-api/prompts/rubrics/STD-003-java-spring-boot-development-rubric.md`

Key patterns to apply:
- Layered architecture (separation of concerns)
- Dependency injection (constructor injection, no field injection)
- Type safety (proper generics, no raw types)
- Configuration externalization
- Custom exceptions with context
- Testing at multiple levels (unit, integration)
</development_standards>
</context>

<foundational_principles>
1. **Separation of Concerns** - Each layer has a single, well-defined responsibility
2. **Dependency Injection** - All dependencies injected via constructor, not field injection
3. **Configuration Externalization** - API URL, API key, timeouts in application.yml
4. **Type Safety** - Proper generic types, Optional for nullable values, no raw types
5. **Testability** - Design enables unit testing (mocks) and integration testing (test doubles)
6. **Portability** - Package structure allows extraction to other projects without modification
7. **Error Handling** - Custom exceptions extend RuntimeException, include context information
8. **Security** - No hardcoded credentials, HTTPS enforced, secure defaults
</foundational_principles>

<instructions>

## Phase 1: High-Level Component Design

Design the major components and their responsibilities:

### Component 1: REST Template Configuration
**Responsibility**: Configure HTTP client for Companies House API calls
- Create `RestTemplate` bean with proper timeouts
- Set up HTTP interceptors for API key injection
- Configure SSL/TLS (HTTPS)
- Handle redirects

**Class**: `CompaniesHouseConfig.java` (@Configuration)
- Spring @Configuration class
- Bean method for RestTemplate
- Bean method for RestTemplateBuilder
- Documentation on timeout choices

### Component 2: Configuration Properties
**Responsibility**: Hold externalized configuration values
- API base URL
- API key (from environment or application-local.yml)
- Connection timeout
- Read timeout
- Any other configurable values

**Class**: `CompaniesHouseProperties.java` (@ConfigurationProperties)
- Uses @ConfigurationProperties(prefix = "companies-house")
- Uses Lombok @Data for getters/setters
- Validation annotations (@NotBlank, @Positive, etc.)
- Default values where appropriate

### Component 3: API Client Interface
**Responsibility**: Define the public API for Companies House integration
- Method signature for retrieving registered address
- Return type (Optional<RegisteredAddressResponse> or similar)
- Documented behavior (what exceptions can be thrown)

**Interface**: `CompaniesHouseClient.java`
```java
public interface CompaniesHouseClient {
    /**
     * Get the registered office address for a company
     */
    Optional<RegisteredAddressResponse> getRegisteredAddress(String companyNumber);
}
```

### Component 4: API Client Implementation
**Responsibility**: Implement HTTP calls to Companies House API
- Handle RestTemplate calls
- Parse responses into DTOs
- Error handling and exception mapping

**Class**: `CompaniesHouseClientImpl.java`
- Injected: RestTemplate, CompaniesHouseProperties
- Constructor injection only
- Decorated with @Component or @Service

### Component 5: DTOs (Data Transfer Objects)
**Responsibility**: Map Companies House API responses to Java objects

**Response DTOs** (in `dto.response` package):
- `RegisteredAddressResponse` - Complete address from API
- `ApiErrorResponse` - Error structure from API

**Error DTOs** (in `dto.error` package):
- `ErrorDetails` - Our error response structure

### Component 6: Custom Exceptions
**Responsibility**: Provide specific, informative exceptions for each error scenario

**Exceptions** (in `client.exception` package):
- `CompaniesHouseApiException` - Base exception (extends RuntimeException)
- `CompanyNotFoundException` - 404 Not Found
- `RateLimitExceededException` - 429 Too Many Requests
- `InvalidCompanyNumberException` - Validation error

### Component 7: Global Exception Handler (Optional)
**Responsibility**: Convert exceptions to HTTP responses (if exposing as REST API later)

**Class**: `GlobalExceptionHandler.java` (@RestControllerAdvice)
- Only needed if this becomes a microservice
- For library-only use, this can be deferred

### Component Diagram

```
User Code
    ↓
CompaniesHouseClient (interface)
    ↓
CompaniesHouseClientImpl (@Component)
    ↓ (depends on)
    ├── RestTemplate (bean from CompaniesHouseConfig)
    ├── CompaniesHouseProperties (@ConfigurationProperties)
    └── Custom Exceptions (CompanyNotFoundException, etc.)
    ↓ (returns)
RegisteredAddressResponse, ApiErrorResponse (DTOs)
```

## Phase 2: Architecture Decision Records (ADRs)

Create detailed ADRs for each major decision:

### ADR-001: HTTP Client Library

**Context**:
We need to make HTTP calls to Companies House API. Spring Boot offers two main options.

**Decision**:
Use RestTemplate (not WebClient)

**Rationale**:
- RestTemplate: Synchronous, simpler to understand, built-in to Spring Boot
- WebClient: Asynchronous, reactive, better for high concurrency
- For a client library that most applications will use synchronously, RestTemplate is simpler and more approachable
- RestTemplate is easier to mock in unit tests
- WebClient can be added later if needed

**Consequences**:
- Synchronous calls (blocks until response)
- Cannot handle non-blocking I/O scenarios
- Simpler test setup vs WebClient

**Alternatives Considered**:
- WebClient (reactive, but adds complexity)
- Apache HttpClient (overkill for Spring Boot)
- OkHttp (unnecessary when RestTemplate exists)

### ADR-002: Synchronous vs Asynchronous API

**Context**:
Client applications need to retrieve company addresses. Should the client be blocking or non-blocking?

**Decision**:
Use synchronous (blocking) API

**Rationale**:
- Most applications need the address immediately before proceeding
- Synchronous is simpler to understand and use
- Matches REST conventions (request → response)
- Can be wrapped in async later if needed (CompletableFuture, etc.)

**Consequences**:
- Caller's thread blocks until response received
- Cannot handle extreme high-concurrency scenarios
- Simpler implementation and testing

**Alternatives Considered**:
- CompletableFuture return types (adds complexity)
- Reactive streams (overkill for simple use case)

### ADR-003: Error Handling Strategy

**Context**:
Companies House API returns various error codes (404, 429, 500, etc.). How should we handle them?

**Decision**:
Custom exceptions extending RuntimeException, specific exception type for each error category

**Rationale**:
- RuntimeException means caller can choose whether to catch/handle
- Checked exceptions force error handling (verbose)
- Specific exception types (CompanyNotFoundException, RateLimitExceededException) enable precise handling
- Each exception includes context (company number, HTTP status, original message)

**Consequences**:
- Caller must understand exception hierarchy
- Clear contract about what can go wrong
- Enables retry logic based on exception type

**Alternatives Considered**:
- Return error codes in response object (verbose, ambiguous)
- Throw generic Exception (loses specificity)
- Checked exceptions (verbose try-catch everywhere)

### ADR-004: Retry and Resilience Strategy

**Context**:
API calls can fail temporarily (network hiccup, API temporarily down). Should we implement retries?

**Decision**:
Implement simple built-in retry for specific scenarios; consider Resilience4j for circuit breaker in future

**Rationale**:
- Some errors are transient and worth retrying (connection timeout)
- Some are permanent and shouldn't retry (404, invalid API key)
- Built-in retry keeps dependencies minimal
- Resilience4j can be added if needed for circuit breaker pattern

**Consequences**:
- Adds retry logic to client implementation
- Slightly longer response time on transient failures
- Resilience4j adds dependency (deferred)

**Alternatives Considered**:
- No retries (brittle)
- Always retry all errors (inefficient)
- Spring Retry (different approach, also valid)

### ADR-005: API Key Configuration

**Context**:
API key must be provided to authenticate with Companies House API. How should this be configured?

**Decision**:
Externalize via `application.yml` with `application-local.yml` override for local development

**Rationale**:
- `application.yml` has placeholder/default values
- `application-local.yml` (gitignored) has actual API key for local dev
- Prod can override via environment variable: `companies-house.api.key: ${COMPANIES_HOUSE_API_KEY}`
- Never hardcoded in source code

**Consequences**:
- Requires creation of application-local.yml per developer
- Clear separation of committed defaults vs local overrides
- Matches 12-factor app principles

**Alternatives Considered**:
- Hardcoded in code (WRONG - security risk)
- Only environment variables (inflexible for dev)
- Spring profiles (more complex, good for prod but overkill for this)

### ADR-006: DTO Mapping Strategy

**Context**:
Companies House API returns JSON. Need to map to Java objects. Should we use manual mapping, MapStruct, or something else?

**Decision**:
Manual mapping (for simplicity) with Lombok @Data for getters/setters

**Rationale**:
- Manual mapping is straightforward for simple objects
- No extra dependencies (MapStruct adds complexity)
- Easy to debug - mapping logic is visible
- Can add MapStruct later if more complex mapping needed

**Consequences**:
- Manual @Data getters/setters (boilerplate but minimal)
- Explicit mapping logic
- Easier to understand for maintainers

**Alternatives Considered**:
- MapStruct (overkill for simple cases)
- Jackson annotations only (less control)

### ADR-007: Package Structure for Portability

**Context**:
This library will eventually be integrated into a larger project. How should packages be organized?

**Decision**:
Feature-based organization within `com.example.companieshouse` package

**Rationale**:
- Packages organized by feature (client, dto, config, exception)
- Enables easy package rename when moving to larger project
- Clear separation makes extraction straightforward
- No dependency on specific application structure

**Consequences**:
- Must rename entire package when integrating to larger project
- Clear boundaries between components
- Easy to move as a module

**Alternatives Considered**:
- Layer-based at top level (com.example.client, com.example.service) - harder to move
- Feature-based at top level with multiple features - not needed yet

## Phase 3: Complete Package and File Structure

Define all files that will be created:

```
com.example.companieshouse/
│
├── client/
│   ├── CompaniesHouseClient.java (interface)
│   │   └── public Optional<RegisteredAddressResponse> getRegisteredAddress(String companyNumber)
│   │
│   ├── CompaniesHouseClientImpl.java (implementation)
│   │   ├── @Component
│   │   ├── Constructor injection: RestTemplate, CompaniesHouseProperties
│   │   ├── Handles: HTTP calls, response mapping, error handling
│   │   └── Throws custom exceptions
│   │
│   └── exception/
│       ├── CompaniesHouseApiException.java (base exception)
│       ├── CompanyNotFoundException.java (404)
│       ├── RateLimitExceededException.java (429)
│       └── InvalidCompanyNumberException.java (validation)
│
├── dto/
│   ├── response/
│   │   ├── RegisteredAddressResponse.java (success response)
│   │   │   └── Fields: addressLine1, addressLine2, ..., postalCode, country, etc.
│   │   │
│   │   └── ApiErrorResponse.java (API error response)
│   │       └── Fields: errorNumber, message, type, etc.
│   │
│   └── error/
│       └── ErrorDetails.java (our error response)
│           └── Fields: status, message, timestamp, etc.
│
└── config/
    ├── CompaniesHouseConfig.java (@Configuration)
    │   ├── @Bean RestTemplate restTemplate()
    │   └── @Bean RestTemplateBuilder restTemplateBuilder()
    │
    └── CompaniesHouseProperties.java (@ConfigurationProperties)
        ├── @ConfigurationProperties(prefix = "companies-house")
        ├── Fields: baseUrl, apiKey, connectTimeout, readTimeout
        └── Validation annotations (@NotBlank, @Positive, etc.)
```

### File Responsibilities

| File | Purpose | Key Methods | Dependencies |
|------|---------|------------|--------------|
| CompaniesHouseClient | Public API interface | getRegisteredAddress() | (none - interface) |
| CompaniesHouseClientImpl | HTTP call handler | getRegisteredAddress() | RestTemplate, CompaniesHouseProperties |
| CompaniesHouseConfig | Spring configuration | RestTemplate bean | (Spring) |
| CompaniesHouseProperties | Config properties | getters/setters | (Spring) |
| RegisteredAddressResponse | Success DTO | getters/setters | (none) |
| ApiErrorResponse | Error DTO | getters/setters | (none) |
| ErrorDetails | Error details | getters/setters | (none) |
| CompaniesHouseApiException | Base exception | constructor | (none) |
| CompanyNotFoundException | 404 exception | constructor | CompaniesHouseApiException |
| RateLimitExceededException | 429 exception | constructor | CompaniesHouseApiException |
| InvalidCompanyNumberException | Validation | constructor | RuntimeException |

### File Naming Conventions

- **Classes**: PascalCase (e.g., `CompaniesHouseClient`, `RegisteredAddressResponse`)
- **Interfaces**: PascalCase, no "I" prefix (e.g., `CompaniesHouseClient`)
- **Packages**: lowercase.dot.separated (e.g., `com.example.companieshouse.client`)
- **Constants**: UPPER_CASE (e.g., `DEFAULT_TIMEOUT_MILLIS`)
- **Methods**: camelCase (e.g., `getRegisteredAddress`)
- **Variables**: camelCase (e.g., `companyNumber`)

### Spring Bean Naming

- RestTemplate bean: typically default (can be accessed as `restTemplate()`)
- CompaniesHouseClient bean: typically default
- CompaniesHouseProperties: loaded automatically by Spring

</instructions>

<output_specifications>

Create a comprehensive architecture document at:

```
/Users/andym/Dropbox/_home/Development/bh/CompanyHouse/company-house-api/docs/architecture.md
```

### Document Structure

```markdown
# Companies House API Integration - Architecture Design

## Overview
High-level architecture summary

## Component Architecture

### Components
Description of each major component

### Component Diagram
ASCII or Mermaid diagram showing relationships

### Component Responsibilities
Table of components and their purposes

## Architecture Decision Records (ADRs)

### ADR-001: HTTP Client Library
- Context: ...
- Decision: ...
- Rationale: ...
- Consequences: ...
- Alternatives Considered: ...

### ADR-002 through ADR-007
[Each ADR with same structure]

## Package Structure

### Directory Layout
Complete package structure with file purposes

### File Organization
Table showing each file and its responsibility

### Module Boundaries
Clear description of what each module includes/excludes

## Configuration Design

### application.yml Structure
```yaml
companies-house:
  api:
    base-url: https://api.company-information.service.gov.uk
    key: ${COMPANIES_HOUSE_API_KEY:REPLACE_IN_LOCAL_FILE}
    connect-timeout-ms: 5000
    read-timeout-ms: 10000
```

### Environment-Specific Configuration
How to override for dev/test/prod

### API Key Management
How to securely handle API key

## Testing Strategy

### Unit Testing Approach
- Mock RestTemplate
- Test client implementation with mocked responses
- Test exception mapping
- Test error handling

### Integration Testing Approach
- Use WireMock to simulate Companies House API
- Test end-to-end scenarios
- Test actual Spring configuration loading

### Test Doubles
- WireMock for API mocking
- MockRestServiceServer for RestTemplate testing

## Dependencies

### Spring Boot Starters
- spring-boot-starter-web (for RestTemplate)

### Testing Dependencies
- spring-boot-starter-test (JUnit 5, Mockito)
- com.github.tomakehurst:wiremock-jre8 (or latest version)

### Utilities
- org.projectlombok:lombok (for @Data, @RequiredArgsConstructor)

### Version Recommendations
[List versions to use]

## Security Considerations

### API Key Protection
- Never hardcoded
- Externalized via configuration
- Environment variable in production

### HTTPS Enforcement
- RestTemplate configured for HTTPS
- No SSL verification bypasses

### Error Messages
- No sensitive data in exceptions
- No API key in logs

## Extensibility Considerations

### Adding New Endpoints
If more Companies House API endpoints needed:
1. Add new method to CompaniesHouseClient interface
2. Implement in CompaniesHouseClientImpl
3. Create new response DTO
4. Add specific exceptions if needed

### Switching HTTP Clients
If need to switch from RestTemplate to WebClient:
- Only CompaniesHouseClientImpl changes
- Interface remains the same
- Callers unaffected

## Next Steps
This architecture document feeds into the Planning prompt, which will create task breakdown and file-by-file specification.
```

### Document Quality Checklist

Your architecture document should have:
- ✅ All 7 ADRs with context, decision, rationale, consequences
- ✅ Complete package structure with all files listed
- ✅ Clear responsibilities for each file
- ✅ Configuration design with application.yml structure
- ✅ Testing strategy (unit, integration, test doubles)
- ✅ Dependencies listed with versions
- ✅ Security considerations addressed
- ✅ Extensibility guidance

</output_specifications>

<critical_reminders>

### 1. Follow STD-003 Strictly
- Constructor injection (@RequiredArgsConstructor) NOT field injection
- Proper generics (List<User> not List)
- Optional<T> for nullable values (not null returns)
- Custom exceptions that are specific and informative
- No hardcoded configuration

### 2. Make Real Decisions
- Each ADR should justify WHY you chose an option
- "RestTemplate vs WebClient" - explain the trade-off
- "Synchronous vs Asynchronous" - explain the implication
- Don't leave decisions ambiguous

### 3. Think About Portability
- This will move to another project
- Package structure should be extractable without modification
- No assumptions about where it runs
- Clean boundaries between components

### 4. Be Specific
- ❌ "Configure timeouts" → ✅ "Default connect timeout 5000ms, read timeout 10000ms"
- ❌ "Handle errors" → ✅ "CompanyNotFoundException for 404, RateLimitExceededException for 429"
- ❌ "Support testing" → ✅ "RestTemplate is mockable; integration tests use WireMock"

### 5. Design for Testing
- Every component should be mockable
- RestTemplate should be injected (not created in place)
- Properties externalized (not hardcoded)
- Exceptions enable different error handling

### 6. Document Trade-offs
- Nothing is perfect
- Each decision has consequences
- RestTemplate is simpler but synchronous
- Manual mapping is explicit but verbose
- Document the trade-offs honestly

### 7. Reference Real Examples
- When writing ADRs, reference actual code patterns
- When describing package structure, show actual paths
- When describing testing, be specific about WireMock setup

</critical_reminders>

<begin>

================================================================================
                    BEGIN ARCHITECTURE DESIGN
================================================================================

**FIRST ACTION**: Read the requirements document (docs/requirements.md) to understand what you're designing for.

**SECOND ACTION**: Read the STD-003 Java Spring Boot development rubric to understand the patterns and standards you should follow.

**THIRD ACTION**: Work through the three phases systematically:

### Phase 1: High-Level Component Design
- Understand each component's responsibility
- Create the component diagram
- Define the layer structure

### Phase 2: Architecture Decision Records
- Create detailed ADRs for all 7 key decisions
- Justify each decision with context and rationale
- Document consequences and alternatives considered

### Phase 3: Package and File Structure
- Define complete package hierarchy
- Specify every file to be created
- Document file responsibilities
- Create naming conventions

**OUTPUT**: Create the complete architecture document at `docs/architecture.md` with:
- Component overview and diagram
- All 7 ADRs (fully detailed)
- Complete package structure
- Configuration design
- Testing strategy
- Dependencies
- Security considerations

**SUCCESS**: When you're done, someone reading this document should understand:
- Exactly what code needs to be written
- Why each component exists
- Why specific decisions were made
- How components interact
- How to test each component
- How to extend the architecture

================================================================================

</begin>

