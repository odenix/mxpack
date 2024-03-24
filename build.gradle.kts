@file:Suppress("UnstableApiUsage")

plugins {
  `java-library`
  alias(libs.plugins.spotless)
  alias(libs.plugins.jmh)
}

java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(17)
  }
  consistentResolution {
    useCompileClasspathVersions()
  }
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
    licenseHeaderFile(file("src/spotless/license.java"))
  }
}

dependencies {
  api(libs.jSpecify)
  testImplementation(libs.junitApi)
  testImplementation(libs.assertJ)
  testImplementation(libs.jqwik)
  testImplementation(libs.messagePack)
  testRuntimeOnly(libs.junitLauncher)
}

configurations.all {
  resolutionStrategy {
    failOnVersionConflict()
    failOnNonReproducibleResolution()
  }
}

repositories {
  mavenCentral()
}
