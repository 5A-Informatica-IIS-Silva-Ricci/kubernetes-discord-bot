import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.10"
}

group = "dev.giuliopime.kubernetes-discord-bot"
version = "1.0"

repositories {
    mavenCentral()
    maven {
        name = "m2-dv8tion"
        url = uri("https://m2.dv8tion.net/releases")
    }
    maven("https://jitpack.io/")
}

dependencies {
    // JDA
    implementation("net.dv8tion:JDA:4.3.0_335") {
        exclude(module = "opus-java")
    }

    // JDA Utilities for Kotlin
    implementation("com.github.minndevelopment:jda-ktx:1a45395")

    // File .env per impostazioni e chiavi private
    implementation("io.github.cdimascio:dotenv-kotlin:6.2.2")

    // Logger
    implementation("io.github.microutils:kotlin-logging-jvm:2.0.11")
    implementation("org.slf4j:slf4j-api:1.7.32")
    implementation("ch.qos.logback:logback-classic:1.2.5")
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}
