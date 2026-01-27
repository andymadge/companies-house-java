# 01 - Requirements Gathering Prompt

> **Status**: Ready for use
> **Purpose**: Guide discovery of functional and non-functional requirements for Companies House API integration
> **Output**: `docs/requirements.md`
> **Complexity**: SIMPLE - Single session, no context compaction needed

---

<context>
<project>
Companies House API Integration - Client Library

Retrieve company registered office addresses by integrating with the UK Companies House Public Data API. The result will be a reusable Java Spring Boot client library that can be integrated into a larger project.
</project>

<role>
Requirements analyst and API specialist. You will gather detailed requirements by analyzing the Companies House API, understanding what data is available, what the constraints are, and what the integration must support.
</role>

<objective>
Create a comprehensive requirements document that:
- Defines functional requirements (what the system must do)
- Defines non-functional requirements (performance, security, reliability)
- Documents the Companies House API capabilities and constraints
- Defines the data model (request and response DTOs)
- Establishes acceptance criteria to determine when the integration is complete
</objective>

<api_documentation>
You will analyze the following Companies House API documentation:

**Overview**: https://developer.company-information.service.gov.uk/overview
- General API structure, authentication approach, rate limits

**Company Profile Endpoint**: https://developer-specs.company-information.service.gov.uk/companies-house-public-data-api/reference/company-profile/company-profile
- GET /company/{companyNumber}
- Returns complete company profile including registered office address

**Registered Office Address Endpoint**: https://developer-specs.company-information.service.gov.uk/companies-house-public-data-api/reference/registered-office-address/registered-office-address
- GET /company/{companyNumber}/registered-office-address
- Returns just the registered office address for a company

You will need to determine which endpoint is most appropriate for this integration.
</api_documentation>
</context>

<foundational_principles>
1. **Be Specific** - Requirements must be testable and measurable, not vague aspirations
2. **Document Constraints** - Know what the API doesn't support, rate limits, timeouts
3. **Think Error Cases** - Requirements must cover success AND failure paths
4. **Consider Portability** - This library will move to a larger project - structure accordingly
5. **Security First** - No hardcoded credentials, no sensitive data in logs
6. **Testability** - Requirements should make it clear what standalone testing is possible
7. **Reference Standards** - Align with Spring Boot best practices (STD-003)
</foundational_principles>

<instructions>

## Step 1: API Discovery and Analysis

Using WebFetch, retrieve and analyze the Companies House API documentation:

1. **Fetch Overview Documentation**
   - Get https://developer.company-information.service.gov.uk/overview
   - Extract: API structure, authentication methods, rate limits, rate limit details, general constraints

2. **Fetch Company Profile Endpoint Spec**
   - Get https://developer-specs.company-information.service.gov.uk/companies-house-public-data-api/reference/company-profile/company-profile
   - Extract: Endpoint URL, HTTP method, path parameters, query parameters, authentication requirements, success response format, error response codes, example response

3. **Fetch Registered Office Address Endpoint Spec**
   - Get https://developer-specs.company-information.service.gov.uk/companies-house-public-data-api/reference/registered-office-address/registered-office-address
   - Extract: Endpoint URL, HTTP method, path parameters, authentication requirements, success response format, error response codes, example response

4. **Endpoint Comparison**
   - Compare both endpoints: which provides the registered address?
   - Determine which is most appropriate (likely the specific registered-office-address endpoint for simplicity)
   - Document pros/cons of each approach

5. **Document Authentication**
   - Authentication method (likely API key)
   - How to pass API key (header, query param, etc.)
   - Any authentication requirements or setup

6. **Document Rate Limits**
   - What are the rate limits?
   - How are rate limits signaled (HTTP headers, response codes)?
   - What happens when rate limit is exceeded?
   - Implications for retry strategy

## Step 2: Define Functional Requirements

Based on API analysis, define what the system MUST do:

### FR-1: Retrieve Registered Address
- **Objective**: Client can retrieve the registered office address for a UK company
- **Input**: Company number (e.g., "09370669")
- **Output**: Registered office address with full details
- **Specifics to define**:
  - What constitutes a valid company number format?
  - What fields are essential in the address response?
  - What should happen if company not found?
  - Should we support multiple company lookups?

### FR-2: Handle Missing/Invalid Companies
- **Objective**: Gracefully handle requests for non-existent companies
- **Specify**: What exception/error response should be returned? Should it be retryable?

### FR-3: Handle API Errors and Rate Limiting
- **Objective**: Properly handle API errors (500 errors, rate limit 429, etc.)
- **Specify**: What error codes should trigger retries? What should be permanent failures?

### FR-4: Handle Network Failures
- **Objective**: Handle network timeouts and connection failures
- **Specify**: Default timeout values, retry behavior, fallback behavior

### FR-5: API Key Configuration
- **Objective**: Support passing API key securely without hardcoding
- **Specify**: How should API key be provided to the client?

(Add more functional requirements as needed based on API analysis)

## Step 3: Define Non-Functional Requirements

### Performance Requirements
- **Response Time Target**: What's acceptable? (e.g., <2 seconds for single lookup)
- **Timeout Values**: Default HTTP request timeout, connection timeout
- **Throughput**: Expected queries per second?

### Security Requirements
- **No Hardcoded Credentials**: API keys must be configurable, never in source code
- **HTTPS Enforcement**: All API calls must use HTTPS
- **Secure Defaults**: No logging of sensitive data (company numbers? addresses?)
- **Authentication**: Support standard Spring Security patterns for future integration

### Reliability Requirements
- **Error Handling**: Specific exception types for different error scenarios
- **Retry Logic**: Should we retry failed requests? Under what conditions?
- **Circuit Breaker**: Should we implement circuit breaker pattern?
- **Logging**: Appropriate logging without exposing sensitive data

### Testability Requirements
- **Standalone Testing**: Can be tested in isolation (during development)
- **Mock-Friendly**: Code should support mocking (RestTemplate, etc.)
- **Test Data**: Can we use public test company numbers?
- **No External Dependencies**: Unit tests don't require real API calls

### Maintainability Requirements
- **Code Organization**: Clear separation of concerns (client layer, DTOs, config)
- **Documentation**: JavaDoc on public methods, clear error messages
- **Extensibility**: Easy to add support for other endpoints later
- **Portability**: Can be extracted and integrated into other projects

### Configuration Requirements
- **Externalized Config**: API base URL, API key, timeouts in application.yml
- **Environment-Specific Config**: Different configs for dev/test/prod
- **Override Capability**: Can override config for local development

## Step 4: Define Data Model

Based on API responses, define the DTOs:

### Input
- **CompanyLookupRequest** (or just use String for company number?)
  - Company number (required)
  - Validation rules (format, length)

### Output - Success Response
- **RegisteredAddressResponse**
  - Address lines (address_line_1, address_line_2, etc.)
  - Postal code
  - Country
  - Care of name (optional)
  - Po box (optional)
  - Any other fields from API response

### Output - Error Response
- **ErrorDetails** / **ApiErrorResponse**
  - Error code
  - Error message
  - HTTP status
  - Timestamp
  - Any other relevant error information

### Exceptions
- **CompaniesHouseApiException** (base exception)
- **CompanyNotFoundException** (404)
- **RateLimitExceededException** (429)
- **InvalidCompanyNumberException** (validation)

## Step 5: Define Acceptance Criteria

List testable criteria that determine when this integration is "done":

### Functional Acceptance Criteria
- [ ] Can successfully retrieve registered address for valid company number
- [ ] Returns 404 error (CompanyNotFoundException) for non-existent company
- [ ] Returns 429 error (RateLimitExceededException) when rate limit exceeded
- [ ] Handles 500 server errors gracefully
- [ ] Handles network timeout gracefully
- [ ] Returns proper DTOs with all required fields
- [ ] Rejects invalid company numbers with clear error message
- [ ] API key can be configured via application.yml without hardcoding

### Non-Functional Acceptance Criteria
- [ ] All API calls use HTTPS
- [ ] API key is never logged or exposed in error messages
- [ ] Response time for single lookup < 2 seconds (typical)
- [ ] Proper exception types thrown for each error scenario
- [ ] Code follows Spring Boot best practices (STD-003)
- [ ] No hardcoded configuration values
- [ ] Constructor injection used throughout
- [ ] Proper generics (no raw types)

### Testing Acceptance Criteria
- [ ] 80%+ code coverage on client implementation
- [ ] Unit tests mock HTTP calls (no real API calls)
- [ ] Integration tests use WireMock to simulate API
- [ ] Both success and error paths tested
- [ ] Edge cases tested (empty input, null values, malformed responses)

### Documentation Acceptance Criteria
- [ ] JavaDoc on all public methods
- [ ] README.md with setup instructions
- [ ] README.md with usage examples
- [ ] Error handling documented
- [ ] Configuration documented

</instructions>

<output_specifications>

Create a comprehensive requirements document at:

```
/Users/andym/Dropbox/_home/Development/bh/CompanyHouse/company-house-api/docs/requirements.md
```

### Document Structure

```markdown
# Companies House API Integration - Requirements

## Executive Summary
Brief overview of what this integration is for, what it does

## API Analysis

### Endpoint Selection
Which endpoint (company profile vs registered-office-address) and why?

### Endpoint Specification
- URL
- HTTP Method
- Path Parameters
- Authentication
- Success Response Format (with example)
- Error Response Codes

### Rate Limits
- Requests per time period
- How rate limiting is signaled

### Authentication
- Method
- How to provide API key
- Any special requirements

## Functional Requirements

### FR-1: Retrieve Registered Address
- Objective
- Input/Output
- Specifics

### FR-2 through FR-5
[Continue for all functional requirements]

## Non-Functional Requirements

### Performance
- Response time targets
- Timeout values
- Throughput expectations

### Security
- No hardcoded credentials
- HTTPS enforcement
- Data sensitivity handling

### Reliability
- Error handling approach
- Retry strategy
- Circuit breaker consideration

### Testability
- Standalone testing capability
- Mock-friendly design
- Unit vs integration test approach

### Maintainability
- Code organization
- Documentation standards
- Extensibility

## Data Model

### Input DTOs
- CompanyLookupRequest
  - Company number
  - Validation rules

### Output DTOs - Success
- RegisteredAddressResponse
  - [All address fields from API]

### Output DTOs - Error
- ErrorDetails
  - [All error fields]

### Exception Types
- CompaniesHouseApiException (base)
- CompanyNotFoundException (404)
- RateLimitExceededException (429)
- InvalidCompanyNumberException (validation)

## Acceptance Criteria

### Functional
- [ ] Retrieve registered address for valid company
- [ ] Handle 404 for non-existent company
- [ ] Handle 429 rate limit
- [ ] Handle 500 server errors
- [ ] Handle network timeouts
- [ ] API key configurable without hardcoding
- [More items...]

### Non-Functional
- [ ] HTTPS enforced
- [ ] No sensitive data in logs
- [ ] Response time < 2 seconds
- [ ] Proper exception hierarchy
- [ ] STD-003 compliance
- [More items...]

### Testing
- [ ] 80%+ code coverage
- [ ] Unit tests mock HTTP
- [ ] Integration tests use WireMock
- [ ] Success and error paths tested
- [More items...]

### Documentation
- [ ] JavaDoc on public methods
- [ ] README with setup
- [ ] Usage examples
- [More items...]

## Next Steps
This requirements document feeds into the Architecture Design prompt.
```

### Deliverable Quality
The requirements document should be:
- **Specific** - No vague language, all requirements testable
- **Complete** - Covers all functional and non-functional aspects
- **Clear** - Someone reading it understands exactly what to build
- **Traceable** - Each requirement can be linked to design decisions and test cases

</output_specifications>

<critical_reminders>

### 1. Analyze API Documentation Thoroughly
- Actually fetch and read the API docs using WebFetch
- Don't guess at endpoints, parameters, or response formats
- Document exact URLs, HTTP methods, parameter names
- Include actual example responses

### 2. Be Specific About Requirements
- ❌ "Fast response time" → ✅ "Response time < 2 seconds for single lookup"
- ❌ "Handle errors" → ✅ "Specific exception types for 404, 429, 500, timeout"
- ❌ "Secure" → ✅ "API key never hardcoded, never logged, passed via environment variable"

### 3. Think About Error Scenarios
- What happens when company doesn't exist?
- What happens when rate limit is hit?
- What happens when API returns 500?
- What happens when network times out?
- What if response is malformed JSON?

### 4. Consider Portability
- This will move to a larger project
- Requirements should support clean package extraction
- No dependencies on specific Spring Boot beans or application context
- Client library should work standalone

### 5. Reference STD-003 Standards
- Requirements should align with Spring Boot best practices
- Mention constructor injection, proper generics, custom exceptions
- Mention testing pyramid (unit > integration > e2e)
- Mention no hardcoded configuration

### 6. Define Clear Data Models
- List exact fields for request/response DTOs
- Specify validation rules (required fields, formats, lengths)
- Map Companies House API response fields to DTO fields
- Define exception hierarchy

### 7. Acceptance Criteria Must Be Testable
- Each criterion should be verifiable
- Include measurable metrics (coverage %, response time)
- Include specific test scenarios
- Include documentation requirements

</critical_reminders>

<begin>

================================================================================
                    BEGIN REQUIREMENTS GATHERING
================================================================================

**FIRST ACTION**: Fetch and analyze the Companies House API documentation using WebFetch.

1. Start with: https://developer.company-information.service.gov.uk/overview
2. Then fetch: Company Profile endpoint specification
3. Then fetch: Registered Office Address endpoint specification
4. Compare the endpoints and choose which is most appropriate

**SECOND**: Work through the 5 instruction steps systematically:
   - Step 1: API Discovery (you'll complete this with WebFetch)
   - Step 2: Functional Requirements
   - Step 3: Non-Functional Requirements
   - Step 4: Data Model Definition
   - Step 5: Acceptance Criteria

**OUTPUT**: Create the complete requirements document at `docs/requirements.md` with all sections filled in, based on your analysis.

**SUCCESS**: When you're done, the requirements document should be detailed enough that someone reading it understands exactly what needs to be built, what the constraints are, and how to know when it's complete.

================================================================================

</begin>

