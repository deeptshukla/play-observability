val scalaV2_12 = "2.12.10"

val commonSettings = List(
  organization := "com.deept",
  resolvers += DefaultMavenRepository,
  scalaVersion := scalaV2_12
)


val backendDependencies = List(
  ws,
  jdbc,
  guice,
  "com.h2database" % "h2" % "1.4.200",
  "org.slf4j" % "slf4j-api" % "1.7.30"
)


lazy val root = (project in file(".")).enablePlugins(PlayScala).settings(
  commonSettings ++
  List(
    name := """my-play-application""",
    version := "0.1.0",
    scalaVersion := scalaV2_12,
    libraryDependencies ++= backendDependencies,
    dependencyOverrides += "com.zaxxer" % "HikariCP" % "2.5.1"
  )
)
