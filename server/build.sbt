name := "xchess"
version := "current"

scalaVersion := "2.13.8"
scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-unchecked",
)

val AkkaVersion     = "2.6.19"
val AkkaHttpVersion = "10.2.9"
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-stream"      % AkkaVersion,
  "com.typesafe.akka" %% "akka-http"        % AkkaHttpVersion,
  "de.heikoseeberger" %% "akka-http-circe"  % "1.39.2",
  "io.circe"          %% "circe-generic"    % "0.14.2",
  "io.circe"          %% "circe-parser"     % "0.14.2",
  "ch.qos.logback"    %  "logback-classic"  % "1.2.11"
)

lazy val collectJars = taskKey[Unit]("Collects deployment JARs.")
collectJars := {
  val jarsDir = baseDirectory.value / "target" / "universal" / "jars"
  IO.delete(jarsDir)
  val jars = (Runtime / fullClasspathAsJars).value.map(_.data)
  jars.foreach(file => IO.copyFile(file, jarsDir / file.name))
}
