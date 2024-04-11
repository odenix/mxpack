import me.champeau.jmh.JMHTask

plugins {
  java
  alias(libs.plugins.jmh)
  alias(libs.plugins.spotless)
}

val jdkVersion = 22

java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(jdkVersion)
    vendor = JvmVendorSpec.ORACLE
  }
}

dependencies {
  implementation(libs.jSpecify)
  implementation(project(":core"))
  jmh(libs.jqwik)
  jmh(libs.messagePack)
}

spotless {
  java {
    googleJavaFormat().reflowLongStrings()
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
