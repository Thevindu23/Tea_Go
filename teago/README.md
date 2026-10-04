# TeaGo

TeaGo connects tea land owners and factory owners. The existing stack is retained: Java 23, Spring Boot 4.0.4, Maven, MySQL, Spring Data JPA, Spring Security, Lombok, and static HTML/CSS/JavaScript. H2 is used only for automated tests.

## Run on Windows

Use a Java 23-compatible JDK and a running MySQL server. This workspace was built with JDK 26 targeting Java 23 bytecode. No frontend build or Node server is needed.

From the workspace root (the directory above this file):

```powershell
# Set these only if your local credentials differ from the defaults.
$env:TEAGO_DB_USERNAME = 'root'
$env:TEAGO_DB_PASSWORD = 'your-local-password'
.\run.ps1
```

Open http://localhost:8080. `run.ps1` builds and tests the project, starts a hidden process bound to loopback, and uses the separate `teago_local` database. It does not overwrite the original `teago` database. Logs and the PID are in `.runtime` at the workspace root. The local Maven cache is `.m2` there.

```powershell
.\stop.ps1
.\run.ps1 -NoBuild        # reuse the last verified jar
.\run.ps1 -Port 8081      # use another local port
```

For an existing database, explicitly set `TEAGO_DB_URL`. The application's default URL uses `teago`; the run script overrides this to `teago_local`. Passwords are read from the environment, never committed. Use MySQL 8+ for a supported deployment. The installed local server identifies itself as MariaDB; the live development smoke test also covers its compatible MySQL connection and row-lock query.

## Local demonstration accounts

The running `teago_local` development database contains `demo.owner` and `demo.factory`, both with password `TeaGoDemo2026!`. These are fictitious local test accounts, not automatic seeds. A fresh database starts empty; register accounts through the landing page. Never use these demonstration credentials on a shared deployment.

## Workflows

1. Register a land owner and a factory owner. Passwords must contain 8–72 characters; BCrypt also limits passwords to 72 UTF-8 bytes.
2. As the land owner, register with a factory. The factory approves or rejects the registration. Rejected registrations can be submitted again.
3. The factory sets a rate and records deliveries for approved land owners. A delivery creates one payment calculation using the rate effective on the delivery date. Later rate changes do not rewrite historical payments.
4. Land owners manage employees, enter daily wages and tea collections, submit loan/advance and fertilizer requests, and submit complaints. Daily worksheets hold one entry per employee and date; saving that cell again replaces its value.
5. Factories approve requests and issue loan cheques. Cheques move from Pending to In Transit to Delivered (or Returned, then In Transit again). Only the receiving land owner can verify a delivered cheque. One cheque is allowed per loan.
6. Either role can generate the permitted bills and statements. Factory statements contain only that factory's transactions. Land owner statements consolidate their factories. Repeated bill generation updates the same owner/scope/month record. Viewing a statement does not save bills.
7. Factories can record an external bank transfer or issued payment cheque with a reference. TeaGo does not connect to a bank or transfer money. The settlement persists and cannot be recorded twice for the same factory, owner, and month. If subsequent source edits change the amount, the summary shows **Review required**, rather than recording another settlement.

Only land owner and factory owner accounts are enabled. Supervisor remains a reserved model value; login, self-registration, and the old unscoped supervisor cheque API are unavailable until factory assignments and a dedicated dashboard are implemented.

## Calculation rules

- Delivery earnings: recorded weight × recorded rate, rounded to two decimal places.
- Delivery deductions: the entered non-negative amount.
- Loan deduction: the archive's original rule is preserved — 10% in the approval month. This is not a complete amortization schedule; no interest or additional installments are invented.
- Advance deduction: the full amount in the month after approval, exactly once, regardless of the order in which reports are viewed.
- Fertilizer deduction: approved fertilizer cost in the request month, preserving the existing rule and fixed unit price of 40.
- Net balance: earnings minus deductions. Negative balances remain visible; a negative balance cannot be recorded as a payout. There is no automatic carry-forward or bank settlement.
- Saved bills store a consistent deduction breakdown. Regeneration intentionally refreshes that snapshot; reports calculate from current source records. Monthly salary estimates and daily wage worksheets are separate views of employee pay.

## Security and API conventions

Spring Security uses server-side sessions, BCrypt passwords, role checks, and record ownership checks. Browser storage is only a display cache. No bearer token is required. Pages and APIs share the Spring Boot origin.

Fetch `/api/auth/csrf` with the session cookie and send the returned token in its named header on POST, PUT, and DELETE requests, including registration/login/logout. Login rotates both the session identifier and CSRF token. `/js/api.js` handles this for the existing pages. Unauthenticated API calls return 401; unauthorized calls return 403. Unscoped cross-account list endpoints are denied.

The missing worksheet endpoints are implemented under `/api/daily-wages` and `/api/tea-collections`. Payment calculations, bill previews, and external settlement records are available under `/api/payments` and `/api/bills`. Controllers document their paths and request types.

## Verify

From the workspace root:

```powershell
mvn "-Dmaven.repo.local=$PWD/.m2/repository" -f teago/pom.xml test
node --check teago/src/main/resources/static/js/api.js
node --check teago/src/main/resources/static/js/index.js
node --check teago/src/main/resources/static/js/dashboard-owner.js
node --check teago/src/main/resources/static/js/dashboard-factory.js
```

The integration suite exercises hashing, session authentication, CSRF, role and record isolation, daily-record validation/upserts, factory billing, repeatable bill generation, read-only statements, advance timing, negative balances, historical rates, cheque transitions, and persistent settlement records. Tests use an isolated in-memory H2 database, not your MySQL data.

## Existing data

The archive contains no database dump. Hibernate's existing `ddl-auto=update` remains for local development. Back up and review existing data before applying changes to a populated database: duplicate usernames/NICs, duplicate payments/cheques/registrations, new version columns, and bill scopes need explicit reconciliation. Existing plaintext passwords will not authenticate; migrate them to BCrypt or reset them before switching databases. Old bills should be regenerated to populate the saved deduction breakdown. This is not an automatic production migration.
