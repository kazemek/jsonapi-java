# Phase 0.3 — CI Quality and Supply Chain

> **Scope:** Build tooling, CI, and agent completion  
> **Dependencies:** Phase 0.1  
> **Status:** Complete

## Goal

Give every change a shared merge bar: coverage reports, SonarCloud Quality Gate, supply-chain verification, and visible CI test/verification artifacts—without requiring a Sonar token for ordinary local builds.

## Deliverables

- Enable JaCoCo (XML and HTML) in the `jsonapi-java-library` convention plugin, with reports generated after tests.
- Apply the SonarScanner for Gradle plugin at the root with SonarCloud project settings and `sonar.qualitygate.wait=true`.
- Update GitHub Actions to run build, coverage, and Sonar with full git history, SHA-pinned actions, and required secrets.
- Bootstrap and maintain `gradle/verification-metadata.xml` (SHA-256, `verify-metadata=true`) covering the CI classpath, including transitive BOMs such as `kotlinx-coroutines-bom`.
- Configure Renovate with `helpers:pinGitHubActionDigests` so Action digests and verification metadata stay maintainable.
- Add a project `sonar-quality-gate` skill and require it in the agent completion lifecycle.
- Upload dependency-verification, test, and JaCoCo reports as workflow artifacts; publish JUnit results via `dorny/test-reporter`.
- Document conventions, verification regeneration, and CI reporting in `AGENTS.md`.

## Non-goals

- Aggregated JaCoCo reports across modules.
- Analyzing `build-logic` as a Sonar module.
- PGP signature verification / keyring maintenance.
- Separate `gradle.lockfile` dependency locking.
- Personal (`~/.cursor/skills/`) copy of the Sonar skill.
- Polling SonarCloud REST APIs outside the Gradle scanner wait (except defensive
  zero-new-issues checks documented in the Sonar skill).
- Loosening SonarCloud Quality Gate conditions without an explicit request.

## Follow-up (post-completion)

The project later associated a custom Quality Gate that keeps Sonar way conditions and
additionally fails when `new_violations` > 0, so code smells and other issues block CI
even when maintainability rating remains A.

## Acceptance criteria

- [x] `./gradlew clean build` succeeds without `SONAR_TOKEN`.
- [x] `./gradlew jacocoTestReport` produces XML under each library module at `build/reports/jacoco/test/jacocoTestReport.xml`.
- [x] Root Sonar configuration targets SonarCloud project `kazemek_jsonapi-java` in organization `kazemek` with Quality Gate wait enabled.
- [x] CI checks out with `fetch-depth: 0` and runs `./gradlew clean build jacocoTestReport sonar` with `GITHUB_TOKEN` and `SONAR_TOKEN`.
- [x] CI `uses:` lines are 40-character commit SHAs with `# vX.Y.Z` comments.
- [x] `gradle/verification-metadata.xml` exists with `verify-metadata=true`, SHA-256 entries, and CI-required artifacts such as `kotlinx-coroutines-bom`.
- [x] `renovate.json` extends `helpers:pinGitHubActionDigests`.
- [x] `.cursor/skills/sonar-quality-gate/SKILL.md` exists; `AGENTS.md` requires it before declaring work complete.
- [x] CI uploads a `gradle-reports` artifact (verification, tests, JaCoCo, test-results) and publishes a Unit tests check when JUnit XML exists.
- [x] `AGENTS.md` documents JaCoCo, Sonar CI, verification regeneration, and report artifacts.
