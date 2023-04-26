/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.a11y.tools

import play.api.libs.functional.syntax._
import play.api.libs.json._
import uk.gov.hmrc.a11y.ShellClient
import uk.gov.hmrc.a11y.config.Configuration._
import uk.gov.hmrc.a11y.config.TestInfo
import uk.gov.hmrc.a11y.report.{Logger, OutputFile}

import scala.util.matching.Regex

class Axe(testInfo: TestInfo, shellClient: ShellClient = new ShellClient()) extends Logger {

  import testInfo._
  import shellClient._

  def violations(capturedPagesFolders: List[String]): List[Violation] =
    capturedPagesFolders.flatMap { capturedPageFolder =>
      val axeReport: Option[JsValue] = runAxe(capturedPageFolder)
      val pageUrl: String            = savedPageUrl(capturedPageFolder)
      val path: String               = urlPath(capturedPageFolder)
      parseReport(path, pageUrl, axeReport)
    }

  private[tools] def parseReport(path: String, pageUrl: String, axeReport: Option[JsValue]): List[Violation] =
    if (axeReport.isEmpty) {
      logger.error(s"Error generating AXE report for path: $path. Page captured at: $pageUrl. Error message: $stderr")
      List.empty
    } else {
      val axeViolations    = (axeReport.get \ 0 \ "violations").as[List[AxeAlert]]
      val incompleteAlerts = (axeReport.get \ 0 \ "incomplete").as[List[AxeAlert]]

      val allViolations = axeViolations.flatMap { violation =>
        violation.nodes.map { node =>
          Violation(
            "axe",
            testSuiteName,
            path,
            pageUrl,
            violation.code,
            node.severity,
            AlertLevel(node.alertLevel),
            node.failureSummary,
            node.snippet,
            violation.helpUrl
          )
        }
      }

      val allIncompleteAlerts: List[Violation] = incompleteAlerts.flatMap { incomplete =>
        incomplete.nodes.map { node =>
          Violation(
            "axe",
            testSuiteName,
            path,
            pageUrl,
            incomplete.code,
            node.severity,
            AlertLevel(node.alertLevel),
            s"Incomplete Alert: ${incomplete.description}",
            node.snippet,
            incomplete.helpUrl
          )
        }
      }
      allViolations ++ allIncompleteAlerts
    }

  private def runAxe(reportFolderPath: String): Option[JsValue] = {

    /**
      * axeRunner is a shell script created in accessibility-assessment Dockerfile to run axe-cli.
      */
    val axeCommand = s"""axeRunner --stdout --include main file://$reportFolderPath/index.html"""

    runCommand(axeCommand)

    for (i <- 1 to 3; if retryEnabled && stderr.mkString.nonEmpty) {
      logger.error(
        s"Error generating AXE report for $reportFolderPath: $stderr . Retrying AXE report generation. Count: $i"
      )
      runCommand(axeCommand)
    }

    if (stdout.mkString.isEmpty) {
      logger.error(s"Error generating AXE report for $reportFolderPath: $stderr.")
      None
    } else {
      logger.info(s"Completed running AXE for $reportFolderPath")
      OutputFile.writeToFile(s"$reportFolderPath/axe-report.json", stdout.mkString)
      Some(Json.parse(stdout.mkString))
    }
  }
}

case class Node(severity: String, alertLevel: String, snippet: String, failureSummary: String)

object Node {
  implicit val nodeReads: Reads[Node] = (
    ((JsPath \ "impact").read[String] or Reads.pure("UNDEFINED")) and
      ((JsPath \ "impact").read[String] or Reads.pure("UNDEFINED")) and
      ((JsPath \ "html").read[String] or Reads.pure("UNDEFINED")) and
      ((JsPath \ "failureSummary").read[String] or Reads.pure("UNDEFINED"))
  )(Node.apply _)
}

case class AxeAlert(description: String, helpUrl: String, code: String, nodes: List[Node])

object AxeAlert {
  implicit val axeAlertReads: Reads[AxeAlert] = (
    ((JsPath \ "description").read[String] or Reads.pure("UNDEFINED")) and
      ((JsPath \ "helpUrl").read[String] or Reads.pure("UNDEFINED")) and
      ((JsPath \ "id").read[String] or Reads.pure("UNDEFINED")) and
      (JsPath \ "nodes").read[List[Node]]
  )(AxeAlert.apply _)
}

object Axe {

  /**
    * Finds the axe-core version by running the command `axeRunner -v`.
    * By default, the version returned by running `axeRunner -V` is the version of axe-cli.
    * But we need the version of axe-core, the underlying engine that includes the accessibility rules.
    * So instead of `axeRunner -V`, we run the command `axeRunner -v` which outputs the axe-core version
    * being used. We use a regex pattern to extract the axe-core version number from the output.
    *
    * NOTE: axeRunner is a shell script created in accessibility-assessment Dockerfile to run axe-cli.
    */

  def axeVersion(shellClient: ShellClient = new ShellClient()): String = {
    val axeVersionPattern: Regex = ".*Running axe-core ([0-9]+\\.[0-9]+\\.[0-9]+).*".r

    /**
      * axeRunner is a shell script created in accessibility-assessment Dockerfile to run axe-cli.
      */
    val axeCommand = s"""axeRunner -v"""
    import shellClient._
    runCommand(axeCommand)

    stdout.mkString match {
      case axeVersionPattern(axeVersion) => axeVersion
      case _                             => "ERROR_DETERMINING_AXE_VERSION"
    }
  }
}
