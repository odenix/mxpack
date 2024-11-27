@file:Suppress("UnstableApiUsage")

import com.vanniktech.maven.publish.SonatypeHost

plugins {
  `java-library`
  alias(libs.plugins.spotless)
  alias(libs.plugins.mavenPublish)
}

val jdkVersion = 23
val javaVersion = 17

dependencies {
  api(libs.jspecify)
  testImplementation(libs.assertj)
  testImplementation(libs.jqwikApi)
  testImplementation(libs.jqwikTime)
  testImplementation(libs.junitJupiter)
  testImplementation(libs.messagePack)
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
  withSourcesJar()
  withJavadocJar()
}

tasks.withType<JavaCompile>().configureEach {
  options.release = javaVersion
}

tasks.compileTestJava {
  // for jqwik diagnostic messages
  options.compilerArgs.add("-parameters")
}

tasks.test {
  useJUnitPlatform()
  systemProperty("jqwik.database", "build/jqwik.bin")
}

tasks.javadoc {
  with (options as StandardJavadocDocletOptions) {
    docTitle("MiniPack Core")
    encoding("UTF-8")
    memberLevel = JavadocMemberLevel.PROTECTED
    links("https://jspecify.dev/docs/api/")
    // https://github.com/gradle/gradle/issues/19726
    val sourceSetDirectories = sourceSets.main.get().java.sourceDirectories.joinToString(":")
    addStringOption("-source-path", sourceSetDirectories)
  }
  exclude("org/minipack/java/internal/**")
}

spotless {
  java {
    licenseHeaderFile(file("../gradle/spotless/license.java"))
  }
  format("javaCode", com.diffplug.gradle.spotless.JavaExtension::class.java) {
    targetExclude("src/test/java/org/minipack/java/example/**")
    // https://github.com/google/google-java-format/issues/1193
    // googleJavaFormat(libs.versions.googleJavaFormat.get()).reflowLongStrings()
  }
}

// https://vanniktech.github.io/gradle-maven-publish-plugin/central/#configuring-maven-central
mavenPublishing {
  publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

  //coordinates("org.minipack", "minipack-core")

  pom {
    name = "MiniPack Core"
    description = "A modern, efficient, and extensible Java library for reading and writing the MessagePack serialization format."
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
