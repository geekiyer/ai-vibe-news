plugins {
    kotlin("jvm") version "1.9.0"
    kotlin("plugin.serialization") version "1.9.0"
    id("io.ktor.plugin") version "2.3.0"
}

group = "com.aivibes"
version = "0.0.1"

application {
    mainClass.set("com.aivibes.ApplicationKt")
}

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    // Ktor server dependencies
    implementation("io.ktor:ktor-server-core-jvm:2.3.0")
    implementation("io.ktor:ktor-server-netty-jvm:2.3.0")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:2.3.0")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:2.3.0")
    
    // Ktor client dependencies
    implementation("io.ktor:ktor-client-core:2.3.0")
    implementation("io.ktor:ktor-client-cio:2.3.0")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.0")
    
    // HTML and CSS templating
    implementation("io.ktor:ktor-server-html-builder-jvm:2.3.0")
    
    // Database
    implementation("org.jetbrains.exposed:exposed-core:0.41.1")
    implementation("org.jetbrains.exposed:exposed-dao:0.41.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.41.1")
    implementation("org.postgresql:postgresql:42.7.2")
    implementation("com.h2database:h2:+")
    
    // Logging
    implementation("ch.qos.logback:logback-classic:1.5.13")
    
    // Testing
    testImplementation("io.ktor:ktor-server-tests-jvm:2.3.0")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:1.9.0")
} 