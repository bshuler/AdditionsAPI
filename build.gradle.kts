import org.gradle.jvm.toolchain.JavaLanguageVersion

plugins {
    java
    id("com.gradleup.shadow") version "9.6.1"
}

group = "com.chrismin13"
version = providers.gradleProperty("pluginVersion").get()

// Java 25: the latest resolvable paper-api (26.2.build.111-stable, matching
// Minecraft's current calendar-versioned release) publishes Gradle module
// metadata requiring JVM 25 (same finding as the EpicFurnaces/InvUnload
// siblings). The foojay-resolver-convention plugin (declared in
// settings.gradle.kts) auto-provisions this JDK into Gradle's own toolchain
// cache; it does not touch the system/Homebrew JDK. See PLAN.md/CLAUDE.md.
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:${providers.gradleProperty("paperApiVersion").get()}")

    // NBT/attribute reflection helper (com.comphenix.attribute.NbtFactory) and
    // the ArmorEquip event bridge only import stable org.bukkit.* types plus
    // java.lang.reflect - no third-party runtime dependency needed for those.

    compileOnly("com.google.code.findbugs:jsr305:3.0.2")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(25)
    // This is a 2015-era codebase carrying its own vendored reflection/NMS
    // helpers (com.comphenix.attribute, us.fihgu.toolbox.reflection) and a
    // large amount of pre-1.13 "flattening" Material-handling code that
    // intentionally still references now-deprecated LEGACY_* constants and
    // numeric-durability APIs for older-version compatibility. Do not fail
    // the build on deprecation/unchecked/rawtypes warnings - see CLAUDE.md.
    options.compilerArgs.addAll(listOf("-Xlint:none"))
}

tasks.shadowJar {
    archiveClassifier.set("")
    archiveBaseName.set("AdditionsAPI")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

tasks.processResources {
    val props = mapOf("version" to project.version)
    filesMatching("plugin.yml") {
        expand(props)
    }
}

sourceSets {
    main {
        java {
            // legacy-hooks/ holds dead/defunct integrations kept for reference
            // only (currently none under src/, they live in the top-level
            // legacy-hooks/ directory which is not part of any source set).
        }
    }
}
