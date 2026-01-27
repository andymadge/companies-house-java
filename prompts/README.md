# Companies House API Integration - Prompt Suite

This directory contains a complete suite of 5 prompts for building a Companies House API integration in Java Spring Boot using Test-Driven Development (TDD).

## Overview

The prompt suite follows a sequential workflow where each prompt builds on the outputs of previous prompts:

```
01-requirements → 02-architecture → 03-planning → 04-implementation → 05-testing
     ↓                  ↓                ↓              ↓                ↓
requirements.md   architecture.md    plan.md        src/ code       test-report.md
```

## The 5 Prompts

### 01 - Requirements Gathering
**File**: `01-requirements-gathering.md`
**Complexity**: SIMPLE - Single session
**Purpose**: Discover functional and non-functional requirements by analyzing the Companies House API

**Workflow**:
1. Use WebFetch to analyze Companies House API documentation
2. Compare endpoint options and select the best one
3. Document requirements, data model, and acceptance criteria
4. Output: `docs/requirements.md`

**Estimated Time**: 30-45 minutes
**Prerequisite**: None

---

### 02 - Architecture Design
**File**: `02-architecture-design.md`
**Complexity**: MODERATE - Usually single session
**Purpose**: Design layered architecture with components and decisions

**Workflow**:
1. Read requirements.md
2. Read STD-003 Java Spring Boot rubric
3. Design components and their responsibilities
4. Create Architecture Decision Records (ADRs) for key decisions
5. Specify complete package structure and file layout
6. Output: `docs/architecture.md`

**Estimated Time**: 45-60 minutes
**Prerequisite**: 01-requirements-gathering complete

---

### 03 - Implementation Planning
**File**: `03-implementation-planning.md`
**Complexity**: SIMPLE - Single session
**Purpose**: Create detailed task breakdown and TDD workflow plan

**Workflow**:
1. Read requirements.md and architecture.md
2. Plan project initialization (Maven, directories, .gitignore)
3. Break into 11 tasks (T1-T11) with TDD descriptions
4. Create dependency graph and critical path
5. Define quality gates and coverage targets
6. Output: `docs/plan.md`

**Estimated Time**: 45-60 minutes
**Prerequisite**: 02-architecture-design complete

---

### 04 - TDD Implementation ⭐ (Context Compaction Survival)
**File**: `04-tdd-implementation.md`
**Complexity**: COMPLEX - Multi-session with progress tracking
**Purpose**: Execute test-first development of all components

**Workflow**:
1. Check for existing progress in `.work/implementation/progress.yaml`
2. If resuming: Read next_action and continue from that point
3. If fresh start: Initialize .work/implementation/ directory structure
4. Execute tasks T1-T11 following TDD discipline:
   - RED: Write failing test
   - GREEN: Implement minimum code
   - REFACTOR: Clean up
   - COMMIT: Save working code
   - UPDATE: Progress files
5. Output: `src/main/java/`, `src/test/java/`, `.work/implementation/*.yaml`

**Estimated Time**: 3-5 hours total (can span multiple sessions)
**Prerequisite**: 03-implementation-planning complete
**Special Feature**: Full context compaction survival with progress tracking

---

### 05 - Comprehensive Testing
**File**: `05-comprehensive-testing.md`
**Complexity**: MODERATE - Single session for testing phase
**Purpose**: Execute comprehensive testing and generate test report

**Workflow**:
1. Run existing unit test suite
2. Identify and add missing test cases
3. Execute integration tests with WireMock
4. Test end-to-end scenarios
5. Generate code coverage report (target: 80%+)
6. Verify STD-003 compliance
7. Create comprehensive test report
8. Output: `docs/test-report.md`, JaCoCo coverage reports

**Estimated Time**: 1-2 hours
**Prerequisite**: 04-tdd-implementation complete (T1-T11 all done)

---

## How to Use This Prompt Suite

ALWAYS USE PLAN MODE IN CLAUDE CODE FOR ALL PROMPTS.

### Quick Start

1. **Start Prompt 01**: Copy the entire prompt into Claude Code
   ```
   Use file: prompts/01-requirements-gathering.md
   ```

2. **When it completes**, it will produce `docs/requirements.md`

3. **Start Prompt 02**: Copy it into Claude Code, providing context that you've completed Prompt 01
   ```
   Use file: prompts/02-architecture-design.md
   Mention: "I just completed requirements gathering with output in docs/requirements.md"
   ```

4. **Continue sequentially**: Repeat for prompts 03, 04, 05

### For Prompt 04 (Implementation) - Context Compaction

Because Prompt 04 spans multiple sessions and files:

**First Session**:
- Run prompt 04
- It will initialize `.work/implementation/` and begin T1
- Work through tasks as context allows
- When context gets full, the prompt will guide you to save progress
- It automatically updates `.work/implementation/progress.yaml`

**Subsequent Sessions**:
- Start prompt 04 again
- It checks `.work/implementation/progress.yaml`
- It automatically resumes from `next_action`
- Continue from where you left off

**Key Files for Resumption**:
- `.work/implementation/progress.yaml` - Current task and next action
- `.work/implementation/task-status.yaml` - Status of all 11 tasks
- `.work/implementation/files-created.yaml` - Catalog of files created

### Full Project Workflow

```
Session 1:
  Run 01-requirements → produces docs/requirements.md

Session 2:
  Run 02-architecture → produces docs/architecture.md

Session 3:
  Run 03-planning → produces docs/plan.md

Sessions 4-6 (Implementation - may span multiple):
  Run 04-implementation
  → Check .work/implementation/progress.yaml to resume
  → Complete tasks T1-T11
  → Produces src/**/*.java, src/**/*Test.java

Session 7:
  Run 05-testing
  → Run comprehensive tests
  → Generate coverage reports
  → Produces docs/test-report.md
```

## Architecture Decisions

Based on clarifications provided:

| Decision | Choice |
|----------|--------|
| **Package structure** | `com.example.companieshouse` |
| **Component type** | Client library (no REST controllers) |
| **Build tool** | Maven |
| **Config format** | application.yml |
| **API key setup** | Local override file (application-local.yml) |

These are baked into the prompts and don't need to be re-specified.

## File Structure Created

After completing all prompts:

```
company-house-api/
├── .work/implementation/                    # Internal progress tracking (gitignored)
│   ├── progress.yaml
│   ├── task-status.yaml
│   └── files-created.yaml
│
├── docs/                                    # Deliverable documents
│   ├── requirements.md
│   ├── architecture.md
│   ├── plan.md
│   └── test-report.md
│
├── prompts/                                 # Prompt files
│   ├── 01-requirements-gathering.md
│   ├── 02-architecture-design.md
│   ├── 03-implementation-planning.md
│   ├── 04-tdd-implementation.md
│   ├── 05-comprehensive-testing.md
│   ├── README.md (this file)
│   └── rubrics/
│       ├── STD-001-prompt-creation-rubric.md
│       └── STD-003-java-spring-boot-development-rubric.md
│
├── src/main/java/com/example/companieshouse/  # Implementation
│   ├── client/
│   │   ├── CompaniesHouseClient.java
│   │   ├── CompaniesHouseClientImpl.java
│   │   └── exception/
│   ├── dto/
│   │   ├── response/
│   │   └── error/
│   └── config/
│
├── src/test/java/com/example/companieshouse/  # Tests
│   ├── client/
│   ├── dto/
│   └── config/
│
├── src/main/resources/
│   ├── application.yml
│   └── application-local.yml.example
│
├── pom.xml
├── .gitignore
└── README.md
```

## Key Features of This Prompt Suite

### 1. XML Prompting Structure
All prompts follow Anthropic's XML prompting guidelines with proper sections:
- `<context>` - Project, role, objective
- `<instructions>` - Step-by-step workflow
- `<output_specifications>` - Expected deliverables
- `<critical_reminders>` - Important points
- `<begin>` - How to start

### 2. STD-003 Compliance
All prompts reference and enforce the Java Spring Boot development rubric:
- Constructor injection (no field injection)
- Proper generics (no raw types)
- Custom exceptions with context
- Comprehensive testing
- Clean architecture
- Security best practices

### 3. Context Compaction Survival
Prompt 04 uses full STD-001 patterns:
- Progress tracking in `.work/implementation/`
- Resumption protocol for cold starts
- Clear `next_action` for continuation
- All state stored in files, not context memory

### 4. TDD Enforcement
Prompt 04 ensures Test-Driven Development:
- RED phase: Write failing test first
- GREEN phase: Implement minimum code
- REFACTOR phase: Clean up
- COMMIT phase: Save working code
- PROGRESS: Track in .work/ files

### 5. Quality Gates
All prompts include measurable quality criteria:
- Code coverage targets (80%+ overall, 90%+ on core)
- STD-003 compliance checklist
- Definition of done for each task
- Pre-commit verification steps

## Tips for Success

### Before Starting

1. **Read the plan first**: `typed-wondering-candy.md` at `.claude/plans/` explains the overall strategy
2. **Understand the flow**: Each prompt depends on previous ones
3. **Be prepared for Prompt 04**: It's the longest and spans multiple sessions

### During Execution

1. **Follow TDD discipline**: Tests first, always
2. **Commit frequently**: Save working code regularly
3. **Track progress**: Update `.work/implementation/` files as you go
4. **Test before moving on**: Ensure tests pass before starting next task
5. **Reference the rubrics**: STD-001 and STD-003 are your guides

### When Context Gets Full (Prompt 04)

1. The prompt will remind you to save progress
2. Update `.work/implementation/progress.yaml`
3. Commit your work: `git add . && git commit -m "Progress: [description]"`
4. Start a new session
5. Run prompt 04 again
6. It will automatically resume from `next_action`

## Understanding the Output

### After Prompt 01
- `docs/requirements.md` - Full requirements document with functional/non-functional requirements

### After Prompt 02
- `docs/architecture.md` - Component design with 7 ADRs, complete package structure

### After Prompt 03
- `docs/plan.md` - 11 tasks with TDD workflow, file specifications, dependency graph

### After Prompt 04
- `src/main/java/` - Complete implementation (12-15 Java files)
- `src/test/java/` - Complete tests (10-15 test files)
- `.work/implementation/progress.yaml` - Final progress snapshot
- `pom.xml` - Maven configuration

### After Prompt 05
- `docs/test-report.md` - Comprehensive test report with coverage metrics
- JaCoCo coverage report - Detailed line-by-line coverage

## Frequently Asked Questions

**Q: Can I skip a prompt?**
A: No. Each prompt builds on previous outputs. They're sequential.

**Q: What if I need to change something from a previous prompt?**
A: You can update the output document and re-run dependent prompts with the updated input.

**Q: How long does Prompt 04 (Implementation) take?**
A: Typically 3-5 hours depending on familiarity with Spring Boot and TDD. Can span multiple sessions.

**Q: What if I get stuck on a task?**
A: Check the `next_action` in `progress.yaml` for exactly what to do next. Read the task description from `plan.md` for more context.

**Q: How do I know if I'm following STD-003?**
A: Prompt 05 has a STD-003 verification checklist. Also, Prompt 04 reminds you of key standards throughout.

**Q: What should I commit to git?**
A: Everything in `src/` and `docs/`. The `.work/implementation/` directory is for internal tracking (add to `.gitignore`).

## Support and Troubleshooting

### If Tests Fail in Prompt 04

1. Check the specific test error message
2. The test failure tells you what code is wrong
3. Fix the implementation
4. Re-run the test
5. When it passes, continue to next task

### If Coverage Is Below Target

1. Check which lines aren't covered in JaCoCo report
2. Write tests for uncovered scenarios
3. Verify tests pass
4. Re-run coverage report
5. Repeat until targets met

### If You Get Confused

1. Read `docs/requirements.md` to understand what you're building
2. Read `docs/architecture.md` to understand how it's structured
3. Read `docs/plan.md` to understand the task breakdown
4. Check `.work/implementation/progress.yaml` for where you are
5. Read the specific task description for details

## Related Documentation

- **Plan File**: `/Users/andym/.claude/plans/typed-wondering-candy.md` - Overall strategy and design
- **STD-001**: `rubrics/STD-001-prompt-creation-rubric.md` - Prompt creation standards
- **STD-003**: `rubrics/STD-003-java-spring-boot-development-rubric.md` - Java Spring Boot standards
- **Companies House API**: https://developer.company-information.service.gov.uk/overview

## Getting Started

Ready to begin? Here's what to do:

1. Copy the entire contents of `prompts/01-requirements-gathering.md`
2. Paste into Claude Code
3. Press Enter and let the prompt guide you through requirements gathering
4. When it completes and generates `docs/requirements.md`, move to Prompt 02

Good luck! 🚀

---

**Last Updated**: 2026-01-27
**Version**: 1.0
