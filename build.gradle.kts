plugins {
    kotlin("jvm") version "1.9.22"
    id("io.ktor.plugin") version "2.3.8"
    kotlin("plugin.serialization") version "1.9.22"
}

group = "com.aivibes"
version = "0.0.1"

application {
    mainClass.set("com.aivibes.ApplicationKt")
}

repositories {
    mavenCentral()
}

dependencies {
    // Ktor server dependencies
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("io.ktor:ktor-server-content-negotiation-jvm")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm")
    
    // HTML and CSS templating
    implementation("io.ktor:ktor-server-html-builder-jvm")
    
    // Database
    implementation("org.jetbrains.exposed:exposed-core:0.45.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.45.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.45.0")
    implementation("com.h2database:h2:2.2.224")
    
    // Logging
    implementation("ch.qos.logback:logback-classic:1.4.14")
    
    // Testing
    testImplementation("io.ktor:ktor-server-test-host-jvm")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:1.9.22")
} 