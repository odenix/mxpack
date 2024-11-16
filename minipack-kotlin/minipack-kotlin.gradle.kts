@file:Suppress("UnstableApiUsage")
import dev.adamko.dokkatoo.dokka.parameters.VisibilityModifier
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  `java-library`
  kotlin("jvm") version "2.0.21"
  `maven-publish`
  alias(libs.plugins.dokkaHtml)
  alias(libs.plugins.spotless)
}

val jdkVersion = 23
val minJavaVersion = 17

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
  options.release = minJavaVersion
}

kotlin {
  compilerOptions {
    jvmTarget = JvmTarget.fromTarget(minJavaVersion.toString())
  }
}

tasks.test {
  useJUnitPlatform()
}

spotless {
  java {
    googleJavaFormat().reflowLongStrings()
    licenseHeaderFile(file("../gradle/spotless/license.java"))
  }
}

dependencies {
  implementation(project(":minipack-java"))
  dokkatooPluginHtml(libs.dokkaJava)
  testImplementation(libs.junitJupiter)
  testImplementation(libs.assertJ)
  testImplementation(libs.jqwik)
  testRuntimeOnly(libs.junitLauncher)
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
  moduleName.set("minipack-kotlin")
  pluginsConfiguration.html {
    footerMessage = "Copyright 2024 the MiniPack contributors"
  }
  dokkatooSourceSets {
    named("javaMain") {
      jdkVersion = minJavaVersion // link to JDK docs
      documentedVisibilities(VisibilityModifier.PUBLIC, VisibilityModifier.PROTECTED)
      sourceRoots = fileTree("src/main/kotlin/org/minipack/kotlin") {
        include("*.kt")
      }
    }
  }
}

publishing {
  publications {
    create<MavenPublication>("maven") {
      groupId = "org.minipack"
      artifactId = "minipack-kotlin"
      from(components["java"])
      pom {
        name = "MiniPack for Kotlin"
        description = "A modern Kotlin implementation of the MessagePack serialization format."
        url = "https://github.com/translatenix/minipack"
        licenses {
          license {
            name = "The Apache License, Version 2.0"
            url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
          }
        }
        developers {
          developer {
            id = "translatenix"
            name = "translatenix"
            email = "119817707+translatenix@users.noreply.github.com"
          }
        }
        scm {
          connection = "scm:git:https://github.com/translatenix/minipack.git"
          developerConnection = "scm:git:ssh://git@github.com/translatenix/minipack.git"
          url = "https://github.com/translatenix/minipack"
        }
      }
    }
  }
  repositories {
    mavenLocal()
  }
}
