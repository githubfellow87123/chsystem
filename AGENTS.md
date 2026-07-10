# AGENTS.md

Compact guidance for OpenCode sessions working in this repo.

## Stack

- Kotlin 2.2.0, Spring Boot 4.1.0, Java/JDK 17 (builds and tests also pass on JDK 25), Gradle 9.0.0 (Kotlin DSL, wrapper only — no installed Gradle required). JPA uses `jakarta.persistence` (Spring Boot 3).
- Single-module project. Package root: `com.tngtech.chsystem`. Entry point: `ChSystemApplication.kt`.
- Persistence: Spring Data JPA + Hibernate over H2. `allOpen` is applied to JPA entities/embeddables/mapped superclasses (they must not be final).
- No separate lint/formatter/typecheck tasks configured. `kotlin.code.style=official` is the only style signal — follow it and match surrounding files.

## Build & run

The Spring profile is selected via the Gradle `-Pprofile=<name>` property, NOT a standard Spring argument. `build.gradle.kts` maps `profile` → `spring.profiles.active` for `bootRun`; the jar must be launched with `-Dspring.profiles.active=...` directly.

- Build: `./gradlew -Pprofile=dev build`
- Run app: `./gradlew -Pprofile=dev bootRun`
- Run built jar: `java -jar -Dspring.profiles.active=dev build/libs/chsystem-1.0-SNAPSHOT.jar`
- Docker: `./gradlew -Pprofile=prod build` then `docker build . -t chsystem:1.0`; run with `--spring.profiles.active=docker`.

Profiles and their DBs (all H2, `sa`/`password`):
- `dev` — file `~/dbs/dev_chsystem.h2` (created on demand; needs a writable `~/dbs`).
- `prod` — in-memory `jdbc:h2:mem:testdb` (data lost on restart; do not rely on persistence here).
- `docker` — file `/dbs/chsystem.h2` (bind-mount `/dbs` to persist, see `docker-compose.yml`).

`ddl-auto: update` — schema is mutated on startup; entity changes apply automatically without migrations.

## Tests

- JUnit 5 only (`useJUnitPlatform()`). JUnit 4/vintage and Mockito are explicitly excluded — use JUnit Jupiter APIs and MockK. `springmockk` (`com.ninja-squad:springmockk`) is provided for `@MockBean`/`@SpyBean` equivalents.
- Test classes follow a naming convention that reflects their scope — keep it when adding tests:
  - `*UnitTest` — pure unit tests (no Spring context).
  - `*ComponentTest` — `@DataJpaTest`-style slice tests.
  - `*IntegrationTest` — `@SpringBootTest` full-context tests (e.g. `PlayerControllerIntegrationTest`).
- Run all tests: `./gradlew test`
- Run a single test class: `./gradlew test --tests "com.tngtech.chsystem.controller.PlayerControllerIntegrationTest"`
- Run by pattern: `./gradlew test --tests "*PairingService*"`

## Layout notes

- `entities/` = JPA `@Entity` (DB shape), `model/` = request/response DTOs returned by controllers, `dto/` = internal service-layer value objects. Don't mix these layers.
- Controllers map between `entities` and `model` via extension functions defined in the controller files (e.g. `PlayerEntity.toPlayerModel()`).
- `service/score`, `service/match`, `service/matchmaking`, `service/rank` are the domain cores (Swiss-style tournament ranking/scoring/pairing). Prefer understanding the existing scoring services before editing ranking logic.
- CORS is globally open (`WebConfig`) for `/**` with GET/POST/PUT/DELETE — relevant if adding endpoints or security.

## Multi-repo note

This is the backend. `docker-compose.yml` references a sibling frontend image built from `../chsystem-frontend`; that repo is not part of this checkout. Frontend changes are out of scope here.
