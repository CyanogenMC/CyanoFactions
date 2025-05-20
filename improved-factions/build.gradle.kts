import java.nio.file.Files
import java.util.*

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.devtools.ksp)
    id("maven-publish")
    id("com.gradleup.shadow") version "9.0.0-beta13"
    id("com.github.ben-manes.versions") version "0.52.0"
}

val versionPropsFile = file("version.properties")
val versionProps = Properties()

if (versionPropsFile.exists()) {
    versionProps.load(versionPropsFile.inputStream())
}

val buildIncrement = (versionProps["buildIncrement"]?.toString()?.toInt() ?: 1) + 1
val versionName = versionProps["versionName"]!!.toString()

versionProps["buildIncrement"] = buildIncrement.toString()
versionProps["versionName"] = versionName

versionProps.store(versionPropsFile.outputStream(), null)


group = "io.github.toberocat.improved-factions"
version = versionName

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://jitpack.io")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://repo.jeff-media.com/public")
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
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinx.datetime.jvm)
    implementation(libs.toberocore)
    implementation(libs.sqlite.jdbc)
    implementation(libs.h2)
    implementation(libs.spigot.update.checker)
    compileOnly(libs.guiengine)
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

tasks.shadowJar {
    archiveFileName.set("${project.name}-${project.version}.jar")
    if (System.getenv("CI") == null) {
        destinationDirectory.set(file("../server/plugins"))
    }
    relocate("com.fasterxml.jackson", "io.github.toberocat.relocated.jackson")
    relocate("net.kyori", "io.github.toberocat.relocated.kyori")
    relocate("dev.s7a", "io.github.toberocat.relocated.base64itemstack")
    relocate("org.bstats", "io.github.toberocat.relocated.bstats")
    relocate("com.jeff_media.updatechecker", "io.github.toberocat.relocated.updatechecker")
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
            expand("buildIncrement" to buildIncrement)
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

val generateBuildConfig: Task by tasks.creating {
    val outputDir = layout.buildDirectory.dir("generated/source/buildConfig/kotlin").get()
    Files.createDirectories(outputDir.asFile.toPath())

    val versionName = version.toString()

    doLast {
        val buildConfigFile = outputDir.file("BuildConfig.kt")
        buildConfigFile.asFile.writeText(
            """
            object BuildConfig {
                const val VERSION_NAME = "$versionName"
                const val BUILD_INCREMENT = $buildIncrement
                const val VERSION = "$versionName.$buildIncrement"
            }
        """.trimIndent()
        )
    }
}
