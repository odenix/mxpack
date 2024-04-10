import me.champeau.jmh.JMHTask

plugins {
  java
  alias(libs.plugins.jmh)
  alias(libs.plugins.spotless)
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
  includes.add("WriteTimestamp.run")
  fork = 1
  warmupIterations = 3
  iterations = 5
}

tasks.withType<JMHTask>().configureEach {
  outputs.upToDateWhen { false }
}
