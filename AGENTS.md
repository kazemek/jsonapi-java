# Build & Test

    ./gradlew clean build

Requires JDK 21 (enforced via Gradle toolchain).

# Project structure

Multi-module Gradle build (Kotlin DSL, Gradle 9.6.1). Submodules are listed in `settings.gradle.kts`.
Currently: `jsonapi-java-core`.

Source lives under `<module>/src/`. Tests use Groovy + Spock (`<module>/src/test/groovy/`).

# Build logic

Shared build configuration lives in `build-logic/` as a precompiled script plugin.
The convention plugin is `jsonapi-java-library` (applied via `id("jsonapi-java-library")`).

It provides: `java-library` + `groovy` plugins, JDK 21 toolchain, Spock/Groovy/ByteBuddy
test dependencies, and JUnit Platform configuration.

New submodules only need:

```kotlin
plugins {
    id("jsonapi-java-library")
}
```

# CI

GitHub Actions runs `./gradlew clean build` on push to `main` and on PRs.
