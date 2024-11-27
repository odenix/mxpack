@file:Suppress("UnstableApiUsage")
import dev.adamko.dokkatoo.dokka.parameters.VisibilityModifier
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  `java-library`
  kotlin("jvm") version(libs.versions.kotlin)
  alias(libs.plugins.dokkaHtml)
  alias(libs.plugins.mavenPublish)
  alias(libs.plugins.spotless)
}

val jdkVersion = 23
val javaVersion = 17

dependencies {
  implementation(project(":minipack-core"))
  testImplementation(libs.assertj)
  testImplementation(libs.jqwikApi)
  testImplementation(libs.jqwikTime)
  testImplementation(libs.junitJupiter)
  testRuntimeOnly(libs.jqwikEngine)
  testRuntimeOnly(libs.junitLauncher)
}

java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(jdkVersion)
    vendor = JvmVendorSpec.ORACLE
  }
  consistentResolution {
    useCompileClasspathVersions()
  }
}

tasks.withType<JavaCompile>().configureEach {
  options.release = javaVersion
}

kotlin {
  compilerOptions {
    jvmTarget = JvmTarget.fromTarget(javaVersion.toString())
  }
}

tasks.test {
  useJUnitPlatform()
  systemProperty("jqwik.database", "build/jqwik.bin")
}

spotless {
  kotlin {
    licenseHeaderFile(file("../gradle/spotless/license.java"))
  }
  format("kotlinCode", com.diffplug.gradle.spotless.KotlinExtension::class.java) {
    targetExclude("src/test/kotlin/io/github/odenix/minipack/kotlin/example/**")
    ktfmt(libs.versions.ktfmt.get()).googleStyle()
  }
}

configurations.matching {
  "dokka" !in it.name.lowercase()
}.configureEach {
  resolutionStrategy {
    //failOnVersionConflict()
    failOnNonReproducibleResolution()
  }
}

// For configuration options see:
// https://adamko-dev.github.io/dokkatoo/kdoc/modules/dokkatoo-plugin/dev.adamko.dokkatoo/-dokkatoo-extension/index.html
dokkatoo {
  moduleName = "io.github.odenix.minipack.kotlin"
  pluginsConfiguration.html {
    footerMessage = "Copyright 2024 the MiniPack contributors"

  }
  dokkatooSourceSets {
    named("main") {
      jdkVersion = javaVersion // link to JDK docs
      documentedVisibilities(VisibilityModifier.PUBLIC, VisibilityModifier.PROTECTED)
      includes.from("src/main/dokka/minipack-kotlin.md")
      sourceRoots = fileTree("src/main/kotlin") {
        include("io/github/odenix/minipack/kotlin/**")
      }
    }
  }
}

mavenPublishing {
  pom {
    name = "MiniPack for Kotlin"
    description = "Kotlin integration for MiniPack, a Java library for reading and writing the MessagePack serialization format."
    url = "https://github.com/odenix/minipack"
    licenses {
      license {
        name = "The Apache License, Version 2.0"
        url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
      }
    }
    developers {
      developer {
        id = "odenix"
        name = "odenix"
        email = "119817707+odenix@users.noreply.github.com"
      }
    }
    scm {
      connection = "scm:git:https://github.com/odenix/minipack.git"
      developerConnection = "scm:git:ssh://git@github.com/odenix/minipack.git"
      url = "https://github.com/odenix/minipack"
    }
  }
}
