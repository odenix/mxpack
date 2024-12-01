import me.champeau.jmh.JMHTask

plugins {
  java
  alias(libs.plugins.jmh)
  alias(libs.plugins.spotless)
}

val jdkVersion = 23

java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(jdkVersion)
    vendor = JvmVendorSpec.ORACLE
  }
}

dependencies {
  implementation(libs.jspecify)
  implementation(project(":mxpack-core"))
  jmh(libs.jqwikApi)
  jmh(libs.jqwikTime)
  jmh(libs.jqwikEngine)
  jmh(libs.messagePack)
}

spotless {
  java {
    // https://github.com/google/google-java-format/issues/1193
    // googleJavaFormat(libs.versions.googleJavaFormat.get()).reflowLongStrings()
    licenseHeaderFile(file("../gradle/spotless/license.java"))
  }
}

jmh {
  includes.add("WriteLong.run")
  fork = 1
  warmupIterations = 3
  iterations = 5
}

tasks.withType<JMHTask>().configureEach {
  outputs.upToDateWhen { false }
}
