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
import uk.gov.hmrc.a11y.config.Configuration.retryEnabled
import uk.gov.hmrc.a11y.config.TestInfo
import uk.gov.hmrc.a11y.report.{Logger, OutputFile}

import scala.util.matching.Regex

class Vnu(testInfo: TestInfo, shellClient: ShellClient = new ShellClient()) extends Logger {

  import testInfo._
  import shellClient._

  def violations(capturedPagesFolders: List[String]): List[Violation] =
    capturedPagesFolders.flatMap { capturedPageFolder =>
      val report: List[VnuAlert] = runVnu(capturedPageFolder)
      val pageUrl: String        = savedPageUrl(capturedPageFolder)
      val path: String           = urlPath(capturedPageFolder)
      parseReport(path, pageUrl, report)
    }

  private[tools] def parseReport(path: String, pageUrl: String, vnuReport: List[VnuAlert]): List[Violation] =
    vnuReport.map { alert =>
      Violation(
        "vnu",
        testSuiteName,
        path,
        pageUrl,
        code = "UNDEFINED",
        alert.severity,
        AlertLevel(alert.severity),
        alert.message,
        alert.extract,
        helpUrl = "UNDEFINED"
      )
    }

  private def runVnu(reportFolderPath: String): List[VnuAlert] = {

    val vnuCommand = s"""vnu --format json $reportFolderPath/index.html"""

    runCommand(vnuCommand)

    for (i <- 1 to 3; if retryEnabled && !stderr.mkString.contains("messages")) {
      logger.error(
        s"No 'messages' object in generated VNU report $stderr for $reportFolderPath. Retrying VNU report generation. Count: $i"
      )
      runCommand(vnuCommand)
    }

    if (stderr.mkString.contains("messages")) {
      logger.info(s"Completed running VNU for $reportFolderPath")
      OutputFile.writeToFile(s"$reportFolderPath/vnu-report.json", stderr.mkString)
      (Json.parse(stderr.mkString) \ "messages").as[List[VnuAlert]]
    } else {
      logger.error(s"No 'messages' object in generated VNU report for $reportFolderPath: $stderr.")
      List.empty
    }
  }
}

object Vnu {

  /**
    * Finds the VNU version by running the command `vnu --version`.
    * NOTE: vnu is a shell script created in accessibility-assessment Dockerfile to run vnu-jar.
    */

  def vnuVersion(shellClient: ShellClient = new ShellClient()): String = {
    val vnuCommand               = s"""vnu --version"""
    val vnuVersionPattern: Regex = "([0-9]+\\.[0-9]+\\.[0-9]+)".r
    import shellClient._

    runCommand(vnuCommand)

    stdout.mkString match {
      case vnuVersionPattern(version) => version
      case _                          => "ERROR_DETERMINING_VNU_VERSION"
    }
  }
}

case class VnuAlert(severity: String, message: String, extract: String)

object VnuAlert {
  implicit val vnuAlertReads: Reads[VnuAlert] = (
    ((JsPath \ "type").read[String] or Reads.pure("UNDEFINED")) and
      ((JsPath \ "message").read[String] or Reads.pure("UNDEFINED")) and
      ((JsPath \ "extract").read[String] or Reads.pure("UNDEFINED"))
  )(VnuAlert.apply _)
}
