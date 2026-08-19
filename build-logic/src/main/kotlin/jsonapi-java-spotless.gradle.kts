plugins {
    id("com.diffplug.spotless")
}

spotless {
    java {
        target("**/*.java")
        targetExclude("**/build/**", "**/bin/**")
        googleJavaFormat()
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
    }
    groovy {
        target("**/*.groovy")
        targetExclude("**/build/**", "**/bin/**")
        // Pin Groovy-Eclipse 4.35 (Groovy 5.0.0): newer bundled formatters (4.38+ / Groovy 6)
        // collapse the column alignment in Spock where: data tables.
        greclipse("4.35").configFile("config/spotless/greclipse.properties")
        trimTrailingWhitespace()
        endWithNewline()
    }
    kotlin {
        target("**/*.kt")
        targetExclude("**/build/**", "**/bin/**")
        ktlint()
        trimTrailingWhitespace()
        endWithNewline()
    }
    kotlinGradle {
        target("**/*.gradle.kts")
        targetExclude("**/build/**", "**/bin/**")
        ktlint()
        trimTrailingWhitespace()
        endWithNewline()
    }
}
