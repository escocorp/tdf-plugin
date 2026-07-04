plugins {
    java
    kotlin("jvm") version "2.4.0"
    kotlin("plugin.serialization") version "2.4.0"
}

version = "1.0"

val javaVersion = 25

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(javaVersion))
}

kotlin {
    jvmToolchain(javaVersion)
}

sourceSets.main {
    java.srcDirs("src")
}

repositories {
    mavenCentral()

    ivy {
        url = uri("https://github.com/escocorp/esco-plugin-v2/releases/download")
        patternLayout {
            artifact("[revision]/[module].[ext]")
        }
        metadataSources {
            artifact()
        }
    }

    ivy {
        url = uri("https://github.com/")
        patternLayout {
            artifact("/[organisation]/[module]/releases/download/[revision]/dependencies.jar")
        }
        metadataSources {
            artifact()
        }
    }

    ivy {
        url = uri("https://github.com/")
        patternLayout {
            artifact("/[organisation]/[module]/releases/download/master/[revision].jar")
        }
        metadataSources {
            artifact()
        }
    }
}

val mindustryVersion = "v158.1"
val useLatest = false

dependencies {
    compileOnly("esco:esco-plugin-v2:v1")

    compileOnly(
        if (useLatest)
            "Anuken:MindustryBuilds:latest"
        else
            "Anuken:Mindustry:$mindustryVersion"
    )
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.fromTarget(javaVersion.toString()))
    }
}

val versionGitDir = layout.buildDirectory.dir("version-git")

val writeVersionFile by tasks.registering {
    val gitDir = file("${rootDir}/.git")

    if (gitDir.exists()) {
        inputs.file("${gitDir}/HEAD")

        val packed = file("${gitDir}/packed-refs")
        if (packed.exists()) {
            inputs.file(packed)
        }

        val refs = file("${gitDir}/refs")
        if (refs.exists()) {
            inputs.dir(refs)
        }
    }

    outputs.dir(versionGitDir)

    doLast {
        val dir = versionGitDir.get().asFile
        dir.mkdirs()

        val versionFile = dir.resolve("version")

        val proc = ProcessBuilder(
            "git",
            "rev-parse",
            "--short",
            "HEAD"
        )
            .directory(rootDir)
            .start()

        proc.waitFor()

        versionFile.writeText(
            if (proc.exitValue() == 0)
                proc.inputStream.bufferedReader().readText().trim()
            else
                "unknown"
        )
    }
}

tasks.jar {
    dependsOn(writeVersionFile)

    archiveFileName.set("${project.name}.jar")

    from({
        configurations.runtimeClasspath.get()
            .map { if (it.isDirectory) it else zipTree(it) }
    })

    from(rootDir) {
        include("plugin.json")
    }

    from(versionGitDir) {
        include("version")
    }

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
