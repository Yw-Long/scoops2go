# Software Testing & Quality Assurance CW2 - Scoops2Go

Welcome! This repository contains starter code for the Scoops2Go system. Your task in CW2 is to create a test suite (including test case design specification + defect report) for this system. **Please see the CW2 assessment brief for further details and instructions!**

## Your details

Name: **Yunwei Long**
Student ID: **21906298**

---

## Summary of work

This repository contains a complete test suite for the Scoops2Go v1.0 system, covering automated unit/integration tests, a test case design specification, and a defect report. The three submission components are located as described below.

---

## 1. Test Case Design Specification

**File:** `TestCases.xlsx`  
**Location:**
```
Yunwei_Long_21906298/TestCases.xlsx
```
The spreadsheet documents 71 test cases across the following feature areas:

| Prefix | Feature Area |
|--------|-------------|
| `PB_TC` | Product Browsing |
| `TC_TC` | Treat Configuration |
| `BK_TC` | Basket & Order |
| `OT_TC` | Order Tracking |
| `PR_TC` | Promotions |
| `UI_TC` | UI & Accessibility |
| `SEC_TC` | Security |
| `PERF_TC` | Performance |

Each test case includes: ID, description, prerequisites, test type, steps, expected result, actual result, pass/fail status, and linked defect ID (where applicable).

---

## 2. Implemented Automated Tests (SUT Repository)

All automated tests are located under:

```
api/src/test/java/scoops2Go/
```

### How to run

```bash
cd scoops2go-main/api
mvn test
```

**Requirements:** JDK 23, Maven 3.x. The test suite uses an H2 in-memory database — no external database setup is needed.

### Test class index

| File | Test IDs Covered | Description |
|------|-----------------|-------------|
| `api/src/test/java/scoops2Go/api/ProductApiTests.java` | PB_TC_004, PB_TC_005, PB_TC_006 | GET /api/product and GET /api/product/{id} — endpoint availability and field name validation |
| `api/src/test/java/scoops2Go/api/ParameterisedProductApiTests.java` | PB_TC_005 (extended), PB_TC_006 (extended) | Parameterised tests using external CSV data (`products.csv`) covering all seed product IDs and BVA invalid IDs |
| `api/src/test/java/scoops2Go/api/OrderApiTests.java` | BK_TC_010, BK_TC_014A/B, BK_TC_015, BK_TC_017, OT_TC_004, OT_TC_005 | POST/PUT/GET order endpoints and checkout flow |
| `api/src/test/java/scoops2Go/api/PerformanceApiTests.java` | PERF_TC_003 | GET /api/product response time ≤ 500 ms (REQ-PER-002), 3 repeated runs |
| `api/src/test/java/scoops2Go/service/OrderServicePricingTests.java` | BK_TC_005, BK_TC_006, BK_TC_007, BK_TC_011, BK_TC_012, BK_TC_013 | Summer surcharge boundary testing (REQ-BR-002) and delivery time formula (REQ-BR-003) |
| `api/src/test/java/scoops2Go/service/OrderServicePromotionTests.java` | PR_TC_001–PR_TC_004, PR_TC_005–PR_TC_011, PR_TC_012, PR_TC_014, PR_TC_017, PR_TC_019 | All four promotion rules: luckyForSome, megaMelt100, frozen40, tripleTreat3 |
| `api/src/test/java/scoops2Go/service/OrderServiceValidationTests.java` | TC_TC_006–TC_TC_009, BK_TC_003, BK_TC_004 | Treat product validation and basket size validation (BVA) |
| `api/src/test/java/scoops2Go/ui/UiAutomationTests.java` | UI_TC_008, UI_TC_009, SEC_TC_001, PERF_TC_001, PERF_TC_002 | Selenium WebDriver tests — cross-browser compatibility, GBP currency format, unauthenticated order access, and UI performance |

### Test data

Parameterised product tests read from:

```
api/src/test/resources/testdata/products.csv
```

### Coverage report

JaCoCo raw execution data is generated automatically during `mvn test`:

```
scoops2go-main/api/target/jacoco.exec
```

To generate the HTML report, run the following after `mvn test`:

```bash
cd scoops2go-main/api
mvn jacoco:report
```

The HTML report will then be available at:

```
scoops2go-main/api/target/site/jacoco/index.html
```

Approximate line coverage of the API module: **~65%**

### Security analysis

Snyk SCA scan results are saved at:

```
api/snyk-sca-results.txt
```

To reproduce: install Snyk CLI, set `$env:SNYK_TOKEN`, then run `snyk test` from the `scoops2go-main/api/` directory.

---

## 3. Defect Report

**File:** `Defect_Report.docx`  
**Location:**
```
Yunwei_Long_21906298/Defect_Report.docx
```

The report contains two sections:

- **Test Summary** — scope, environment, statistics (71 test cases designed, 58 executed, 74% pass rate), per-test-case pass/fail table, and recommendations.
- **Defect Log** — 17 defects documented with ID, description, steps to reproduce, expected/actual behaviour, severity/priority, status, and source test case ID.

### Defect severity summary

| Severity | Count | Defect IDs |
|----------|-------|-----------|
| Critical | 2 | BK_D001, SEC_D001 |
| Major | 12 | PB_D001, PB_D002, BK_D003, SEC_D002, UI_D001, UI_D003, UI_D004, UI_D005, UI_D006, UI_D007, PR_D001, PR_D002 |
| Minor | 2 | TC_D001, BK_D002 |
| Trivial | 1 | UI_D002 |

---

## 4. Postman Collection

**File:** `Scoops2Go Test Suite.postman_collection.json`  
**Location:**
```
Yunwei_Long_21906298/Scoops2Go Test Suite.postman_collection.json
```

## 5. Continuous Integration (CI)
Continuous Integration is implemented using GitHub Actions to automate the build, test, and quality assurance process.

The CI pipeline is triggered automatically on every push and pull request to the repository, and performs the following tasks:
1. Checkout the latest code
2. Set up JDK 23 and Maven 3.x
3. Build the project and resolve dependencies
4. Execute all automated tests (API, service, UI, performance, security)
5. Generate JaCoCo test coverage report
6. Run Snyk SCA security scan
7. Publish test results and build status

CI configuration file:
```
.github/workflows/ci.yml
```

The collection covers API-level performance and security tests organised into three folders:

| Folder | Test IDs Covered | Description |
|--------|-----------------|-------------|
| Performance | PERF_TC_004, PERF_TC_005 | Response time validation for order and checkout endpoints |
| Security | SEC_TC_002, SEC_TC_003 | Unauthorised access and input validation checks |
| Order API | BK_TC_010, BK_TC_015 | Order creation and status retrieval |

### How to import and run

1. Open Postman and click **Import**
2. Select `Scoops2Go Test Suite.postman_collection.json`
3. Ensure the Scoops2Go API is running locally on `http://localhost:8080`
4. Click **Run collection** to execute all requests and view test results

> **Tool version:** Postman v11.92.0 (x64, Windows 11)

---

## Repository structure

```
scoops2go-main/
├── api/                        ← Spring Boot REST API (System Under Test)
│   ├── src/
│   │   ├── main/               ← Production source code
│   │   └── test/
│   │       ├── java/scoops2Go/
│   │       │   ├── api/        ← API integration tests
│   │       │   ├── service/    ← Unit tests (service layer)
│   │       │   └── ui/         ← Selenium UI & performance tests
│   │       └── resources/
│   │           └── testdata/
│   │               └── products.csv   ← External test data (parameterised tests)
│   └── snyk-sca-results.txt    ← SCA security scan output
│   └── lighthouse-report.pdf   ← lighthouse scan output
├── app/                        ← Vue.js frontend
└── README.md
```