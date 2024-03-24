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
    licenseHeaderFile(file("gradle/spotless/license.java"))
  }
}

dependencies {
  api(libs.jSpecify)
  testImplementation(libs.junitApi)
  testImplementation(libs.assertJ)
  testImplementation(libs.jqwik)
  testImplementation(libs.messagePack)
  testRuntimeOnly(libs.junitLauncher)
  dokkatooPluginHtml(libs.dokkaJava)
}

configurations.all {
  if (!name.contains("dokka")) {
    resolutionStrategy {
      failOnVersionConflict()
      failOnNonReproducibleResolution()
    }
  }
}

dokkatoo {
  moduleName.set("minipack")
  pluginsConfiguration.html {
    footerMessage.set("Copyright 2024 The minipack project authors")
  }
  dokkatooSourceSets {
    register("main") {
      jdkVersion.set(17) // link to JDK 17 docs
      documentedVisibilities(VisibilityModifier.PUBLIC, VisibilityModifier.PROTECTED)
      sourceRoots = fileTree("src/main/java/org/translatenix/minipack") {
        include("*.java")
      }
    }
  }
}
