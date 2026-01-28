# Prompt 06: Test Gap Fixes - Companies House API Client

## Context

<context>
<project>Companies House API Client - Java Spring Boot library for retrieving UK company registered addresses</project>
<role>Test Engineer - Completing test coverage gaps identified in Prompt 05 comprehensive testing</role>
<objective>Add 2 missing test methods to achieve 100% coverage on error handling paths</objective>
<status>Library is production-ready (95% coverage, all 51 tests passing). These are non-blocking enhancements.</status>
</context>

---

## Background

Prompt 05 (Comprehensive Testing) generated a detailed test report at `docs/test-report.md` which identified:

**Production Readiness**: ✅ READY FOR PRODUCTION
**Current Coverage**: 95% overall, 95% on core client
**Tests Passing**: 51/51 (100%)

**Identified Test Gaps** (non-blocking):
1. **HTTP 503 Service Unavailable** - MEDIUM priority, 15 min effort
   - Currently handled by existing 5xx catch block in implementation
   - No explicit test verifying 503 behavior

2. **Malformed Retry-After Header** - LOW priority, 10 min effort
   - Lines 129-131 in `CompaniesHouseClientImpl.java` (NumberFormatException catch block) uncovered
   - Code handles gracefully, but lacks test for confidence

**Goal**: Add these 2 test methods to `CompaniesHouseClientImplTest.java` to close gaps and achieve 100% error path coverage.

---

## Foundational Principles

1. **Follow Existing Test Patterns**: Match the structure, naming, and assertion style of existing tests in `CompaniesHouseClientImplTest.java`
2. **Test-Driven Verification**: After adding tests, verify they pass and increase coverage
3. **Maintain Test Quality**: Use `@DisplayName`, Arrange-Act-Assert pattern, AssertJ assertions
4. **Single File Modification**: Only edit `CompaniesHouseClientImplTest.java` - no implementation changes needed
5. **Verify Coverage Impact**: Run JaCoCo after changes to confirm coverage improvement

---

## Prerequisites

**Required Files** (read before starting):
- `src/test/java/com/example/companieshouse/client/CompaniesHouseClientImplTest.java` - Target file for edits
- `src/main/java/com/example/companieshouse/client/CompaniesHouseClientImpl.java` - Reference for understanding error handling (lines 129-131)
- `docs/test-report.md` - Section 7.2 has exact test code recommendations

**Verification Commands**:
```bash
# Run tests to verify current state
mvn test -Dtest=CompaniesHouseClientImplTest

# Generate coverage report after changes
mvn clean test jacoco:report

# View coverage
open target/site/jacoco/index.html
```

---

## Task: Add Two Missing Test Methods

### Step 1: Read Current Test File Structure

**Action**: Read `CompaniesHouseClientImplTest.java` to understand:
- Existing test patterns (mocking, assertions, naming)
- Location where new tests should be added (likely near other HTTP error tests around lines 298-399)
- Imports needed (HttpServerErrorException, HttpHeaders)

### Step 2: Add HTTP 503 Test

**Location**: After `shouldThrowApiExceptionFor502()` test (around line 337)

**Test Code** (from test-report.md section 7.2):
```java
@Test
@DisplayName("Should throw API exception for HTTP 503 Service Unavailable")
void shouldThrowApiExceptionFor503() {
    // Arrange
    String companyNumber = "09370669";

    when(restClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);

    HttpServerErrorException exception = new HttpServerErrorException(
        HttpStatus.SERVICE_UNAVAILABLE, "Service Unavailable"
    );
    when(responseSpec.body(CompanyProfileResponse.class)).thenThrow(exception);

    // Act & Assert
    assertThatThrownBy(() -> client.getRegisteredAddress(companyNumber))
        .isInstanceOf(CompaniesHouseApiException.class)
        .hasMessageContaining("503")
        .hasMessageContaining(companyNumber);
}
```

**Purpose**: Verifies that HTTP 503 Service Unavailable is handled correctly (caught by 5xx error handler).

### Step 3: Add Malformed Retry-After Test

**Location**: After `shouldHandleRateLimitWithoutRetryAfter()` test (around line 276)

**Test Code** (from test-report.md section 7.2, enhanced):
```java
@Test
@DisplayName("Should handle malformed Retry-After header gracefully")
void shouldHandleMalformedRetryAfter() {
    // Arrange
    String companyNumber = "09370669";

    HttpHeaders headers = new HttpHeaders();
    headers.set("Retry-After", "not-a-number");  // Non-numeric value triggers NumberFormatException

    when(restClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);

    HttpClientErrorException exception = new HttpClientErrorException(
        HttpStatus.TOO_MANY_REQUESTS, "Rate Limited", headers, null, null
    );
    when(responseSpec.body(CompanyProfileResponse.class)).thenThrow(exception);

    // Act & Assert
    assertThatThrownBy(() -> client.getRegisteredAddress(companyNumber))
        .isInstanceOf(RateLimitExceededException.class)
        .hasMessageContaining("429")
        .hasMessageContaining(companyNumber)
        .satisfies(e -> {
            RateLimitExceededException rle = (RateLimitExceededException) e;
            assertThat(rle.getRetryAfterSeconds()).isNull(); // Graceful degradation
        });
}
```

**Purpose**: Verifies that malformed Retry-After headers are handled gracefully (NumberFormatException caught, returns null, line 129-131 in impl).

### Step 4: Verify Imports

Ensure these imports are present in `CompaniesHouseClientImplTest.java`:
```java
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
```

These should already exist from other tests, but verify after adding new tests.

### Step 5: Run Tests and Verify Coverage

**Action 1**: Run unit tests to ensure new tests pass:
```bash
mvn test -Dtest=CompaniesHouseClientImplTest
```

**Expected Result**:
- Total tests: 18 (was 16, now +2)
- All 18 passing
- New tests appear in output: `shouldThrowApiExceptionFor503`, `shouldHandleMalformedRetryAfter`

**Action 2**: Run full test suite:
```bash
mvn clean test
```

**Expected Result**:
- Total tests: 53 (was 51, now +2)
- All 53 passing

**Action 3**: Generate coverage report:
```bash
mvn clean test jacoco:report
```

**Action 4**: Check coverage improvement:
```bash
open target/site/jacoco/index.html
```

**Expected Coverage Improvement**:
- **CompaniesHouseClientImpl**: Should increase from 95% to **100%** (lines 129-131 now covered)
- **Overall**: Should increase from 95% to **96-97%**

**Verification**: Read `target/site/jacoco/com.example.companieshouse.client/CompaniesHouseClientImpl.html` and confirm lines 129-131 are now GREEN (covered).

---

## Output Specifications

### Modified File
**Path**: `src/test/java/com/example/companieshouse/client/CompaniesHouseClientImplTest.java`

**Changes**:
1. Add `shouldThrowApiExceptionFor503()` test method after line 337 (after `shouldThrowApiExceptionFor502()`)
2. Add `shouldHandleMalformedRetryAfter()` test method after line 276 (after `shouldHandleRateLimitWithoutRetryAfter()`)
3. Verify imports (HttpHeaders, HttpServerErrorException should already exist)

**Test Count**: 16 → 18 tests (+2)

### Verification Artifacts

**Coverage Report**: `target/site/jacoco/index.html`
- Verify overall coverage increased to 96-97%
- Verify CompaniesHouseClientImpl shows 100% coverage
- Verify lines 129-131 are covered (green in HTML report)

**Test Execution Output**:
- All 53 tests passing (51 existing + 2 new)
- No failures, no errors
- Execution time should remain similar (~69 seconds)

---

## Success Criteria

**Test Addition**:
- ✅ 2 new test methods added to `CompaniesHouseClientImplTest.java`
- ✅ Tests follow existing patterns (naming, structure, assertions)
- ✅ Tests use `@DisplayName` annotations with clear descriptions
- ✅ Arrange-Act-Assert structure maintained

**Test Execution**:
- ✅ All 53 tests pass (51 existing + 2 new)
- ✅ No test failures or errors
- ✅ Test execution time remains acceptable (<90 seconds)

**Coverage Improvement**:
- ✅ CompaniesHouseClientImpl coverage: 95% → **100%**
- ✅ Overall project coverage: 95% → **96-97%**
- ✅ Lines 129-131 (NumberFormatException catch) now covered
- ✅ HTTP 503 error handling path now covered

**Code Quality**:
- ✅ No changes to implementation code (tests only)
- ✅ Existing tests unchanged and still passing
- ✅ New tests match quality of existing tests

---

## Commit Guidelines

After completing the work and verifying all tests pass:

**Commit Message** (Conventional Commits format):
```
test: add missing HTTP 503 and malformed Retry-After tests

Add two test methods to CompaniesHouseClientImplTest to close
coverage gaps identified in Prompt 05 comprehensive testing:

1. shouldThrowApiExceptionFor503()
   - Tests HTTP 503 Service Unavailable error handling
   - Verifies 5xx catch block handles 503 correctly

2. shouldHandleMalformedRetryAfter()
   - Tests malformed (non-numeric) Retry-After header handling
   - Covers NumberFormatException catch block (lines 129-131)
   - Verifies graceful degradation (returns null)

Coverage improvement:
- CompaniesHouseClientImpl: 95% → 100%
- Overall project: 95% → 97%
- Total tests: 51 → 53

Closes test gaps from docs/test-report.md section 7.2

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>
```

**Git Commands**:
```bash
git add src/test/java/com/example/companieshouse/client/CompaniesHouseClientImplTest.java
git commit -m "[message above]"
git status  # Verify clean working tree
```

---

## Critical Reminders

1. **Read Before Edit**: Read `CompaniesHouseClientImplTest.java` completely to understand existing patterns before adding tests

2. **Match Existing Style**: New tests should be indistinguishable from existing tests in style, naming, and structure

3. **Tests Only**: Do NOT modify implementation code. The implementation already handles these cases correctly; we're just adding tests.

4. **Verify Coverage**: After adding tests, MUST verify coverage increased by checking JaCoCo report

5. **Run Full Suite**: Run `mvn clean test` (not just `mvn test`) to ensure no side effects and fresh coverage data

6. **Check Specific Lines**: After coverage generation, verify lines 129-131 in `CompaniesHouseClientImpl.java` are now green (covered)

---

## Quick Reference

### File Locations
- **Target File**: `src/test/java/com/example/companieshouse/client/CompaniesHouseClientImplTest.java`
- **Reference (impl)**: `src/main/java/com/example/companieshouse/client/CompaniesHouseClientImpl.java`
- **Test Report**: `docs/test-report.md` (section 7.2 has code samples)
- **Coverage Report**: `target/site/jacoco/index.html` (after running `mvn clean test jacoco:report`)

### Key Commands
```bash
# Read test file
cat src/test/java/com/example/companieshouse/client/CompaniesHouseClientImplTest.java

# Run unit tests only
mvn test -Dtest=CompaniesHouseClientImplTest

# Run full test suite
mvn clean test

# Generate coverage
mvn clean test jacoco:report

# View coverage report
open target/site/jacoco/index.html

# Check specific file coverage
open target/site/jacoco/com.example.companieshouse.client/CompaniesHouseClientImpl.java.html
```

### Expected Metrics
- **Before**: 51 tests passing, 95% coverage, lines 129-131 uncovered
- **After**: 53 tests passing, 97% coverage, lines 129-131 covered (green)
- **Time**: ~25 minutes (read 5 min, add tests 10 min, verify 10 min)

---

## Begin

1. **Read Test File**: Read `CompaniesHouseClientImplTest.java` to understand structure and locate insertion points

2. **Add Test 1**: Add `shouldThrowApiExceptionFor503()` after line 337

3. **Add Test 2**: Add `shouldHandleMalformedRetryAfter()` after line 276

4. **Verify Imports**: Check HttpHeaders and HttpServerErrorException imports exist

5. **Run Tests**: Execute `mvn clean test` and verify 53 tests pass

6. **Check Coverage**: Run `mvn clean test jacoco:report` and verify 97% coverage, lines 129-131 covered

7. **Commit**: Stage and commit changes with provided commit message

8. **Report**: Confirm final metrics (53/53 tests passing, 97% coverage, 100% on ClientImpl)

---

**Estimated Time**: 25-30 minutes
**Complexity**: LOW (simple test addition, no implementation changes)
**Risk**: MINIMAL (tests only, existing tests unaffected)
