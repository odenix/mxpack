@file:Suppress("UnstableApiUsage")
import dev.adamko.dokkatoo.dokka.parameters.VisibilityModifier
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.net.URI

plugins {
  `java-library`
  `maven-publish`
  signing
  kotlin("jvm") version(libs.versions.kotlin)
  alias(libs.plugins.dokkaHtml)
  alias(libs.plugins.spotless)
}

val jdkVersion = 23
val javaVersion = 17

dependencies {
  implementation(project(":mxpack-core"))
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
  withJavadocJar()
  withSourcesJar()
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
    targetExclude("src/test/kotlin/org/odenix/mxpack/kotlin/example/**")
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
  moduleName = "org.odenix.mxpack.kotlin"
  pluginsConfiguration.html {
    val outputDir = System.getProperty("dokkaDir")
    if (outputDir != null) {
      dokkatooPublications {
        html {
          outputDirectory = rootDir.resolve(outputDir)
        }
      }
    }
    footerMessage = "Copyright &copy; 2024 the MxPack project authors"
  }
  dokkatooSourceSets {
    named("main") {
      jdkVersion = javaVersion // link to JDK docs
      documentedVisibilities(VisibilityModifier.PUBLIC, VisibilityModifier.PROTECTED)
      includes.from("src/main/dokka/mxpack-kotlin.md")
      sourceRoots = fileTree("src/main/kotlin") {
        include("org/odenix/mxpack/kotlin/**")
      }
    }
  }
}

// https://docs.gradle.org/current/userguide/publishing_maven.html
// https://central.sonatype.org/publish-ea/publish-ea-guide/
publishing {
  repositories {
    maven {
      name = "file"
      url = file("build/m2").toURI()
    }
  }
  publications {
    create<MavenPublication>("maven") {
      from(components["java"])
      pom {
        name = "mxpack-kotlin"
        description = "Kotlin integration for MxPack, a modern Java library for reading and writing the MessagePack serialization format."
        url = "https://github.com/odenix/mxpack"
        licenses {
          license {
            name = "The Apache License, Version 2.0"
            url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
          }
        }
        developers {
          developer {
            name = "MxPack project authors"
            email = "mxpack@odenix.org"
          }
        }
        scm {
          connection = "scm:git:git://github.com/odenix/mxpack.git"
          developerConnection = "scm:git:ssh://github.com/odenix/mxpack.git"
          url = "https://github.com/odenix/mxpack/tree/master"
        }
      }
    }
  }
  repositories {
    maven {
      url = project.layout.buildDirectory.dir("maven").get().asFile.toURI()
    }
  }
}

// https://docs.gradle.org/current/userguide/signing_plugin.html
signing {
  val signingKey = System.getenv("SIGNING_KEY")
  if (signingKey != null) {
    val passphrase = System.getenv("PASSPHRASE")!!
    useInMemoryPgpKeys(signingKey, passphrase)
    sign(publishing.publications["maven"])
  }
}
