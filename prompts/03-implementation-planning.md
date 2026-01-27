# 03 - Implementation Planning Prompt

> **Status**: Ready for use
> **Purpose**: Create detailed TDD task breakdown and file-level planning
> **Input**: `docs/requirements.md`, `docs/architecture.md`, STD-003 rubric
> **Output**: `docs/plan.md`
> **Complexity**: SIMPLE - Single session

---

<context>
<project>
Companies House API Integration - Client Library

You are creating a detailed implementation plan that the development team will follow to build the integration using Test-Driven Development. This plan must be clear enough that a developer can pick any task and immediately understand what to do.
</project>

<role>
Technical planner and project coordinator. You will create an implementation roadmap with clear tasks, dependencies, file-level details, and quality gates. You will ensure the plan follows TDD principles and STD-003 standards.
</role>

<objective>
Create a comprehensive implementation plan that:
- Breaks down work into 11 clearly defined tasks (T1-T11)
- Specifies the TDD workflow (Red-Green-Refactor) for each task
- Defines exactly which files to create and their structure
- Identifies task dependencies and critical path
- Establishes quality gates and definition of done
- Enables developers to work with confidence
</objective>

<input_documents>
Read these FIRST to understand what you're planning:
1. `docs/requirements.md` - What to build
2. `docs/architecture.md` - How to structure it
3. Reference `prompts/rubrics/STD-003-java-spring-boot-development-rubric.md` for standards
</input_documents>
</context>

<foundational_principles>
1. **TDD Discipline** - Every task: Write failing test → Implement → Refactor → Commit
2. **Task Sequencing** - Dependencies ordered correctly (DTOs before Client, Client before Service)
3. **Atomic Commits** - Each task produces a committable, working state
4. **Clear Scope** - Each task has a single focus, not too broad or narrow
5. **Incremental Progress** - Test suite grows with each task
6. **Definition of Done** - Clear criteria for task completion
7. **Quality Gates** - Coverage, standards compliance, documentation requirements
</foundational_principles>

<instructions>

## Step 1: Project Initialization Planning

Plan the project setup:

### Maven Configuration

**File**: `pom.xml`
- Parent: `spring-boot-starter-parent` (version 3.1.x or latest)
- Starters needed:
  - `spring-boot-starter-web` (RestTemplate)
  - `spring-boot-starter-test` (JUnit 5, Mockito, etc.)
- Dependencies:
  - Lombok (for @Data, @RequiredArgsConstructor)
  - WireMock (for integration testing)
  - Jackson (typically included in starter-web)
- Plugins:
  - maven-compiler-plugin (Java 17+)
  - maven-surefire-plugin (for running tests)
  - jacoco-maven-plugin (for coverage reports)

### Directory Structure

```
company-house-api/
├── src/
│   ├── main/
│   │   ├── java/com/example/companieshouse/
│   │   │   ├── client/
│   │   │   ├── dto/
│   │   │   └── config/
│   │   └── resources/
│   │       ├── application.yml
│   │       └── application-local.yml.example
│   │
│   └── test/
│       ├── java/com/example/companieshouse/
│       │   ├── client/
│       │   ├── dto/
│       │   ├── config/
│       │   └── integration/
│       │
│       └── resources/
│           └── application-test.yml
│
├── pom.xml
├── .gitignore
└── README.md (first draft)
```

### .gitignore Configuration

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
```

### application.yml Structure

```yaml
spring:
  application:
    name: companies-house-client

companies-house:
  api:
    base-url: https://api.company-information.service.gov.uk
    key: ${COMPANIES_HOUSE_API_KEY:REPLACE_IN_LOCAL_FILE}
    connect-timeout-ms: 5000
    read-timeout-ms: 10000
```

## Step 2: Task Breakdown (T1-T11)

Create 11 implementation tasks following TDD and dependencies:

### T1: Project Setup and Build Configuration
**File**: `pom.xml`, directory structure, `.gitignore`, `README.md` (skeleton)

**TDD Workflow**:
1. **RED**: Write a simple Java class with main method that verifies Maven builds
2. **GREEN**: Create pom.xml with all dependencies, build succeeds
3. **REFACTOR**: Organize pom.xml properties, versions
4. **COMMIT**: "T1: Project setup with Maven and dependencies"

**Definition of Done**:
- [ ] `mvn clean install` succeeds
- [ ] `mvn test` can run (no tests yet, but framework works)
- [ ] Directory structure created
- [ ] .gitignore configured
- [ ] README.md started (at least project description)
- [ ] No build warnings

**Estimated Complexity**: LOW

---

### T2: Configuration Properties Class
**Files**: `CompaniesHouseProperties.java`, `CompaniesHousePropertiesTest.java`

**TDD Workflow**:
1. **RED**: Write test that loads configuration from application-test.yml
2. **GREEN**: Create CompaniesHouseProperties with @ConfigurationProperties
3. **REFACTOR**: Add Lombok @Data, add validation annotations
4. **COMMIT**: "T2: Configuration properties with validation"

**Test Scenarios**:
- Properties load from application-test.yml
- Default values are applied
- Validation fails if required properties missing
- Getters/setters work correctly

**Definition of Done**:
- [ ] CompaniesHouseProperties loads from application-test.yml
- [ ] All fields have validation (@NotBlank, @Positive, etc.)
- [ ] Constructor injection ready
- [ ] Tests pass (100% coverage expected - simple class)
- [ ] JavaDoc on class level

**Estimated Complexity**: LOW

---

### T3: Spring Configuration Bean
**Files**: `CompaniesHouseConfig.java`, `CompaniesHouseConfigTest.java`

**TDD Workflow**:
1. **RED**: Write test that RestTemplate bean is created with proper timeouts
2. **GREEN**: Create @Configuration class with @Bean method
3. **REFACTOR**: Extract timeout values to constants, document choices
4. **COMMIT**: "T3: RestTemplate bean configuration"

**Test Scenarios**:
- RestTemplate bean is created
- Timeouts are set correctly
- HTTPS works
- Can inject bean into test

**Definition of Done**:
- [ ] RestTemplate bean available for injection
- [ ] Timeouts configured from properties
- [ ] Integration test can start Spring context and get bean
- [ ] Tests pass
- [ ] JavaDoc explains timeout reasoning

**Estimated Complexity**: LOW

---

### T4: Response DTOs
**Files**: `RegisteredAddressResponse.java`, `ApiErrorResponse.java`, `ErrorDetails.java` (and tests)

**TDD Workflow**:
1. **RED**: Write test deserializing Companies House API JSON into RegisteredAddressResponse
2. **GREEN**: Create DTO with fields, use Jackson defaults
3. **REFACTOR**: Add Lombok @Data, improve naming consistency
4. **COMMIT**: "T4: Response DTOs with JSON deserialization"

**Test Scenarios**:
- Deserialize sample Companies House response JSON
- All fields present and correct
- Handle null/optional fields gracefully
- ApiErrorResponse deserializes error responses
- Round-trip serialization works

**Definition of Done**:
- [ ] RegisteredAddressResponse deserializes valid JSON
- [ ] ApiErrorResponse deserializes error JSON
- [ ] ErrorDetails can be created and serialized
- [ ] Tests include real JSON examples from API docs
- [ ] Coverage: 100% (simple DTOs)
- [ ] No manual getters/setters (using @Data)

**Estimated Complexity**: LOW

---

### T5: Custom Exceptions
**Files**: All exception classes in `client.exception` package (and tests)

**TDD Workflow**:
1. **RED**: Write test that creates each exception type with proper context
2. **GREEN**: Create exception classes
3. **REFACTOR**: Extract common logic to base exception
4. **COMMIT**: "T5: Custom exceptions with context"

**Exception Types**:
- `CompaniesHouseApiException` (base, extends RuntimeException)
- `CompanyNotFoundException` (404)
- `RateLimitExceededException` (429)
- `InvalidCompanyNumberException` (validation)

**Test Scenarios**:
- Each exception can be created with message
- Base exception includes HTTP status when relevant
- Each exception can be caught/handled independently
- Exception messages are clear and actionable
- Stack traces show original cause when applicable

**Definition of Done**:
- [ ] All 4 exception types created
- [ ] Each includes company number in message (when applicable)
- [ ] Base exception captures HTTP status
- [ ] Tests pass (100% coverage - simple classes)
- [ ] JavaDoc explains when each should be thrown

**Estimated Complexity**: LOW

---

### T6: Client Interface
**Files**: `CompaniesHouseClient.java` (no test needed - interface)

**Purpose**: Define the public contract

**Content**:
```java
public interface CompaniesHouseClient {
    /**
     * Retrieve registered office address for a company.
     *
     * @param companyNumber the company number (required)
     * @return the registered address if found
     * @throws CompanyNotFoundException if company doesn't exist (404)
     * @throws RateLimitExceededException if rate limit exceeded (429)
     * @throws CompaniesHouseApiException for other API errors
     * @throws InvalidCompanyNumberException if company number is invalid
     */
    Optional<RegisteredAddressResponse> getRegisteredAddress(String companyNumber);
}
```

**Definition of Done**:
- [ ] Interface clearly documented with JavaDoc
- [ ] Exception types documented
- [ ] Return type uses Optional (not null)
- [ ] Follows STD-003 interface naming (no "I" prefix)

**Estimated Complexity**: TRIVIAL

---

### T7: Client Implementation - Success Path
**Files**: `CompaniesHouseClientImpl.java`, `CompaniesHouseClientImplTest.java` (Part 1)

**TDD Workflow**:
1. **RED**: Write test for successful API call returning address
2. **GREEN**: Implement basic HTTP call, map response to DTO
3. **REFACTOR**: Extract URL building, clean up variable names
4. **COMMIT**: "T7: Client implementation - success path"

**Test Scenarios**:
- Makes correct HTTP GET call to /company/{companyNumber}/registered-office-address
- Includes API key in Authorization header (or query param, per API docs)
- Deserializes response to RegisteredAddressResponse
- Returns Optional.of(response)
- Uses RestTemplate (mocked in unit test)

**Test Setup** (important for later):
- Mock RestTemplate with MockRestServiceServer or direct mocks
- Use sample JSON response from API documentation
- Verify HTTP request details (URL, headers, method)

**Definition of Done**:
- [ ] Test mocks RestTemplate
- [ ] HTTP GET call made to correct URL
- [ ] API key passed correctly
- [ ] Response deserialized correctly
- [ ] Tests pass
- [ ] Code follows STD-003 (constructor injection, proper generics)
- [ ] Coverage: 80%+

**Estimated Complexity**: MEDIUM

---

### T8: Client Implementation - Error Handling
**Files**: `CompaniesHouseClientImpl.java`, `CompaniesHouseClientImplTest.java` (Part 2)

**TDD Workflow**:
1. **RED**: Write tests for 404, 429, 500, timeout scenarios
2. **GREEN**: Add exception handling and mapping logic
3. **REFACTOR**: Extract error handling to separate method if needed
4. **COMMIT**: "T8: Client implementation - error handling"

**Test Scenarios**:
- HTTP 404 → throws CompanyNotFoundException
- HTTP 429 → throws RateLimitExceededException
- HTTP 500 → throws CompaniesHouseApiException
- Connection timeout → throws appropriate exception
- Malformed JSON response → throws JsonMappingException or CompaniesHouseApiException
- Empty/null company number → throws IllegalArgumentException or InvalidCompanyNumberException
- Unknown HTTP status → throws CompaniesHouseApiException with status in message

**Test Implementation Tips**:
- Test each error scenario independently
- Mock RestTemplate to return different responses/exceptions
- Verify exception message contains relevant context
- Use @DisplayName on tests to describe scenarios

**Definition of Done**:
- [ ] All error scenarios tested
- [ ] Correct exception type thrown for each scenario
- [ ] Exception messages include context (status code, company number)
- [ ] Exception messages are useful for debugging
- [ ] Tests pass
- [ ] Coverage: 90%+ on client implementation
- [ ] Input validation for company number

**Estimated Complexity**: MEDIUM

---

### T9: Global Exception Handler (Optional for Library)
**Files**: `GlobalExceptionHandler.java`, `GlobalExceptionHandlerTest.java`

**Note**: This is optional if the library is just a client. Include if planning REST API wrapper.

**TDD Workflow**:
1. **RED**: Write test that exception is converted to HTTP response
2. **GREEN**: Create @RestControllerAdvice with @ExceptionHandler methods
3. **REFACTOR**: Extract response building logic
4. **COMMIT**: "T9: Global exception handler for API responses"

**Handler Methods**:
- CompanyNotFoundException → 404 with error details
- RateLimitExceededException → 429 with error details
- Generic Exception → 500 with generic message

**Definition of Done**:
- [ ] All custom exceptions handled
- [ ] HTTP status codes correct
- [ ] Error response includes timestamp, message, path
- [ ] Sensitive data not in error messages
- [ ] Tests pass (mock Spring context)

**Estimated Complexity**: LOW (deferred - can skip for pure library)

---

### T10: Integration Tests with WireMock
**Files**: `CompaniesHouseClientIntegrationTest.java`, WireMock stubs

**Purpose**: Test the full stack with realistic API simulation

**TDD Workflow**:
1. **RED**: Write test using @SpringBootTest, WireMock, real Spring context
2. **GREEN**: Configure WireMock stubs, run tests
3. **REFACTOR**: Extract stub setup to helper methods
4. **COMMIT**: "T10: Integration tests with WireMock simulation"

**Test Scenarios**:
- Spring context starts
- Configuration loads from application-test.yml
- Dependency injection works (can inject CompaniesHouseClient bean)
- End-to-end call to stubbed API works
- Error responses handled correctly
- Response deserialization works

**WireMock Setup**:
```java
@SpringBootTest
@ActiveProfiles("test")
class CompaniesHouseClientIntegrationTest {
    @ClassRule
    static WireMockRule wireMockRule = new WireMockRule(8089);

    @Autowired
    private CompaniesHouseClient client;

    @Test
    void testSuccessfulLookup() {
        stubFor(get(urlPathEqualTo("/company/09370669/registered-office-address"))
            .willReturn(aResponse()
                .withStatus(200)
                .withBody(/* example JSON */)));

        Optional<RegisteredAddressResponse> result = client.getRegisteredAddress("09370669");
        assertThat(result).isPresent();
    }
}
```

**Definition of Done**:
- [ ] Spring context test (@SpringBootTest) passes
- [ ] WireMock stubbing works
- [ ] Full end-to-end scenario tested
- [ ] Configuration loaded correctly
- [ ] Both success and error scenarios tested
- [ ] Tests pass

**Estimated Complexity**: MEDIUM

---

### T11: Documentation and README
**Files**: `README.md`, JavaDoc updates, example code

**Purpose**: Ensure developers can use and maintain the library

**Contents**:
- How to build: `mvn clean install`
- How to run tests: `mvn test`, `mvn clean test jacoco:report`
- Setup: Create application-local.yml
- Configuration: Explain application.yml properties
- Usage examples: How to inject and use CompaniesHouseClient
- API docs: Generated JavaDoc
- Architecture overview: Link to architecture.md
- Contributing: Link to standards (STD-003)

**Definition of Done**:
- [ ] README.md complete with sections above
- [ ] Setup instructions clear enough for new developer
- [ ] Code examples compile and run
- [ ] All public classes have JavaDoc
- [ ] All public methods have JavaDoc with @param, @return, @throws
- [ ] `mvn javadoc:javadoc` generates without errors
- [ ] README links to architecture and requirements docs

**Estimated Complexity**: LOW

---

## Step 3: File-Level Planning

For each task, specify which files are created/modified:

| Task | Source Files | Test Files | Config Files | Commits |
|------|-------------|-----------|-------------|---------|
| T1 | (existing) | - | pom.xml, .gitignore | 1 |
| T2 | CompaniesHouseProperties | CompaniesHousePropertiesTest | application-test.yml | 2 (test first) |
| T3 | CompaniesHouseConfig | CompaniesHouseConfigTest | - | 2 (test first) |
| T4 | RegisteredAddressResponse, ApiErrorResponse, ErrorDetails | *ResponseTest.java | - | 2 (test first) |
| T5 | 4 Exception classes | ExceptionTests | - | 2 (test first) |
| T6 | CompaniesHouseClient | - | - | 1 (interface) |
| T7 | CompaniesHouseClientImpl (Part 1) | CompaniesHouseClientImplTest (Part 1) | - | 2 (test first) |
| T8 | CompaniesHouseClientImpl (Part 2) | CompaniesHouseClientImplTest (Part 2) | - | 2 (test first) |
| T9 | GlobalExceptionHandler | GlobalExceptionHandlerTest | - | 2 (test first) |
| T10 | - | CompaniesHouseClientIntegrationTest | application-test.yml | 1 |
| T11 | - | - | README.md, JavaDoc | 1 |

## Step 4: Dependency Graph

Tasks must be completed in this order:

```
T1 (Setup)
    ↓
T2 (Properties) ─→ T3 (Config)
    ↓
T4 (DTOs) → T5 (Exceptions) → T6 (Interface)
    ↓
    ├─→ T7 (Client Success) → T8 (Client Errors) ──┐
    │                                               ├─→ T10 (Integration)
    ├─→ T9 (Exception Handler - optional) ─────────┘
    ↓
T11 (Documentation)
```

**Critical Path**: T1 → T2 → T3 → T4 → T5 → T6 → T7 → T8 → T10 → T11

Parallel Opportunities (with proper sequencing):
- T2 and T3 can be done in parallel after T1 (both depend on nothing else)
- T4 and T5 can be done in parallel (no dependencies)
- T7 and T8 must be sequential (Part 1 before Part 2 of same file)
- T9 is optional and can be parallel with T7/T8

## Step 5: Quality Gates and Definition of Done

### Per-Task Quality Gate

Every task must satisfy:
- ✅ Tests written first (RED phase)
- ✅ All tests pass (GREEN phase)
- ✅ Code reviewed for STD-003 compliance
- ✅ Code coverage meets target (see below)
- ✅ JavaDoc on public methods
- ✅ No hardcoded configuration or secrets
- ✅ Commit message is clear

### Coverage Targets

| Component | Target | Notes |
|-----------|--------|-------|
| CompaniesHouseClientImpl | 90%+ | Core logic |
| CompaniesHouseConfig | 80%+ | Spring configuration |
| DTOs | 100% | Simple classes |
| Exceptions | 100% | Simple classes |
| Properties | 100% | Configuration |
| GlobalExceptionHandler | 85%+ | HTTP handling |

### Overall Goals

- **Unit Test Coverage**: 80%+ of core logic
- **Integration Test Coverage**: All error scenarios
- **All Tests Pass**: `mvn clean test` succeeds
- **No Warnings**: Zero build warnings
- **STD-003 Compliance**: Self-assessment checklist satisfied

### Pre-Commit Checklist

Before committing each task:
- [ ] `mvn clean test` passes
- [ ] No build warnings
- [ ] Coverage meets target
- [ ] Code follows STD-003
- [ ] JavaDoc complete
- [ ] No secrets or hardcoded config
- [ ] Commit message describes change

</instructions>

<output_specifications>

Create a comprehensive implementation plan document at:

```
/Users/andym/Dropbox/_home/Development/bh/CompanyHouse/company-house-api/docs/plan.md
```

### Document Structure

```markdown
# Companies House API Integration - Implementation Plan

## Overview
Summary of the 11-task plan, estimated effort, timeline considerations

## Project Setup

### Maven Configuration
[Detailed pom.xml spec]

### Directory Structure
[Complete file tree]

### Configuration Files
[application.yml, .gitignore details]

## Task Breakdown (T1-T11)

### T1: Project Setup
- Objective
- Files to create
- TDD Workflow
- Test scenarios
- Definition of done
- Complexity

### T2 through T11
[Each task with same structure]

## Task Dependencies

### Dependency Graph
[ASCII or Mermaid diagram]

### Critical Path
[Sequence of critical tasks]

### Parallel Opportunities
[What can be done simultaneously]

## File-Level Planning

### Files to Create
[Complete table of all files and when]

### File Purposes
[What each file does]

### Configuration Files
[application.yml structure, profiles]

## Quality Gates

### Coverage Targets
[By component]

### Per-Task Criteria
[What must be satisfied]

### Pre-Commit Checklist
[Items to verify before commit]

## TDD Workflow Reference

### Red-Green-Refactor Cycle
[Explanation with example]

### Commit Strategy
[How to commit at each phase]

### Test Independence
[How tests should be structured]

## Tools and Commands

### Building
```bash
mvn clean install
```

### Testing
```bash
mvn clean test
mvn clean test jacoco:report
```

### Viewing Coverage
```bash
open target/site/jacoco/index.html
```

## Next Steps
Implementation prompt will execute these tasks sequentially using context compaction survival patterns.
```

### Deliverable Quality

Your plan document should be:
- **Complete** - All 11 tasks with clear scope
- **Actionable** - Developer can pick any task and immediately start
- **Ordered** - Dependencies clear, critical path obvious
- **Measurable** - Coverage targets, quality gates defined
- **Realistic** - Complexity levels set appropriately

</output_specifications>

<critical_reminders>

### 1. TDD Order Matters
- ❌ Can't test client before DTOs exist
- ❌ Can't test success path before error path (actually can, but sequencing matters)
- ✅ DTOs → Exceptions → Client Interface → Client Implementation
- ✅ Properties → Config Bean → everything else

### 2. Dependencies Must Be Clear
- Each task must clearly state what it depends on
- Task graph should show critical path
- Developers should know which tasks they can start

### 3. Test First Discipline
- RED phase: Write failing test
- GREEN phase: Implement minimum code
- REFACTOR phase: Clean up
- COMMIT: Save working state
- This must be explicit in task descriptions

### 4. Commits Are Important
- Each task should produce a committable, working state
- All tests pass at each commit
- No "broken" commits
- Enables debugging and rollback if needed

### 5. Quality Gates Must Be Realistic
- 90%+ coverage for client (core logic)
- 80%+ coverage for config (most code)
- 100% coverage for simple DTOs/exceptions (no branches)
- 85%+ for exception handlers

### 6. Configuration Must Be Planned
- Where does application.yml go?
- Where does application-test.yml go?
- What about application-local.yml?
- When is each created (in which task)?

### 7. Integration Tests Must Be Planned
- T10 specifically for integration tests
- WireMock setup explicitly described
- Full Spring context testing
- Before documentation (T11)

</critical_reminders>

<begin>

================================================================================
                    BEGIN IMPLEMENTATION PLANNING
================================================================================

**FIRST ACTION**: Read the requirements (docs/requirements.md) and architecture (docs/architecture.md) documents to understand context.

**SECOND ACTION**: Work through all 5 planning steps:

1. **Step 1**: Plan project initialization (Maven, directories, .gitignore)
2. **Step 2**: Break down into 11 tasks (T1-T11) with full TDD descriptions
3. **Step 3**: Create file-level planning table
4. **Step 4**: Create dependency graph
5. **Step 5**: Define quality gates and definition of done

**OUTPUT**: Create complete plan document at `docs/plan.md` that includes:
- All 11 tasks fully described
- TDD workflow for each
- File specifications
- Dependency graph with critical path
- Quality gates and coverage targets
- Pre-commit checklist

**SUCCESS**: When you're done, a developer should be able to:
- Pick T1 and immediately know what to do
- Understand dependencies before starting a task
- Know what "done" looks like for each task
- Know what tests to write before implementing
- Know what coverage targets to hit
- Know the correct build/test commands

================================================================================

</begin>

