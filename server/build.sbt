name := "xchess"
version := "current"

scalaVersion in ThisBuild := "2.13.4"
scalacOptions in ThisBuild ++= Seq(
  "-deprecation",
  "-feature",
  "-unchecked",
)

val AkkaVersion = "2.6.10"
val AkkaHttpVersion = "10.2.2"
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-stream"      % AkkaVersion,
  "com.typesafe.akka" %% "akka-http"        % AkkaHttpVersion,
  "de.heikoseeberger" %% "akka-http-circe"  % "1.31.0",
  "io.circe"          %% "circe-generic"    % "0.13.0",
  "io.circe"          %% "circe-parser"     % "0.13.0",
  "ch.qos.logback"    %  "logback-classic"  % "1.2.3"
)
