

lazy val commonSettings = Seq(
   organization := "org.bustos",
   version := "0.1.0",
   scalaVersion := "2.11.7"
)

lazy val commons = ProjectRef(file("../common"), "common")

lazy val browser = (project in file("."))
    .settings(name := "browser")
    .settings(commonSettings: _*)
    .settings(libraryDependencies ++= projectLibraries)
    .dependsOn(commons)

val slf4j_version = "1.7.6"
val akka_http_version = "10.0.11"

val projectLibraries = Seq(
    "com.typesafe.akka"       %% "akka-actor"           % "2.4.20",
    "com.typesafe.akka"       %% "akka-http-core"       % akka_http_version,
    "com.typesafe.akka"       %% "akka-http"            % akka_http_version,
    "com.typesafe.akka"       %% "akka-http-spray-json" % akka_http_version,
    "com.typesafe.akka"       %% "akka-http-testkit"    % akka_http_version,
    "org.seleniumhq.selenium" %  "selenium-java"        % "2.35.0",
    "org.scalatest"           %% "scalatest"            % "3.0.1",
    "org.specs2"              %% "specs2-core"          % "2.3.11" % Test,
    "com.wandoulabs.akka"     %% "spray-websocket"      % "0.1.3",
    "log4j"                   %  "log4j"                % "1.2.14",
    "org.slf4j"               %  "slf4j-api"            % slf4j_version,
    "org.slf4j"               %  "slf4j-log4j12"        % slf4j_version,
    "mysql"                   %  "mysql-connector-java" % "latest.release",
    "joda-time"               %  "joda-time"            % "2.7",
    "org.joda"                %  "joda-convert"         % "1.2"
)

