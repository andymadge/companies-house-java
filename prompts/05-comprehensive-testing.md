# 05 - Comprehensive Testing Prompt

> **Status**: Ready for use
> **Purpose**: Execute comprehensive testing and generate test report
> **Input**: `src/` code, STD-003 rubric
> **Output**: `docs/test-report.md`, JaCoCo coverage reports
> **Complexity**: MODERATE - Single session for testing phase

---

<context>
<project>
Companies House API Integration - Client Library

You are testing the completed implementation of the Companies House API integration. This prompt executes comprehensive testing to verify the code meets requirements and quality standards.
</project>

<role>
Quality assurance engineer and test lead. You will verify the implementation through comprehensive testing, validate code coverage, check for edge cases and error scenarios, and generate a detailed test report.
</role>

<objective>
Execute comprehensive testing that:
- Verifies all existing unit tests pass
- Identifies and adds any missing test cases
- Executes integration tests with realistic scenarios
- Measures code coverage (target 80%+)
- Validates STD-003 compliance
- Generates a test report with findings and recommendations
- Ensures the library is production-ready
</objective>

<source_code>
Test the complete implementation in: `src/main/java/com/example/companieshouse/`
Tests located in: `src/test/java/com/example/companieshouse/`
Configuration: `src/main/resources/application.yml`, `src/test/resources/application-test.yml`
</source_code>

<development_standards>
Reference the STD-003 Java Spring Boot development rubric:
`prompts/rubrics/STD-003-java-spring-boot-development-rubric.md`

Key testing standards to verify:
- Unit tests for business logic (mocked dependencies)
- Integration tests for Spring context and database interactions
- Test independence (each test can run standalone)
- Test names clearly describe what's being tested (@DisplayName)
- Both success and failure paths tested
- Edge cases covered (null, empty, invalid input)
- Arrange-Act-Assert pattern in tests
- 70%+ code coverage on core logic
</development_standards>
</context>

<foundational_principles>
1. **Testing Pyramid** - Many unit tests (mocked), fewer integration tests (realistic), few e2e tests
2. **Test Independence** - Each test can run standalone in any order
3. **Descriptive Names** - Test methods/classes clearly describe what's being tested
4. **Arrange-Act-Assert** - Clear structure: setup → execute → verify
5. **Test Both Paths** - Success AND failure scenarios for each feature
6. **Real-World Scenarios** - Test rate limits, timeouts, API errors, malformed responses
7. **Coverage Not Everything** - 100% coverage doesn't mean good tests; focus on important code
</foundational_principles>

<instructions>

## Phase 1: Unit Test Verification

### Step 1A: Run Existing Unit Tests

Execute the existing unit test suite:

```bash
# Navigate to project directory
cd /Users/andym/Dropbox/_home/Development/bh/CompanyHouse/company-house-api

# Run all unit tests
mvn clean test

# Run specific test class if needed
mvn test -Dtest=CompaniesHouseClientImplTest
```

Review the output:
- How many tests passed?
- Are there any failures?
- Are there test compilation errors?

If tests fail, note:
- Which test failed?
- What was the error?
- Is it a real bug or a test issue?

### Step 1B: Identify Missing Test Cases

Review each source file and identify scenarios that are NOT tested:

**CompaniesHouseClientImpl Tests:**
- [ ] Successful API call returns RegisteredAddressResponse
- [ ] HTTP 404 throws CompanyNotFoundException with company number in message
- [ ] HTTP 429 throws RateLimitExceededException
- [ ] HTTP 500 throws CompaniesHouseApiException with status code
- [ ] Connection timeout throws appropriate exception
- [ ] Malformed JSON response throws exception
- [ ] Null company number throws IllegalArgumentException
- [ ] Empty company number throws IllegalArgumentException
- [ ] API key is included in Authorization header
- [ ] Correct endpoint URL is called
- [ ] HTTP method is GET
- [ ] Response deserialization includes all address fields

**CompaniesHouseProperties Tests:**
- [ ] Properties load from application.yml
- [ ] Default values are applied if not specified
- [ ] Validation fails if required properties missing
- [ ] Validation fails if timeout values invalid
- [ ] Getters/setters work correctly
- [ ] Can override with environment variables

**CompaniesHouseConfig Tests:**
- [ ] RestTemplate bean is created
- [ ] Timeout values are set correctly
- [ ] Bean can be injected into service
- [ ] HTTPS configuration is applied

**DTO Tests:**
- [ ] RegisteredAddressResponse deserializes from real API JSON
- [ ] All address fields are present and correct
- [ ] ApiErrorResponse deserializes error JSON
- [ ] Null/optional fields handled gracefully
- [ ] Round-trip serialization works

**Exception Tests:**
- [ ] Each exception can be instantiated
- [ ] Exception messages are informative
- [ ] Exception hierarchy works (can catch base exception)
- [ ] Company number included in relevant exceptions

### Step 1C: Add Missing Test Cases

For any scenarios identified as missing:

1. Write test in appropriate test class
2. Run: `mvn test -Dtest=ClassName`
3. Verify it fails (if testing new code)
4. If implementation missing, implement it
5. Run test again to verify it passes
6. Commit: `git add . && git commit -m "Add test for [scenario]"`

**Example Adding Missing Test:**
```java
@Test
@DisplayName("Should throw RateLimitExceededException when API returns 429")
void testApiRateLimitHandling() {
    // Arrange
    String companyNumber = "09370669";
    when(restTemplate.getForObject(any(String.class), eq(RegisteredAddressResponse.class)))
        .thenThrow(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS));

    // Act & Assert
    assertThrows(RateLimitExceededException.class, () -> {
        client.getRegisteredAddress(companyNumber);
    });
}
```

## Phase 2: Integration Test Verification

### Step 2A: Review Integration Tests

Check for integration tests that verify:
- Spring context starts correctly
- Configuration loads properly
- Beans are properly injected
- End-to-end flow works with WireMock

Look for: `CompaniesHouseClientIntegrationTest.java`

### Step 2B: Test Integration Scenarios

Integration tests should cover:

**Configuration Loading:**
- [ ] Spring context loads without errors
- [ ] CompaniesHouseProperties populated from application-test.yml
- [ ] RestTemplate bean is configured correctly
- [ ] CompaniesHouseClient bean is available

**Success Path:**
- [ ] End-to-end call to WireMock-stubbed API succeeds
- [ ] Response is properly deserialized
- [ ] All address fields are present

**Error Scenarios:**
- [ ] 404 response handled correctly
- [ ] 429 response handled correctly
- [ ] 500 response handled correctly
- [ ] Invalid JSON response handled
- [ ] WireMock timeout simulation works

**WireMock Setup Example:**
```java
@SpringBootTest
@ActiveProfiles("test")
class CompaniesHouseClientIntegrationTest {

    @Rule
    public static WireMockRule wireMockRule = new WireMockRule(8089);

    @Autowired
    private CompaniesHouseClient client;

    @BeforeClass
    public static void setupWireMock() {
        // Update application-test.yml to use http://localhost:8089
    }

    @Test
    @DisplayName("Integration: Successful company lookup returns address")
    void testSuccessfulLookup() {
        // Arrange
        String companyNumber = "09370669";
        String responseJson = /* sample from API docs */;

        stubFor(get(urlPathEqualTo("/company/" + companyNumber + "/registered-office-address"))
            .withHeader("Authorization", containing("test-api-key"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(responseJson)));

        // Act
        Optional<RegisteredAddressResponse> result = client.getRegisteredAddress(companyNumber);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getAddressLine1()).isNotBlank();
    }
}
```

## Phase 3: End-to-End Scenario Testing

Test complete workflows:

**Scenario 1: Happy Path**
- User provides valid company number
- API returns registered address
- Address is properly formatted
- No errors occur

**Scenario 2: Company Not Found**
- User provides non-existent company number
- API returns 404
- CompanyNotFoundException is thrown with company number in message
- Caller can catch and handle

**Scenario 3: Rate Limit Hit**
- User makes multiple rapid requests
- API returns 429
- RateLimitExceededException is thrown
- Caller knows to back off and retry

**Scenario 4: API Server Error**
- API returns 500
- CompaniesHouseApiException is thrown
- Error message includes status code
- Original error message is preserved

**Scenario 5: Network Timeout**
- API doesn't respond within configured timeout
- Timeout exception is thrown
- Caller can retry or fail gracefully

## Phase 4: Code Coverage Analysis

### Step 4A: Generate Coverage Report

```bash
# Generate JaCoCo coverage report
mvn clean test jacoco:report

# View the report
open target/site/jacoco/index.html
```

### Step 4B: Check Coverage by Component

Review coverage for each component:

| Component | Target | Actual | Notes |
|-----------|--------|--------|-------|
| CompaniesHouseClientImpl | 90%+ | ? | Core logic |
| CompaniesHouseConfig | 80%+ | ? | Spring configuration |
| CompaniesHouseProperties | 100% | ? | Simple DTOs |
| Exceptions | 100% | ? | Simple classes |
| DTOs | 100% | ? | Getters/setters |
| GlobalExceptionHandler | 85%+ | ? | HTTP handling |

### Step 4C: Identify Coverage Gaps

If any component is below target:
- Which lines are not covered?
- Are they important logic or edge cases?
- Should we add tests or is it acceptable?

### Step 4D: Coverage Improvement

For gaps below target:
1. Identify untested code
2. Write test case for it
3. Run: `mvn clean test jacoco:report`
4. Verify coverage improved
5. Commit: `git add . && git commit -m "Improve test coverage for [component]"`

## Phase 5: STD-003 Compliance Verification

Run through the STD-003 self-assessment checklist:

### Dependency Injection (6 items)
- [ ] All Spring beans use constructor injection (@RequiredArgsConstructor)
- [ ] No field injection (@Autowired on fields)
- [ ] No manual `new` of managed beans
- [ ] Proper use of @Service, @Repository, @Component
- [ ] Configuration externalized in application.yml
- [ ] No hardcoded configuration values

### Type Safety (5 items)
- [ ] All generics properly typed (no raw types)
- [ ] Optional<T> used for nullable values (not null returns)
- [ ] No unnecessary type casting
- [ ] Enums used instead of string constants (if applicable)
- [ ] Type-safe collections throughout

### Layered Architecture (4 items)
- [ ] Clear separation between layers
- [ ] DTOs used at boundaries
- [ ] Custom exceptions for domain-specific errors
- [ ] No business logic in controllers/configurations

### Error Handling (5 items)
- [ ] Custom exceptions extend appropriate base classes
- [ ] Global exception handler configured (if REST API)
- [ ] Error responses include status, message, path
- [ ] Specific exceptions caught (not generic Exception)
- [ ] Appropriate logging levels

### Testing (6 items)
- [ ] Unit tests for business logic with mocks
- [ ] Integration tests for Spring context
- [ ] Test fixtures or builders reduce duplication
- [ ] Both success and error cases tested
- [ ] Test names clearly describe what's tested
- [ ] Coverage 70%+ on core logic

### Documentation (4 items)
- [ ] Public methods have JavaDoc
- [ ] Complex algorithms documented
- [ ] No comments restating obvious code
- [ ] Configuration and setup documented

### Code Quality (5 items)
- [ ] SOLID principles followed
- [ ] Classes have single responsibility
- [ ] No duplication (DRY)
- [ ] Naming is clear and consistent
- [ ] Code is testable and loosely coupled

**Compliance Score**: Count checkmarks / total items = ___%

If score < 100%, identify issues:
- What's not compliant?
- How to fix it?
- Who should fix it (in code review)?

## Phase 6: Test Report Generation

### Step 6A: Gather Test Data

Collect information about testing:

```bash
# Test count and results
mvn test | grep -E "Tests run:|Failures:|Errors:"

# Coverage percentages
mvn jacoco:report  # Then examine target/site/jacoco/index.html

# Build status
mvn clean install | tail -20
```

### Step 6B: Review for Issues

Check for:
- [ ] All tests passing
- [ ] No build warnings
- [ ] Coverage meets targets
- [ ] No flaky tests (tests that sometimes fail)
- [ ] No ignored/skipped tests
- [ ] Clear test names and descriptions

### Step 6C: Document Findings

Note any issues, concerns, or recommendations:
- Performance issues (slow tests)?
- Missing test scenarios?
- Code quality issues?
- STD-003 compliance gaps?
- Documentation gaps?

## Phase 7: Create Test Report

Generate the comprehensive test report (see output specifications).

</instructions>

<output_specifications>

Create a comprehensive test report at:

```
/Users/andym/Dropbox/_home/Development/bh/CompanyHouse/company-house-api/docs/test-report.md
```

### Document Structure

```markdown
# Companies House API Integration - Test Report

## Executive Summary
- Total tests: [X]
- Tests passed: [X]
- Tests failed: [X]
- Code coverage: [X]%
- Status: PASS | NEEDS WORK

## Test Execution Summary

### Test Results
- Unit tests: [X] passed, [X] failed
- Integration tests: [X] passed, [X] failed
- Total coverage: [X]%

### Build Status
- `mvn clean test`: SUCCESS | FAILED
- No warnings: YES | NO (list any)

## Unit Test Results

### CompaniesHouseClientImpl
- Tests: [X]
- Coverage: [X]%
- Status: ✓ PASS
- Test scenarios:
  - Successful API call: ✓
  - 404 handling: ✓
  - 429 rate limit: ✓
  - 500 server error: ✓
  - Timeout handling: ✓
  - Malformed response: ✓
  - Invalid input: ✓

### CompaniesHouseProperties
- Tests: [X]
- Coverage: [X]%
- Status: ✓ PASS
- Test scenarios:
  - Configuration loading: ✓
  - Default values: ✓
  - Validation: ✓

### CompaniesHouseConfig
- Tests: [X]
- Coverage: [X]%
- Status: ✓ PASS
- Test scenarios:
  - Bean creation: ✓
  - Timeout configuration: ✓
  - Injection: ✓

### DTOs
- Tests: [X]
- Coverage: [X]%
- Status: ✓ PASS
- Test scenarios:
  - JSON deserialization: ✓
  - Field mapping: ✓
  - Null handling: ✓

### Exceptions
- Tests: [X]
- Coverage: [X]%
- Status: ✓ PASS
- Test scenarios:
  - Exception creation: ✓
  - Message formatting: ✓
  - Hierarchy: ✓

## Integration Test Results

### Scenario Testing

| Scenario | Status | Notes |
|----------|--------|-------|
| Spring context startup | ✓ | Config loads correctly |
| Successful company lookup | ✓ | WireMock simulation works |
| Company not found (404) | ✓ | Exception thrown correctly |
| Rate limit exceeded (429) | ✓ | Proper handling |
| Server error (500) | ✓ | Error response handling |
| Timeout | ✓ | Caught and converted |
| Malformed JSON | ✓ | Parsing error handled |

### WireMock Configuration
- Base URL: http://localhost:8089
- Stubs configured for:
  - Success responses (200)
  - Error responses (404, 429, 500)
  - Timeout simulation

## Code Coverage Report

### Overall Coverage
- **Total**: [X]%
- **Target**: 80%
- **Status**: PASS | NEEDS WORK

### Coverage by Component

| Component | Coverage | Target | Status |
|-----------|----------|--------|--------|
| CompaniesHouseClientImpl | [X]% | 90% | ✓ / ⚠ |
| CompaniesHouseConfig | [X]% | 80% | ✓ / ⚠ |
| CompaniesHouseProperties | [X]% | 100% | ✓ / ⚠ |
| DTOs | [X]% | 100% | ✓ / ⚠ |
| Exceptions | [X]% | 100% | ✓ / ⚠ |
| GlobalExceptionHandler | [X]% | 85% | ✓ / ⚠ / - |

### Coverage Trend
(If running tests multiple times)
- Previous: [X]%
- Current: [X]%
- Change: +/-[X]%

## STD-003 Compliance Verification

### Dependency Injection
- [✓] Constructor injection used
- [✓] No field injection
- [✓] Proper Spring annotations
- [✓] No hardcoded beans

**Status**: COMPLIANT

### Type Safety
- [✓] Proper generics throughout
- [✓] Optional used for nullable values
- [✓] No raw types
- [✓] Proper casting

**Status**: COMPLIANT

### Layered Architecture
- [✓] Clear layer separation
- [✓] DTOs at boundaries
- [✓] Custom exceptions
- [✓] No business logic in config

**Status**: COMPLIANT

### Error Handling
- [✓] Specific exception types
- [✓] Informative messages
- [✓] Original cause preserved
- [✓] Appropriate logging

**Status**: COMPLIANT

### Testing
- [✓] Unit tests with mocks
- [✓] Integration tests with Spring context
- [✓] Both success and error paths
- [✓] Clear test names

**Status**: COMPLIANT

### Documentation
- [✓] JavaDoc on public methods
- [✓] Clear error messages
- [✓] No redundant comments
- [✓] Configuration documented

**Status**: COMPLIANT

### Overall STD-003 Compliance: [X]/6 areas = [X]%

## Test Execution Statistics

### Execution Time
- Total test execution: [X] seconds
- Unit tests: [X] seconds
- Integration tests: [X] seconds
- Coverage report: [X] seconds

### Test Independence
- [✓] All tests can run standalone
- [✓] No test interdependencies
- [✓] Tests can run in any order
- [✓] No shared state between tests

## Testing Pyramid

```
      /\
     /  \  E2E Tests (1-2)
    /────\
   /      \  Integration Tests (5-10)
  /────────\
 /__________\ Unit Tests (30-50)
```

Current distribution:
- Unit tests: [X]
- Integration tests: [X]
- E2E tests: [X]

## Gaps and Issues Found

### Missing Test Coverage
- [ ] Area 1: [Description]
- [ ] Area 2: [Description]

### Code Quality Issues
- [ ] Issue 1: [Description]
- [ ] Issue 2: [Description]

### Documentation Gaps
- [ ] Gap 1: [Description]
- [ ] Gap 2: [Description]

### Performance Observations
- Slowest test: [Name] ([X]ms)
- Tests under 100ms: [X]%

## Recommendations

1. **Priority 1 (Must Fix)**
   - [Recommendation with rationale]

2. **Priority 2 (Should Fix)**
   - [Recommendation with rationale]

3. **Priority 3 (Nice to Have)**
   - [Recommendation with rationale]

## Conclusion

The Companies House API integration client library is **READY FOR INTEGRATION** with the following status:

- Code coverage: [X]% (Target: 80%+) ✓
- Tests passing: [X]% ✓
- STD-003 compliance: [X]% ✓
- Build: SUCCESS ✓
- No critical issues: YES ✓

### Quality Assessment

| Aspect | Status |
|--------|--------|
| Functionality | VERIFIED |
| Reliability | VERIFIED |
| Code Quality | VERIFIED |
| Documentation | ADEQUATE |
| Security | VERIFIED |
| Testability | VERIFIED |

**Overall Status**: ✓ APPROVED FOR PRODUCTION

The library is ready for integration into the larger project. All functionality has been tested, code quality standards have been met, and comprehensive documentation is in place.

## Appendix

### A. Test Execution Commands

```bash
# Run all tests
mvn clean test

# Run specific test class
mvn test -Dtest=CompaniesHouseClientImplTest

# Generate coverage report
mvn clean test jacoco:report

# View coverage report
open target/site/jacoco/index.html

# Build final JAR
mvn clean install
```

### B. Test Data

Real JSON samples used in tests (from Companies House API documentation):
- [Sample success response]
- [Sample 404 error response]
- [Sample 429 rate limit response]

### C. Known Issues / Deferred Items

None at this time.

### D. Related Documents

- Requirements: docs/requirements.md
- Architecture: docs/architecture.md
- Plan: docs/plan.md
- Code: src/main/java/com/example/companieshouse/
- Tests: src/test/java/com/example/companieshouse/
```

### Report Quality Checklist

Your test report should have:
- ✅ Executive summary with pass/fail status
- ✅ Detailed test results by component
- ✅ Code coverage metrics and analysis
- ✅ STD-003 compliance verification
- ✅ Integration test scenarios documented
- ✅ Issues and gaps identified
- ✅ Recommendations prioritized
- ✅ Clear approval status

</output_specifications>

<critical_reminders>

### 1. Test Independence
- Each test must run standalone
- No shared test fixtures or state
- Tests in any order must all pass
- Verify with: `mvn test -Dtest=ClassName#methodName`

### 2. Test Error Paths
- ❌ Only testing success scenarios
- ✅ Test success AND all error paths
- Test 404, 429, 500, timeout, malformed response

### 3. Realistic Integration Tests
- Use WireMock to simulate Companies House API
- Don't call real API in integration tests
- Test full Spring context startup
- Verify dependency injection works

### 4. Coverage Numbers Tell a Story
- 100% coverage ≠ good tests (could be testing trivial code)
- 70% coverage with good tests > 90% coverage with bad tests
- Focus on testing important logic, not getters/setters

### 5. STD-003 Compliance Must Be Verified
- Go through entire checklist
- Mark each item ✓ or ✗
- Document any non-compliance
- Plan fixes for any gaps

### 6. Test Names Matter
- ❌ `test1()`, `testMethod()`
- ✅ `testCompanyNotFoundThrowsException()`, `testRateLimitReturns429()`
- Use @DisplayName for long descriptions

### 7. Arrange-Act-Assert Pattern
```java
@Test
void testSomething() {
    // ARRANGE - Setup test data and mocks
    String input = "09370669";
    when(...).thenReturn(...);

    // ACT - Execute the code
    result = client.getRegisteredAddress(input);

    // ASSERT - Verify the results
    assertThat(result).isPresent();
}
```

### 8. Document Everything
- What tests were run?
- What % passed?
- What coverage was achieved?
- What issues were found?
- What recommendations do you have?

</critical_reminders>

<begin>

================================================================================
                    BEGIN COMPREHENSIVE TESTING
================================================================================

**FIRST ACTION**: Run the existing test suite to see current state:

```bash
cd /Users/andym/Dropbox/_home/Development/bh/CompanyHouse/company-house-api
mvn clean test
```

Review the output:
- How many tests passed?
- Are there failures?
- What's the coverage?

**SECOND**: Generate coverage report:

```bash
mvn clean test jacoco:report
open target/site/jacoco/index.html
```

Review coverage:
- Overall percentage?
- Which components need more testing?
- Any untested code paths?

**THIRD**: Work through the 7 phases systematically:

1. **Phase 1** - Verify existing unit tests, add missing tests
2. **Phase 2** - Verify integration tests with WireMock
3. **Phase 3** - Test end-to-end scenarios
4. **Phase 4** - Analyze code coverage and identify gaps
5. **Phase 5** - Check STD-003 compliance
6. **Phase 6** - Gather test data and findings
7. **Phase 7** - Generate comprehensive test report

**OUTPUT**: Create the complete test report at `docs/test-report.md` with:
- Executive summary and status
- Detailed test results by component
- Code coverage metrics
- STD-003 compliance verification
- Issues, gaps, and recommendations
- Clear approval/rejection status

**SUCCESS**: When you're done, the report should clearly communicate:
- Is the code production-ready? (YES | NO)
- What are the risks?
- What still needs to be done?
- What's the quality level?
- Can this be integrated into production?

================================================================================

</begin>

