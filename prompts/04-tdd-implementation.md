# 04 - TDD Implementation Prompt

> **Status**: Ready for use
> **Purpose**: Execute test-first development of the integration
> **Input**: `docs/requirements.md`, `docs/architecture.md`, `docs/plan.md`, STD-003 rubric
> **Output**: `src/` code, `.work/implementation/` tracking files
> **Complexity**: COMPLEX - Multi-session, context compaction survival required

---

<context>
<project>
Companies House API Integration - Client Library

You are executing the implementation of a Java Spring Boot client library for retrieving company registered addresses from the Companies House API. This work will span multiple sessions and must survive context compaction.
</project>

<role>
Java Spring Boot developer implementing the integration using Test-Driven Development. You will write failing tests first, implement minimum code to pass, refactor for quality, commit working changes, and track progress to enable resumption after context compaction.
</role>

<objective>
Implement the complete Companies House API integration by:
- Following Test-Driven Development (Red-Green-Refactor cycle)
- Executing all 11 tasks (T1-T11) from the plan.md
- Creating production-ready, thoroughly tested code
- Following Spring Boot best practices (STD-003)
- Tracking progress to survive context compaction
- Producing code that can be integrated into a larger project
</objective>

<input_documents>
Reference these documents during implementation:
1. `docs/requirements.md` - What to build (functional/non-functional requirements)
2. `docs/architecture.md` - How to structure it (components, ADRs, package design)
3. `docs/plan.md` - Task breakdown and execution order (T1-T11)
4. `prompts/rubrics/STD-003-java-spring-boot-development-rubric.md` - Code quality standards

Your implementation must satisfy all requirements and follow all standards.
</input_documents>
</context>

<foundational_principles>
1. **Test First** - Write failing test BEFORE implementation code (RED phase)
2. **Red-Green-Refactor** - Fail → Pass → Clean → Commit cycle for everything
3. **SOLID Principles** - Single responsibility, dependency inversion, no duplication
4. **Constructor Injection** - No field injection, use @RequiredArgsConstructor
5. **Type Safety** - Proper generics, no raw types, Optional<T> for nullable
6. **Defensive at Boundaries** - Validate all external API inputs and responses
7. **Clear Error Messages** - Exception messages include context (company number, status code)
8. **No Hardcoded Values** - All configuration in application.yml or environment
9. **STD-003 Compliance** - Every file must pass STD-003 self-assessment checklist
</foundational_principles>

<context_compaction_survival>
  <critical_warning>
  THIS IMPLEMENTATION SPANS MULTIPLE FILES AND MULTIPLE SESSIONS.
  Implementation involves creating ~12-15 source files and ~10-15 test files.
  You WILL lose context during this work.
  You MUST track progress in `.work/implementation/` to resume correctly after compaction.
  NEVER restart completed work. ALWAYS resume from next_action in progress.yaml.
  </critical_warning>

  <work_tracking_directory>
    <path>.work/implementation/</path>
    <purpose>Persistent work state that survives context compaction</purpose>
    <critical>CREATE THIS DIRECTORY FIRST before any implementation work</critical>

    <required_files>
      <file name="progress.yaml">
        <purpose>Track current task and exactly what to do next</purpose>
        <updated>After EVERY file created, EVERY task completed</updated>
        <critical>MUST be updated frequently - this is your resumption lifeline</critical>
        <format>YAML with current_task, next_action, completed tasks, in-progress tasks</format>
      </file>

      <file name="task-status.yaml">
        <purpose>Status snapshot of all 11 tasks and their completion</purpose>
        <created>Phase 0 during initialization</created>
        <updated>After each task completes</updated>
        <format>Task ID, status (not_started/in_progress/complete), files created, tests passing</format>
      </file>

      <file name="files-created.yaml">
        <purpose>Catalog of all source and test files created (for reference during resumption)</purpose>
        <created>Phase 0</created>
        <updated>Every time a new file is created</updated>
        <format>List of files with creation date, task, and purpose</format>
      </file>
    </required_files>
  </work_tracking_directory>

  <progress_tracking_schema>
```yaml
# .work/implementation/progress.yaml - UPDATE FREQUENTLY
progress:
  last_updated: "2026-01-27T14:30:00Z"
  current_task: "T3"  # Which task are we on?
  current_phase: "implementation"  # Phase 0 (init), Phase 1 (tasks), Phase 2 (integration), Phase 3 (verification)
  status: "In Progress"  # Not Started | In Progress | Blocked | Complete

  # Status of each task
  tasks:
    T1_project_setup:
      status: "Complete"
      completed_at: "2026-01-27T12:00:00Z"
      files_created: ["pom.xml", ".gitignore", "README.md (skeleton)"]
      tests_passing: true
      notes: "Maven builds successfully"

    T2_configuration_properties:
      status: "In Progress"
      started_at: "2026-01-27T12:30:00Z"
      current_step: "refactor"  # red | green | refactor | commit
      files_created: ["CompaniesHouseProperties.java"]
      files_in_progress: ["CompaniesHousePropertiesTest.java"]
      tests_passing: false
      notes: "Test written, need to implement class"

    T3_spring_config:
      status: "Not Started"

    # ... T4-T11

  # Work completed this session
  work_completed:
    - task: "T1"
      completed_at: "2026-01-27T12:00:00Z"
      commit: "T1: Project setup with Maven dependencies"

  # What's currently being worked on
  work_in_progress:
    - task: "T2"
      status: "Wrote test, implementing CompaniesHouseProperties class"

  # What's remaining
  work_remaining:
    - "T3: Spring configuration"
    - "T4: Response DTOs"
    # ... rest

  # Any blockers
  blockers: []

  # CRITICAL: Exactly what to do next
  next_action: "Finish T2: CompaniesHouseProperties test is failing. Implement CompaniesHouseProperties.java with @ConfigurationProperties(prefix='companies-house'), @Data, @Validated, and validation annotations. Then run mvn test to verify."
```
  </progress_tracking_schema>

  <resumption_protocol>
  WHEN CONTEXT IS COMPACTED OR SESSION RESUMES:

  1. IMMEDIATELY check for existing progress:
     ```bash
     cat .work/implementation/progress.yaml 2>/dev/null || echo "NO_PROGRESS_FILE"
     ```

  2. IF progress file exists:
     - Read: current_task, current_phase, next_action
     - Check: which tasks are complete
     - Load: task-status.yaml and files-created.yaml for reference
     - Resume: from next_action - do NOT restart from beginning
     - DO NOT re-read completed tasks' code
     - FOCUS: Continue exactly where you left off

  3. IF no progress file (fresh start):
     - Initialize .work/implementation/ directory structure
     - Create task-status.yaml with all tasks as "Not Started"
     - Begin Phase 0 (Initialization)
     - Create initial progress.yaml entry

  4. After each meaningful work unit:
     - Update progress.yaml immediately
     - Update task-status.yaml
     - Run tests to verify progress
     - Write clear next_action for potential resumption
     - Commit working code

  5. CHECKPOINT REQUIREMENTS:
     - After EVERY test written
     - After EVERY file created
     - After EVERY test passes
     - After EVERY task completes
     - Before ANY refactoring starts
  </resumption_protocol>

  <compaction_safe_practices>
    <practice>Write progress.yaml after every file created</practice>
    <practice>Update task-status.yaml immediately after task completion</practice>
    <practice>Update files-created.yaml every time a new file is created</practice>
    <practice>Run tests frequently to verify status</practice>
    <practice>Complete one task fully before starting another</practice>
    <practice>Document next_action with specific file path and code to write</practice>
    <practice>Commit after each task passes tests</practice>
    <practice>Never trust context memory for what's been done - trust .work/*.yaml files</practice>
  </compaction_safe_practices>
</context_compaction_survival>

<methodology>

## Phase 0: Initialization

Before starting any implementation:

1. **Check for existing progress**
   ```bash
   cat .work/implementation/progress.yaml 2>/dev/null || echo "NO_PROGRESS_FILE"
   ```

2. **If NO progress file (fresh start)**:
   - Create `.work/implementation/` directory
   - Create `progress.yaml` with all tasks as "Not Started"
   - Create `task-status.yaml` with T1-T11 entries
   - Create `files-created.yaml` (empty initially)
   - Initialize git repo if not already one: `git init`
   - Ready to proceed with T1

3. **If progress file EXISTS (resuming)**:
   - Read current_task, next_action from progress.yaml
   - Check which tasks are complete
   - Skip to that task
   - Continue from next_action (specific file and code)

## Phase 1: Execute Tasks T1-T11 (TDD Cycle)

For each task:

### Step A: RED - Write Failing Test
1. Create test file (e.g., `src/test/java/com/example/companieshouse/.../ClassNameTest.java`)
2. Write test cases covering requirements
3. Run test: `mvn test -Dtest=ClassName`
4. Verify test FAILS (RED)
5. Commit test: `git add src/test && git commit -m "T#: Test for [feature]"`

### Step B: GREEN - Implement Minimum Code
1. Create source file (e.g., `src/main/java/com/example/companieshouse/.../ClassName.java`)
2. Implement MINIMUM code to pass test
3. Run test: `mvn test -Dtest=ClassName`
4. Verify test PASSES (GREEN)
5. Commit code: `git add src/main && git commit -m "T#: Implementation of [feature]"`

### Step C: REFACTOR - Clean Up Code
1. Review code for quality issues
2. Improve names, extract methods, remove duplication
3. Ensure STD-003 compliance
4. Run tests: `mvn test -Dtest=ClassName`
5. Verify tests STILL PASS
6. Commit refactoring: `git add . && git commit -m "T#: Refactor [feature] for code quality"`

### Step D: UPDATE PROGRESS
1. Update progress.yaml (set task to "Complete")
2. Update task-status.yaml (mark task done)
3. Update files-created.yaml (add files created)
4. Write next_action for what comes next
5. Commit progress: `git add .work/implementation && git commit -m "Progress: Completed T#"`

### Step E: MOVE TO NEXT TASK
1. Read task description from docs/plan.md
2. Follow same RED-GREEN-REFACTOR cycle
3. Repeat until all tasks complete

## Phase 2: Integration Testing

After T1-T10 complete:

1. Run full test suite: `mvn clean test`
2. Verify all tests pass
3. Check coverage: `mvn jacoco:report`
4. Verify coverage meets targets (80%+ overall, 90%+ on client)

## Phase 3: Quality Verification

Before declaring complete:

1. **Code Coverage**
   ```bash
   mvn clean test jacoco:report
   open target/site/jacoco/index.html
   ```
   - Client implementation: 90%+
   - Config: 80%+
   - DTOs: 100%
   - Exceptions: 100%

2. **STD-003 Compliance**
   - [ ] Constructor injection used everywhere (@RequiredArgsConstructor)
   - [ ] No field injection (@Autowired on fields)
   - [ ] Proper generics (List<T>, Optional<T>, not raw types)
   - [ ] Custom exceptions extend RuntimeException
   - [ ] Exception messages include context
   - [ ] No hardcoded configuration
   - [ ] All public methods have JavaDoc
   - [ ] All tests have @DisplayName describing what's tested

3. **Build Verification**
   ```bash
   mvn clean install
   ```
   - No build warnings
   - All tests pass
   - JAR builds successfully

4. **Final Commit**
   ```bash
   git add .
   git commit -m "Complete: Implementation ready for integration testing"
   ```

</methodology>

<output_specifications>

### Source Code Files
Create in: `src/main/java/com/example/companieshouse/`

Following package structure from architecture.md:

```
src/main/java/com/example/companieshouse/
├── client/
│   ├── CompaniesHouseClient.java (interface)
│   ├── CompaniesHouseClientImpl.java (implementation)
│   └── exception/
│       ├── CompaniesHouseApiException.java
│       ├── CompanyNotFoundException.java
│       ├── RateLimitExceededException.java
│       └── InvalidCompanyNumberException.java
│
├── dto/
│   ├── response/
│   │   ├── RegisteredAddressResponse.java
│   │   └── ApiErrorResponse.java
│   │
│   └── error/
│       └── ErrorDetails.java
│
└── config/
    ├── CompaniesHouseConfig.java
    └── CompaniesHouseProperties.java
```

### Test Files
Create in: `src/test/java/com/example/companieshouse/`

Mirror structure with Test suffix:
```
src/test/java/com/example/companieshouse/
├── client/
│   ├── CompaniesHouseClientImplTest.java
│   ├── CompaniesHouseClientIntegrationTest.java
│   └── exception/
│       └── (exception tests if needed)
│
├── dto/
│   └── response/
│       ├── RegisteredAddressResponseTest.java
│       └── ApiErrorResponseTest.java
│
└── config/
    ├── CompaniesHouseConfigTest.java
    └── CompaniesHousePropertiesTest.java
```

### Configuration Files

**src/main/resources/application.yml**
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

**src/test/resources/application-test.yml**
```yaml
spring:
  application:
    name: companies-house-client-test

companies-house:
  api:
    base-url: http://localhost:8089  # WireMock
    key: test-api-key
    connect-timeout-ms: 2000
    read-timeout-ms: 5000
```

### Documentation Files

**README.md** (created in T11):
- Setup instructions
- Configuration guide
- Usage examples
- Test running instructions
- Link to architecture and requirements

### Tracking Files
Create in: `.work/implementation/`
- `progress.yaml` - Update after every file and task
- `task-status.yaml` - Update after tasks complete
- `files-created.yaml` - Update as files are created

</output_specifications>

<critical_reminders>

================================================================================
                    CRITICAL REMINDERS
================================================================================

1. **CHECK PROGRESS FIRST**
   - BEFORE starting ANY work, check for existing progress.yaml
   - If it exists, read next_action and resume from that exact point
   - Do NOT restart tasks that are already complete
   - Trust .work/implementation/*.yaml files, not context memory

2. **STATE IN FILES, NOT CONTEXT**
   - progress.yaml is the source of truth
   - Context will compact and you will lose information
   - EVERYTHING important must be in .work/implementation/ files
   - Checkpoint after every meaningful work unit

3. **TDD DISCIPLINE**
   - Test FIRST, always
   - RED → GREEN → REFACTOR → COMMIT cycle
   - Never skip tests "for speed"
   - Tests are your safety net and documentation

4. **COMPLETE TASKS IN ORDER**
   - Follow task dependencies from docs/plan.md
   - Don't skip tasks or reorder
   - Each task builds on previous ones
   - T7 Part 1 must complete before T7 Part 2

5. **STD-003 COMPLIANCE**
   - Constructor injection with @RequiredArgsConstructor (NOT field injection)
   - Proper generics: List<T>, Optional<T> (NOT raw types)
   - Custom exceptions with context (include company number, status code)
   - No hardcoded configuration - use application.yml or @ConfigurationProperties
   - JavaDoc on all public methods
   - Test names with @DisplayName

6. **FOLLOW THE PLAN**
   - Implementation prompt will execute tasks from docs/plan.md
   - Each task has specific files to create
   - Each task has specific test scenarios
   - Each task has "Definition of Done" criteria
   - Satisfy all criteria before marking task complete

7. **TESTING REQUIREMENTS**
   - Unit tests: Mock RestTemplate, don't call real API
   - Integration tests: Use WireMock to simulate API
   - Coverage targets: 90%+ on client, 80%+ on config, 100% on DTOs/exceptions
   - Test BOTH success and error paths

8. **COMMITS MUST WORK**
   - Every commit must have passing tests
   - No "broken" commits that fail tests
   - Enables rollback if needed
   - Enables debugging during implementation

9. **NO HARDCODED SECRETS**
   - API key: NEVER hardcoded in source
   - API key: Externalized to application.yml or environment variable
   - Test API key: In application-test.yml (gitignored in production)
   - .gitignore must include application-local.yml

10. **COMMUNICATE WITH FILES**
    - Use progress.yaml to communicate status to future self
    - Write clear next_action (include specific file path, what to implement)
    - Update all three tracking files (.work/implementation/*.yaml)
    - Include timestamps for debugging

================================================================================

</critical_reminders>

<begin>

================================================================================
                    BEGIN IMPLEMENTATION
================================================================================

**FIRST ACTION - CHECK FOR EXISTING PROGRESS:**
```bash
cat .work/implementation/progress.yaml 2>/dev/null || echo "NO_PROGRESS_FILE"
```

**IF progress file exists (resuming):**
  1. Read the current_task, current_phase, next_action
  2. Skip to that task - do NOT restart from beginning
  3. Follow next_action exactly as written
  4. Continue from where you left off

**IF no progress file (fresh start):**
  1. Create .work/implementation/ directory
  2. Initialize progress.yaml with all tasks as "Not Started"
  3. Initialize task-status.yaml with T1-T11 entries
  4. Begin Phase 0: Initialization
  5. Create initial progress.yaml entry
  6. Proceed with T1

================================================================================
                    WORKFLOW FOR EACH TASK
================================================================================

For each task you execute:

**RED Phase** (Write Failing Test):
1. Create test file
2. Write test cases
3. Run: mvn test -Dtest=ClassName
4. Verify FAILS (RED)
5. Commit: git add src/test && git commit -m "T#: Test for [feature]"
6. Update progress.yaml

**GREEN Phase** (Implement Code):
1. Create source file
2. Implement MINIMUM code to pass test
3. Run: mvn test -Dtest=ClassName
4. Verify PASSES (GREEN)
5. Commit: git add src/main && git commit -m "T#: Implementation"
6. Update progress.yaml

**REFACTOR Phase** (Clean Up):
1. Review for STD-003 compliance
2. Improve code quality
3. Run tests: mvn test -Dtest=ClassName
4. Verify still PASS
5. Commit: git add . && git commit -m "T#: Refactor"
6. Update progress.yaml

**UPDATE Phase**:
1. Update progress.yaml (task → Complete)
2. Update task-status.yaml
3. Update files-created.yaml
4. Write next_action
5. Commit: git add .work/implementation && git commit -m "Progress: Completed T#"

**MOVE TO NEXT TASK**:
1. Read T# from docs/plan.md
2. Repeat cycle

================================================================================
                    REMEMBER
================================================================================

- Check .work/implementation/progress.yaml on session start
- Update it after every file created
- Run tests frequently
- Commit working code
- Document next_action clearly
- Never skip ahead or skip tasks
- Follow TDD discipline (test first, always)
- Verify STD-003 compliance
- No hardcoded secrets or configuration

You can do this! Start with T1, follow the plan, track progress, and commit working code.

================================================================================

</begin>

