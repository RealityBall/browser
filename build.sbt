name := "MLB"

version := "1.0"

scalaVersion := "2.11.2"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

resolvers += "Spray" at "http://repo.spray.io"

lazy val root = (project in file(".")).enablePlugins(SbtTwirl)

libraryDependencies ++= {
  val sprayV = "1.3.2"
  val akkaV = "2.3.6"
  Seq(
    "io.spray"            %%  "spray-can"     % sprayV,
    "io.spray"            %%  "spray-routing" % sprayV,
    "io.spray"            %%  "spray-testkit" % sprayV  % "test",
    "io.spray"            %%  "spray-json"    % "1.3.1",
    "com.typesafe.akka"   %%  "akka-actor"    % akkaV,
    "com.typesafe.akka"   %%  "akka-testkit"  % akkaV   % "test",
    "com.typesafe.slick"  %%  "slick" % "2.1.0",
    "org.specs2"          %%  "specs2-core"   % "2.3.11" % "test",
    "com.wandoulabs.akka" %% "spray-websocket" % "0.1.3",
    "org.slf4j"           %  "slf4j-nop" % "1.6.4",
    "mysql" % "mysql-connector-java" % "latest.release"
  )
}

Revolver.settings