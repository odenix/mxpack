@file:Suppress("UnstableApiUsage")

rootProject.name = "minipack"

include("core")
include("ext")

dependencyResolutionManagement {
  repositories {
    mavenCentral()
  }
  repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
}
