lazy val xchess = project
  .in(file("."))
  .settings(
    name := "xchess",
    version := "current",
    scalaVersion := "3.7.3", // 3.7 brings named tuples
    scalacOptions ++= Seq("-deprecation", "-unchecked", "-Werror", "-Yexplicit-nulls"),
    libraryDependencies += "com.lihaoyi" %% "cask" % "0.10.2",
    libraryDependencies += "org.scalameta" %% "munit" % "1.1.2" % Test,
  )
