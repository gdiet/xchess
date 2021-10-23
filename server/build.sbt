val AkkaVersion     = "2.6.17"
val AkkaHttpVersion = "10.2.6"

lazy val xChess = project
 .in(file("."))
 .settings(
   name := "xChess",
   version := "current",
   scalaVersion := "3.1.0",
   scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked"),
   libraryDependencies ++= Seq(
     "com.typesafe.akka" %% "akka-http"        % AkkaHttpVersion,
     "de.heikoseeberger" %% "akka-http-circe"  % "1.38.2",
     "io.circe"          %% "circe-generic"    % "0.14.1",
     "io.circe"          %% "circe-parser"     % "0.14.1",
   ).map(_.cross(CrossVersion.for3Use2_13)) ++ Seq(
     "com.typesafe.akka" %% "akka-actor"       % AkkaVersion,
     "com.typesafe.akka" %% "akka-stream"      % AkkaVersion,
     "ch.qos.logback"    %  "logback-classic"  % "1.2.6",
   )
 )

lazy val collectJars = taskKey[Unit]("Collects deployment JARs.")
collectJars := {
 val jarsDir = baseDirectory.value / "target" / "universal" / "jars"
 IO.delete(jarsDir)
 val jars = (Runtime / fullClasspathAsJars).value.map(_.data)
 jars.foreach(file => IO.copyFile(file, jarsDir / file.name))
}
