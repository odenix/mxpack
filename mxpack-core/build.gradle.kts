@file:Suppress("UnstableApiUsage")

plugins {
  `java-library`
  `maven-publish`
  signing
  alias(libs.plugins.spotless)
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
}

tasks.javadoc {
  val outputDir = System.getProperty("javadocDir")
  if (outputDir != null) {
    setDestinationDir(rootDir.resolve(outputDir))
  }
  with (options as StandardJavadocDocletOptions) {
    docTitle("MxPack Core")
    encoding("UTF-8")
    memberLevel = JavadocMemberLevel.PROTECTED
    links("https://jspecify.dev/docs/api/")
    // https://github.com/gradle/gradle/issues/19726
    val sourceSetDirectories = sourceSets.main.get().java.sourceDirectories.joinToString(":")
    addStringOption("-source-path", sourceSetDirectories)
  }
  exclude("org/odenix/mxpack/core/internal/**")
}

spotless {
  java {
    licenseHeaderFile(file("../gradle/spotless/license.java"))
  }
  format("javaCode", com.diffplug.gradle.spotless.JavaExtension::class.java) {
    targetExclude("src/test/java/org/odenix/mxpack/java/example/**")
    // https://github.com/google/google-java-format/issues/1193
    // googleJavaFormat(libs.versions.googleJavaFormat.get()).reflowLongStrings()
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
        name = "mxpack-core"
        description = "A modern Java library for reading and writing the MessagePack serialization format."
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