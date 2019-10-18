import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.50"
}

group = "com.tngtech"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk11"))
    implementation(kotlin("gradle-plugin", version = "1.3.50"))
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}