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
    run / mainClass := Some("uk.gov.hmrc.a11y.PageAccessibilityCheck"),
    Compile / packageBin / mainClass := Some("uk.gov.hmrc.a11y.PageAccessibilityCheck"),
    assembly / assemblyJarName := "page-accessibility-check.jar",
    assembly / assemblyOutputPath := file(
      root.base.getAbsolutePath + "/accessibility-assessment-service/app/resources/page-accessibility-check.jar"
    ),
    assembly / test := {},
    Test / test := Def.sequential(Test / test, Test / assembly).value
  )
