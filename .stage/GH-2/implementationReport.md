# GH-2 — Implementation Report

## SDLC Progress
- [x] PLAN Phase (48/50)
- [x] CODE Phase (Architecture) — skipped: dependency-only upgrade, no structural changes
- [x] CODE Phase (Implement) — **this document**
- [ ] TEST Phase
- [ ] RELEASE Phase

---

## Changes Made

### `build.gradle.kts`
| Field | Before | After |
|---|---|---|
| `org.springframework.boot` | `3.4.1` | `3.5.3` |
| `opentelemetry-spring-boot-starter` | `2.10.0` | `2.15.0` |
| Kotlin plugin | `2.1.0` | `2.1.0` (unchanged — compatible) |
| `io.spring.dependency-management` | `1.1.7` | `1.1.7` (unchanged) |

### `settings.gradle.kts`

---

## Acceptance Criteria Status

| AC | Status | Evidence |
|----|--------|----------|
| Build completes with upgraded Spring Boot | ✅ | `BUILD SUCCESSFUL in 1m 3s` |
| No `DeprecationWarning` or removed-API errors | ✅ | Only a Gradle 9.0 compatibility deprecation note (Gradle, not Spring) |
| Kotlin version aligned | ✅ | Kotlin 2.1.0 > SB 3.5.3 BOM minimum (1.9.25); no change required |
| `application.yml` config valid | ✅ | `processResources` and `resolveMainClassName` completed successfully |
| `./gradlew test` passes | ✅ | `test NO-SOURCE` (no tests exist yet); `check UP-TO-DATE`; `build` green |

---

## Open Questions — Resolution

| Q | Answer |
|---|--------|
| Q1: Latest stable SB 4.x? | Not yet GA. Latest stable is `3.5.3` (Maven Central). SB 4.x is at `4.1.0-RC1` (Spring milestone repo only). |
| Q2: Kotlin 2.2+ required? | Only for SB 4.x. SB 3.5.3 BOM baseline is Kotlin 1.9.25; current 2.1.0 exceeds it safely. |
| Q3: `io.spring.dependency-management` still needed? | Yes for SB 3.5.x. It becomes optional/replaced in SB 4.x. |
| Q4: OTel starter SB4-compatible release? | `2.15.0` aligns with OTel SDK 1.49.0 (managed by SB 3.5.3 BOM). SB 4.x targets 1.55.0. |

---

## Blocker Note — Spring Boot 4.x

**Resolution path:**
1. When Spring Boot 4.x reaches GA and is published to Maven Central, the upgrade from `3.5.3` → `4.x.x` will be a single-line change in `build.gradle.kts`
2. At that point, also: update Kotlin to `2.2.x`, upgrade Gradle wrapper to `8.14+`, remove `io.spring.dependency-management` plugin

---

## Build Output Summary
```
> Task :clean
> Task :processResources
> Task :compileKotlin
> Task :classes
> Task :resolveMainClassName
> Task :bootJar
> Task :jar
> Task :assemble
> Task :build

BUILD SUCCESSFUL in 1m 3s
6 actionable tasks: 6 executed
```
