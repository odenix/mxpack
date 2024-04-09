@file:Suppress("UnstableApiUsage")

rootProject.name = "minipack"

include("bench")
include("core")
include("extension")

dependencyResolutionManagement {
  repositories {
    mavenCentral()
  }
  repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
}

for (prj in rootProject.children) {
  prj.buildFileName = "${prj.name}.gradle.kts"
}
