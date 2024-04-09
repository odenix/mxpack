plugins {
  `java-library`
}

dependencies {
  api(libs.jSpecify)
  implementation(project(":core"))
}