plugins {
  base
}

val buildRelease by tasks.registering(Zip::class) {
  dependsOn("mxpack-core:publishMavenPublicationToFileRepository")
  dependsOn("mxpack-kotlin:publishMavenPublicationToFileRepository")
  dependsOn("mxpack-core:check")
  dependsOn("mxpack-kotlin:check")
  from(file("mxpack-core/build/m2"))
  from(file("mxpack-kotlin/build/m2"))
  archiveFileName = "buildRelease.zip"
  destinationDirectory = layout.buildDirectory
}