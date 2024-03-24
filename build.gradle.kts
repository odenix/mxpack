plugins {
  `java-library`
  id("com.diffplug.spotless") version("6.25.0")
  id("me.champeau.jmh") version("0.7.2")
}

java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(17)
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
  api("org.jspecify:jspecify:0.3.0")
  testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
  testImplementation("org.assertj:assertj-core:3.25.3")
  testImplementation("net.jqwik:jqwik:1.8.3")
  testImplementation("org.msgpack:msgpack-core:0.9.8")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

repositories {
  mavenCentral()
}
