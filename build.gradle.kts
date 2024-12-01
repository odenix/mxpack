plugins {
  base
}

val centralBundle by tasks.registering(Zip::class) {
  dependsOn("mxpack-core:publishMavenPublicationToFileRepository")
  dependsOn("mxpack-kotlin:publishMavenPublicationToFileRepository")
  from(file("mxpack-core/build/m2"))
  from(file("mxpack-kotlin/build/m2"))
  archiveFileName = "centralBundle.zip"
  destinationDirectory = layout.buildDirectory
}