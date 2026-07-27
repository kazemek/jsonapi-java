plugins {
    id("com.diffplug.spotless")
}

spotless {
    java {
        target("**/*.java")
        targetExclude("**/build/**")
        googleJavaFormat()
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
    }
    groovy {
        target("**/*.groovy")
        targetExclude("**/build/**")
        greclipse().configFile("config/spotless/greclipse.properties")
        trimTrailingWhitespace()
        endWithNewline()
    }
    kotlin {
        target("**/*.kt")
        targetExclude("**/build/**")
        ktlint()
        trimTrailingWhitespace()
        endWithNewline()
    }
    kotlinGradle {
        target("**/*.gradle.kts")
        targetExclude("**/build/**")
        ktlint()
        trimTrailingWhitespace()
        endWithNewline()
    }
}
