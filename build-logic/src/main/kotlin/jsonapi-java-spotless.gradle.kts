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
        greclipse().configFile("config/spotless/greclipse.properties")
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
