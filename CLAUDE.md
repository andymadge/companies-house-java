# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This repository contains a **5-prompt suite** for building a Companies House API integration in Java Spring Boot using Test-Driven Development. The prompts guide the complete development lifecycle from requirements gathering through implementation to testing.

**Current State**: Prompts 01-03 complete. Requirements, architecture, and implementation plan documented. Ready for Prompt 04 (TDD Implementation).

**Purpose**: Build a reusable client library to retrieve company registered addresses from the UK Companies House Public Data API.

**Key Technologies**:
- Java 17+ (Spring Boot 3.2+)
- Maven
- RestClient (Spring 6.1+) for HTTP calls
- JUnit 5 + Mockito for testing
- WireMock for API simulation
- Lombok for reducing boilerplate

## Repository Structure

### Core Directories

```
prompts/                          # 5 executable prompts for full development lifecycle
├── 01-requirements-gathering.md  # Requirements discovery from API analysis
├── 02-architecture-design.md     # Architecture design with ADRs
├── 03-implementation-planning.md # TDD task breakdown (T1-T11)
├── 04-tdd-implementation.md      # Implementation execution (multi-session)
├── 05-comprehensive-testing.md   # Testing and quality verification
├── README.md                      # How to use the prompt suite
└── rubrics/
    ├── STD-001-prompt-creation-rubric.md    # Prompt engineering standards
    └── STD-003-java-spring-boot-development-rubric.md  # Java/Spring standards

docs/                             # Generated documentation (created by prompts)
├── requirements.md              # ✅ Created by Prompt 01
├── architecture.md              # ✅ Created by Prompt 02
├── plan.md                       # ✅ Created by Prompt 03
└── test-report.md               # (created by Prompt 05)

src/                              # Generated code (created by Prompt 04)
├── main/java/com/example/companieshouse/
│   ├── client/          # API client interface and implementation
│   ├── dto/             # Data transfer objects
│   └── config/          # Spring configuration
└── test/java/com/example/companieshouse/  # Unit and integration tests

.work/implementation/             # Internal progress tracking (Prompt 04)
├── progress.yaml                # Current task and resumption point
├── task-status.yaml             # Status of all 11 tasks
└── files-created.yaml           # Catalog of created files
```

### Key Configuration Files

- **pom.xml** - Maven configuration (created by Prompt 04, Task 1)
- **application.yml** - Spring Boot configuration template
- **application-local.yml.example** - Local development example (never commit real keys)
- **.gitignore** - Excludes application-local.yml and build artifacts (NOTE: `.work/` MUST be committed for progress tracking)

## Execution Workflow

The prompts are designed to be run **sequentially**. Each produces output that feeds into the next:

```
Prompt 01 (30-45 min)  → docs/requirements.md
         ↓
Prompt 02 (45-60 min)  → docs/architecture.md
         ↓
Prompt 03 (45-60 min)  → docs/plan.md
         ↓
Prompt 04 (3-5 hours)  → src/**/*.java + .work/implementation/ tracking
         ↓
Prompt 05 (1-2 hours)  → docs/test-report.md + coverage reports
```

### Running Prompts

1. Copy the entire prompt file content into Claude Code
2. Provide any required context from previous steps
3. Let the prompt guide you through its workflow
4. The prompt will produce clear outputs in specified locations

### Special Notes on Prompt 04 (Implementation)

This prompt spans multiple sessions and uses **context compaction survival patterns** from STD-001:

- **First Session**: Check for `.work/implementation/progress.yaml`, initialize if needed, begin tasks T1-T11
- **Subsequent Sessions**: Start Prompt 04 again, it detects progress file and resumes from `next_action`
- **Progress Files**: Updated using canonical checkpoint triggers (after completions, every 5-10 min, before risky ops)
- **TDD Cycle**: RED (failing test) → GREEN (pass test) → REFACTOR → COMMIT → UPDATE progress
- **Version Control**: `.work/` directory MUST be committed to git (enables team collaboration and machine switching)

Always check `.work/implementation/progress.yaml` before starting Prompt 04 implementation work.

## Architecture Decisions

These design choices are **baked into all prompts** and don't require modification:

| Decision | Value | Rationale |
|----------|-------|-----------|
| HTTP Client | RestClient (Spring 6.1+) | Modern API, RestTemplate in maintenance mode (ADR-001) |
| API Endpoint | `/company/{companyNumber}` | Returns complete address fields including care_of, po_box |
| Package structure | `com.example.companieshouse` | Generic, easy to rename when integrating |
| Component type | Client library (no REST controllers) | Reusable across projects, maximum portability |
| Build tool | Maven | Standard for Spring Boot, good ecosystem |
| Config format | application.yml | Modern, hierarchical, preferred by community |
| API key setup | Local override file | `application.yml` has placeholder, `application-local.yml` (gitignored) has real key |

## Development Standards

All code must follow **STD-003 Java Spring Boot Development Rubric**. Key principles:

### Dependency Injection
- Use **constructor injection** only (no @Autowired on fields)
- Use `@RequiredArgsConstructor` from Lombok to reduce boilerplate
- Declare dependencies as `private final`

### Type Safety
- Use proper generics: `List<T>`, `Optional<T>`, never raw types
- Use `Optional<T>` for nullable returns, never `null`
- Avoid type casting; design types correctly upfront

### Exceptions
- Create specific exception types (CompanyNotFoundException, RateLimitExceededException)
- Extend RuntimeException, not checked exceptions
- Include context in exception messages (company number, HTTP status)
- Use custom exceptions to enable precise error handling

### Testing
- **Test-Driven Development**: Always write tests first (RED), then code (GREEN)
- **Unit tests**: Mock external dependencies (RestClient, HTTP calls)
- **Integration tests**: Use WireMock to simulate Companies House API
- **Coverage targets**: 80%+ overall, 90%+ on client implementation, 100% on DTOs/exceptions
- **Test naming**: Use @DisplayName with clear descriptions of what's tested

### No Hardcoded Configuration
- All configuration in `application.yml` or environment variables
- API keys never in source code
- Database URLs, timeouts, endpoints all externalized

### Code Organization
- Layered architecture: Client → DTOs → Config → Exceptions
- Feature-based packages (not layer-based at root)
- One public interface (CompaniesHouseClient) for the library
- Clear separation of concerns

## Common Commands

### Build and Test

```bash
# Build project
mvn clean install

# Run all tests
mvn clean test

# Run specific test class
mvn test -Dtest=CompaniesHouseClientImplTest

# Run specific test method
mvn test -Dtest=CompaniesHouseClientImplTest#testCompanyNotFound

# Generate code coverage report (JaCoCo)
mvn clean test jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

### Git Workflow

```bash
# Check current implementation status
cat .work/implementation/progress.yaml

# See what tasks are done
cat .work/implementation/task-status.yaml

# View implementation commits during Prompt 04
git log --oneline | grep "T[0-9]:"
```

### During Prompt 04 (Implementation)

The prompt handles most commands, but you may need:

```bash
# If stuck: see exactly what to do next
cat .work/implementation/progress.yaml  # Read next_action field

# Manual test runs (if prompt doesn't auto-run)
mvn test

# Manual commit (if needed between prompts)
git add .
git commit -m "Progress: [description]"
```

## Key Files to Understand

### Before Starting Any Implementation

1. **prompts/README.md** - How to use the prompt suite (start here)
2. **prompts/rubrics/STD-001-prompt-creation-rubric.md** - Prompt creation patterns
3. **prompts/rubrics/STD-003-java-spring-boot-development-rubric.md** - Code standards reference

### After Each Prompt Completes

- **After Prompt 01**: Read `docs/requirements.md` to understand what's being built
- **After Prompt 02**: Read `docs/architecture.md` to understand how it's structured (especially ADRs)
- **After Prompt 03**: Read `docs/plan.md` to understand the 11-task breakdown before implementation
- **After Prompt 04**: Check `src/main/java/` for the implementation
- **After Prompt 05**: Read `docs/test-report.md` to understand test coverage and quality

### During Prompt 04 Implementation

- **.work/implementation/progress.yaml** - Current task, what's done, what's next
- **.work/implementation/task-status.yaml** - Status of all 11 tasks
- **docs/plan.md** - Detailed description of each task (reference for context)
- **docs/architecture.md** - Component responsibilities and package structure

## Companies House API Reference

The prompts guide analysis of these API endpoints:

- **Overview**: https://developer.company-information.service.gov.uk/overview
- **Company Profile**: https://developer-specs.company-information.service.gov.uk/companies-house-public-data-api/reference/company-profile/company-profile
- **Registered Office Address**: https://developer-specs.company-information.service.gov.uk/companies-house-public-data-api/reference/registered-office-address/registered-office-address

The prompts automatically fetch and analyze these during execution.

## What Each Prompt Does

### Prompt 01: Requirements Gathering
- Fetches Companies House API documentation using WebFetch
- Compares endpoint options
- Documents functional and non-functional requirements
- Defines DTOs and exception types
- Produces: `docs/requirements.md`

### Prompt 02: Architecture Design
- Reads requirements.md
- References STD-003 rubric
- Designs components and their interactions
- Creates 7 Architecture Decision Records (ADRs)
- Specifies complete package structure
- Produces: `docs/architecture.md`

### Prompt 03: Implementation Planning
- Breaks work into 11 tasks (T1: Setup, T2-T10: Implementation, T11: Docs)
- Defines TDD workflow for each task
- Creates dependency graph showing task order
- Specifies files to create in each task
- Sets quality gates and coverage targets
- Produces: `docs/plan.md`

### Prompt 04: TDD Implementation ⭐
- Initializes project structure
- Executes T1-T11 following Red-Green-Refactor cycle
- Writes tests first, then implementation
- Commits working code after each task
- Tracks progress in `.work/implementation/` for resumption
- Updates progress.yaml constantly for context compaction survival
- Produces: `src/main/java/`, `src/test/java/`, `.work/implementation/*.yaml`

### Prompt 05: Comprehensive Testing
- Runs full test suite
- Identifies missing test scenarios and adds them
- Generates JaCoCo coverage report
- Tests error paths (404, 429, 500, timeout, etc.)
- Verifies STD-003 compliance
- Produces: `docs/test-report.md`, coverage reports

## If You Need to Modify a Prompt

The prompts are designed to be comprehensive but may need adjustments if:

1. **Different package structure**: Update all references from `com.example.companieshouse`
2. **Different build tool**: Modify pom.xml sections and Maven commands
3. **Different API endpoint**: Prompts guide analysis, but focus area changes
4. **Different Spring Boot version**: Prompts work with Spring Boot 3.0+

Modify prompts **before running them**. Prompts read each other's outputs, so changes cascade.

## Common Issues During Execution

### Context Gets Full During Prompt 04

**Expected behavior**: You'll be reminded to save progress.

**Action**:
1. Let the prompt guide you to commit: `git add . && git commit -m "Progress: [task description]"`
2. Prompt updates `.work/implementation/progress.yaml` with `next_action`
3. Start new session, run Prompt 04 again
4. It automatically detects progress file and resumes from `next_action`

### Test Fails During Implementation

**Expected**: RED phase intentionally fails, then you implement code to pass it.

**If unexpected failure**: Check test error message, it tells you what's wrong. Fix implementation, re-run test.

### Coverage Below Target After Prompt 05

1. Run: `mvn jacoco:report`
2. Open: `target/site/jacoco/index.html`
3. Identify untested lines
4. Write tests for those scenarios
5. Confirm tests pass and coverage improves

## Next Steps

1. **Run Prompt 04**: Copy `prompts/04-tdd-implementation.md` to Claude Code
2. **Review docs/plan.md** for the 11-task TDD breakdown before starting
3. **Reference STD-003** throughout for code quality standards
4. **Track progress** in `.work/implementation/` during Prompt 04
5. **Run full test suite** before finishing: `mvn clean test jacoco:report`

## Notes for Future Instances

- This is a **prompt-driven development** project, not a traditional codebase
- All work is guided by the 5 prompts in `prompts/` directory
- Each prompt is comprehensive and explains exactly what to do
- Context compaction survival is built into Prompt 04 via `.work/implementation/` tracking
- Standard development tools (Maven, JUnit, Mockito) but workflow is prompt-guided
- Quality standards (STD-001, STD-003) are referenced throughout prompts

---

**Last Updated**: 2026-01-27
**Project Phase**: Planning complete, ready for implementation (Prompt 04)
**Start Point**: `prompts/04-tdd-implementation.md`
**Key Reference**: `docs/plan.md` (11-task TDD breakdown)
