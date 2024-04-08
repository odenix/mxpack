@file:Suppress("UnstableApiUsage")
import dev.adamko.dokkatoo.dokka.parameters.VisibilityModifier

plugins {
  `java-library`
  alias(libs.plugins.dokkaHtml)
  alias(libs.plugins.jmh)
  alias(libs.plugins.spotless)
}

java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(21)
    vendor = JvmVendorSpec.ORACLE
  }
  consistentResolution {
    useCompileClasspathVersions()
  }
}

tasks.withType<JavaCompile>().configureEach {
  options.release = 17
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

jmh {
  includes.add("ReadString")
  fork = 1
  warmupIterations = 3
  iterations = 5
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
  jmh(libs.jqwik)
  jmh(libs.messagePack)
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
  moduleName.set("minipack")
  pluginsConfiguration.html {
    footerMessage = "Copyright 2024 The minipack project authors"
  }
  dokkatooSourceSets {
    register("main") {
      jdkVersion = 17 // link to JDK 17 docs
      documentedVisibilities(VisibilityModifier.PUBLIC, VisibilityModifier.PROTECTED)
      sourceRoots = fileTree("src/main/java/org/minipack") {
        include("core/*.java")
      }
    }
  }
}
