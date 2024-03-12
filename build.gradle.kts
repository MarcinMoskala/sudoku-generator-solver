import java.util.*

plugins {
    kotlin("multiplatform") version "1.8.10"
    id("dev.petuska.npm.publish") version "3.0.1"
    java
}

group = "com.marcinmoskala"
val libVersion =  "0.0.1"
version = libVersion

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven")
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
        withJava()
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }
    js(IR) {
        moduleName = "sudoku-generator"
        browser()
        binaries.library()
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting
        val jvmTest by getting
        val jsMain by getting
        val jsTest by getting
    }
    jvmToolchain(11)
}

val properties = Properties().apply {
    rootProject.file("local.properties")
        .takeIf { it.exists() }
        ?.reader()
        ?.let { load(it) }
}

npmPublish {
    packages {
        named("js") {
            packageName.set("sudoku-generator-solver")
            version.set(libVersion)
        }
    }
    registries {
        register("npmjs") {
            uri.set(uri("https://registry.npmjs.org")) //
            authToken.set((properties["npmSecret"] as? String).orEmpty())
        }
    }
}
dependencies {
    implementation(kotlin("stdlib-jdk8"))
}
