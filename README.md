# TeaGo

TeaGo is a tea-industry management platform for land owners and factory owners. It covers factory registration, tea deliveries, rate-based payment calculations, fertilizer requests, loans and advances, cheque tracking, employee records, daily wages, tea collections, complaints, monthly bills, income statements, and external payment settlements.

The project keeps its original technology stack:

- Java 23 and Spring Boot 4.0.4
- Maven, Spring Data JPA, MySQL, and Lombok
- Spring Security with server-side sessions, BCrypt passwords, CSRF protection, and ownership checks
- Static HTML, CSS, and JavaScript frontend
- H2 only for automated tests

## Run locally on Windows

Start MySQL, then run from the repository root:

```powershell
$env:TEAGO_DB_USERNAME = 'root'
$env:TEAGO_DB_PASSWORD = 'your-local-password'
.\run.ps1
```

Open <http://localhost:8080>. The script builds and tests the project, starts TeaGo on loopback, and uses the separate `teago_local` database by default. It does not replace an existing `teago` database.

Useful commands:

```powershell
.\stop.ps1
.\run.ps1 -NoBuild
.\run.ps1 -Port 8081
```

For setup details, business rules, API conventions, migration notes, and test coverage, see [teago/README.md](teago/README.md).

## Verify

```powershell
mvn "-Dmaven.repo.local=$PWD/.m2/repository" -f teago/pom.xml test
node --check teago/src/main/resources/static/js/api.js
node --check teago/src/main/resources/static/js/index.js
node --check teago/src/main/resources/static/js/dashboard-owner.js
node --check teago/src/main/resources/static/js/dashboard-factory.js
```

The test suite uses an isolated in-memory H2 database. The local development database is configured through `TEAGO_DB_URL`, `TEAGO_DB_USERNAME`, and `TEAGO_DB_PASSWORD`; credentials are not committed.

Only land owner and factory owner accounts are enabled. Supervisor remains reserved until assignment data and a dedicated dashboard are implemented.
