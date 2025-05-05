@file:Suppress("UnstableApiUsage")

plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version("0.10.0")
}

rootProject.name = "mxpack"

include("benchmark")
include("mxpack-core")
include("mxpack-kotlin")

dependencyResolutionManagement {
  repositories {
    mavenCentral()
  }
  repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
}
