import com.google.devtools.ksp.gradle.KspTask
import java.nio.file.Files
import java.util.*

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.devtools.ksp)
    id("maven-publish")
    id("com.gradleup.shadow") version "9.0.0-beta13"
    id("com.github.ben-manes.versions") version "0.52.0"
}

group = "io.github.toberocat.improved-factions"
version = "2.3.0"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://jitpack.io")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://repo.mikeprimm.com/")
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    implementation(project(":shared"))

    // Spigot API
    compileOnly(libs.spigot.api)

    // Exposed ORM
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.kotlin.datetime)

    // Jackson
    implementation(libs.jackson.core)
    implementation(libs.jackson.databind)
    implementation(libs.jackson.module.kotlin)

    // Other dependencies
    implementation(libs.kotlin.stdlib.jdk8)
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlinx.datetime.jvm)
    implementation(libs.toberocore)
    implementation(libs.sqlite.jdbc)
    implementation(libs.h2)
    compileOnly(libs.guiengine) {
        exclude(group = "com.jeff_media", module = "SpigotUpdateChecker")
    }
    implementation(libs.adventure.text.minimessage)
    implementation(libs.adventure.text.serializer.legacy)
    implementation(libs.kyori.adventure.platform.bukkit)
    implementation(libs.bstats.bukkit)

    // Provided dependencies
    compileOnly(libs.placeholderapi)
    compileOnly(libs.dynmap.api)

    // Test dependencies
    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.junit.jupiter.params)
    testImplementation(libs.mockbukkit)
    testImplementation(libs.snakeyaml)
    testImplementation(libs.gson)
    testImplementation(libs.logback.classic)

    // KSP
    ksp(project(":code-generation"))
}

tasks.named<Copy>("processResources") {
    filesMatching("plugin.yml") {
        expand("version" to project.version)
    }
}

tasks.withType<KspTask>().configureEach {
    dependsOn(generateBuildConfig)
}

tasks.shadowJar {
    archiveFileName.set("${project.name}-${project.version}.jar")
    relocate("com.fasterxml.jackson", "io.github.toberocat.relocated.jackson")
    relocate("net.kyori", "io.github.toberocat.relocated.kyori")
    relocate("dev.s7a", "io.github.toberocat.relocated.base64itemstack")
    relocate("org.bstats", "io.github.toberocat.relocated.bstats")
    
    exclude("META-INF/LICENSE*")
    exclude("META-INF/NOTICE*")

    mergeServiceFiles()
}

tasks {
    build {
        dependsOn(shadowJar)
    }

    compileKotlin {
        dependsOn(generateBuildConfig)
    }

    processResources {
        filesMatching("**/*.yml") {
            expand("version" to project.version)
        }
    }

    test {
        useJUnitPlatform()
    }
}

kotlin {
    jvmToolchain(21)

    sourceSets.main {
        kotlin.srcDir("build/generated/ksp/main/kotlin")
        kotlin.srcDir("build/generated/source/buildConfig/kotlin")
    }
    sourceSets.test {
        kotlin.srcDir("build/generated/ksp/test/kotlin")
    }
}

ksp {
    arg("languageFolder", "$projectDir/src/main/resources/languages")
    arg("plugin.yml", "$projectDir/src/main/resources/plugin.yml")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}

val generateBuildConfig by tasks.registering {
    val outputDir = layout.buildDirectory.dir("generated/source/buildConfig/kotlin")
    val outputFile = outputDir.map { it.file("BuildConfig.kt") }

    outputs.file(outputFile)

    doLast {
        val dir = outputDir.get().asFile
        try {
            if (!dir.exists()) {
                dir.mkdirs()
                if (!dir.exists()) {
                    Files.createDirectories(dir.toPath())
                }
            }

            logger.info("BuildConfig directory created at: ${dir.absolutePath}, exists: ${dir.exists()}")

            val file = outputFile.get().asFile
            file.writeText(
                """
                object BuildConfig {
                    const val VERSION = "${project.version}"
                }
                """.trimIndent()
            )
            logger.info("BuildConfig.kt generated successfully at: ${file.absolutePath}")
        } catch (e: Exception) {
            logger.error("Failed to generate BuildConfig.kt: ${e.message}")
            throw e
        }
    }
}