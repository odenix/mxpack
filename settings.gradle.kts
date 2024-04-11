@file:Suppress("UnstableApiUsage")

plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version("0.8.0")
}

rootProject.name = "minipack"

include("bench")
include("core")

dependencyResolutionManagement {
  repositories {
    mavenCentral()
  }
  repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
}

for (prj in rootProject.children) {
  prj.buildFileName = "${prj.name}.gradle.kts"
}
