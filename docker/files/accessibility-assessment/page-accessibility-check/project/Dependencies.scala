import sbt._

object Dependencies {

  val compile = Seq(
    "com.typesafe.play"          %% "play-json"           % "2.6.13",
    "org.apache.commons"          % "commons-lang3"       % "3.5",
    "com.typesafe.scala-logging" %% "scala-logging"       % "3.5.0",
    "ch.qos.logback"              % "logback-classic"     % "1.1.2",
    "uk.gov.hmrc"                %% "logback-json-logger" % "4.4.0",
    "com.typesafe"                % "config"              % "1.3.4",
    "com.vladsch.flexmark"        % "flexmark-all"        % "0.35.10"
  )

  val test = Seq(
    "org.scalatest" %% "scalatest" % "3.2.0" % Test
  )
}
