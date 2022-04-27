import sbt.file
import sbtassembly.AssemblyPlugin.autoImport.assemblyOutputPath

lazy val root: Project = (project in file("."))
  .settings(
    name := "accessibility-assessment",
    version := "0.1.0",
    scalaVersion := "2.12.12"
  )
  .aggregate(pagecheck)

lazy val pagecheck: Project = (project in file("page-accessibility-check"))
  .enablePlugins(SbtAutoBuildPlugin)
  .disablePlugins(JUnitXmlReportPlugin) //Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(
    name := "page-accessibility-check",
    version := "0.1.0",
    scalaVersion := "2.12.12",
    scalacOptions ++= Seq("-feature"),
    libraryDependencies ++= Dependencies.test ++ Dependencies.compile,
    Test / unmanagedResources / excludeFilter := "*.html",
    mainClass in run := Some("uk.gov.hmrc.a11y.PageAccessibilityCheck"),
    mainClass in (Compile, packageBin) := Some("uk.gov.hmrc.a11y.PageAccessibilityCheck"),
    assemblyJarName in assembly := "page-accessibility-check.jar",
    assemblyOutputPath in assembly := file(
      root.base.getAbsolutePath + "/accessibility-assessment-service/app/resources/page-accessibility-check.jar"
    )
  )
