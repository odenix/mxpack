plugins {
  java
  alias(libs.plugins.jmh)
}

dependencies {
  implementation(libs.jSpecify)
  implementation(project(":core"))
  jmh(libs.jqwik)
  jmh(libs.messagePack)
}

jmh {
  includes.add("ReadType")
  fork = 1
  warmupIterations = 3
  iterations = 5
}
