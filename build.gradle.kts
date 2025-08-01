import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
plugins {
    id("org.springframework.boot") version "3.5.4"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    kotlin("plugin.jpa") version "1.9.25"

    id("org.jlleitschuh.gradle.ktlint") version "12.3.0"

    id("com.palantir.git-version") version "3.4.0"
}

group = "dev.shiron"
val versionDetails: groovy.lang.Closure<com.palantir.gradle.gitversion.VersionDetails> by extra
val details = versionDetails()
version = details.lastTag
val implementationVersion = "$version-#${details.gitHash.substring(0, 7)}"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

ktlint {
    enableExperimentalRules.set(true)
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.flywaydb:flyway-core")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    implementation("org.hibernate.orm:hibernate-community-dialects")
    runtimeOnly("org.xerial:sqlite-jdbc")

    implementation("net.dv8tion:JDA:5.6.1")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("com.google.code.gson:gson:2.13.1")
}

tasks.withType<KotlinCompile> {
    dependsOn("ktlintFormat")
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    launchScript()
    archiveFileName.set("koto.jar")
}
