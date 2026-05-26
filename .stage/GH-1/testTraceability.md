# GH-1 — Test Traceability Matrix

| Acceptance Criterion | Test ID(s) | Coverage |
|---------------------|------------|----------|
| ES dependency removed → build passes | TEST-001 (context load implies compile success) | ✅ |
| Health endpoint responds without ES component | TEST-002, TEST-003 | ✅ |
| No `es.*` metrics at prometheus endpoint | TEST-004, TEST-005 | ✅ |
| No ES beans in Spring context | TEST-001 (no exception on startup) | ✅ |
| `./gradlew test` passes without Testcontainers | All tests pass without Docker/TC | ✅ |
| App starts on port 8080 with no errors | TEST-001 | ✅ |
