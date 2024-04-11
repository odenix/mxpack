@file:Suppress("UnstableApiUsage")
import dev.adamko.dokkatoo.dokka.parameters.VisibilityModifier

plugins {
  `java-library`
  `maven-publish`
  alias(libs.plugins.dokkaHtml)
  alias(libs.plugins.spotless)
}

val javaReleaseVersion = 17
val javaBuildVersion = 21

java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(javaBuildVersion)
    vendor = JvmVendorSpec.ORACLE
  }
  consistentResolution {
    useCompileClasspathVersions()
  }
}

tasks.withType<JavaCompile>().configureEach {
  options.release = javaReleaseVersion
}

tasks.compileTestJava {
  // for jqwik diagnostic messages
  options.compilerArgs.add("-parameters")
}

tasks.test {
  useJUnitPlatform {
    includeEngines("jqwik")
    includeEngines("junit-jupiter")
  }
}

spotless {
  java {
    googleJavaFormat().reflowLongStrings()
    licenseHeaderFile(file("../gradle/spotless/license.java"))
  }
}

dependencies {
  api(libs.jSpecify)
  dokkatooPluginHtml(libs.dokkaJava)
  testImplementation(libs.junitApi)
  testImplementation(libs.assertJ)
  testImplementation(libs.jqwik)
  testImplementation(libs.messagePack)
  testRuntimeOnly(libs.junitLauncher)
}

configurations.matching {
  "dokka" !in it.name.lowercase()
}.configureEach {
  resolutionStrategy {
    failOnVersionConflict()
    failOnNonReproducibleResolution()
  }
}

dokkatoo {
  moduleName.set("MiniPack")
  pluginsConfiguration.html {
    footerMessage = "Copyright 2024 the MiniPack contributors"
  }
  dokkatooSourceSets {
    register("main") {
      jdkVersion = javaReleaseVersion // link to JDK docs
      documentedVisibilities(VisibilityModifier.PUBLIC, VisibilityModifier.PROTECTED)
      sourceRoots = fileTree("src/main/java/org/minipack") {
        include("core/*.java")
      }
    }
  }
}

publishing {
  publications {
    create<MavenPublication>("maven") {
      groupId = "org.minipack"
      artifactId = "minipack-core"
      from(components["java"])
      pom {
        name = "MiniPack"
        description =
          "A modern, small, and efficient Java implementation of the MessagePack binary serialization format."
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


