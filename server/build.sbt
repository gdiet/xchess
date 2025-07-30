lazy val xchess = project
  .in(file("."))
  .settings(
    name := "xchess",
    version := "current",
    scalaVersion := "3.3.5", // 3.3.x is LTS
    scalacOptions ++= Seq("-deprecation", "-unchecked"),
    libraryDependencies += "com.lihaoyi" %% "cask" % "0.10.2",
    fork := true
  )
