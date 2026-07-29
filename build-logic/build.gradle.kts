plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation(libs.spotless.plugin)
    implementation(libs.errorprone.plugin)
    implementation(libs.nullaway.plugin)
}
