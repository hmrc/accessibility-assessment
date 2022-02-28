/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.a11y.report

import play.api.libs.json.{Json, Writes}
import uk.gov.hmrc.a11y.config.TestInfo
import uk.gov.hmrc.a11y.tools.{Axe, Violation, Vnu}

class JsonReport {

  def generate(testInfo: TestInfo, violations: List[Violation]): String = {

    val axe       = ToolInfo(
      "Axe-core",
      "axe",
      "accessibility",
      Axe.axeVersion()
    )
    val vnu       = ToolInfo(
      "VNU",
      "vnu",
      "HTML Validator",
      Vnu.vnuVersion()
    )
    val axeReport = reportByTool(violations, axe)
    val vnuReport = reportByTool(violations, vnu)
    val report    = Report(testInfo.testSuiteName, testInfo.buildNumber, testInfo.buildUrl, testInfo.numberOfPagesCaptured, totalUnKnownErrorCount(violations), List(axeReport, vnuReport))
    Json.prettyPrint(Json.toJson(report))
  }

  def totalUnKnownErrorCount(violations: List[Violation]): Int = {
    val errorViolations = violations.filter(_.alertLevel == "ERROR")
    val totalErrorCount = errorViolations.size
    val totalKnownErrorCount = errorViolations.count(_.knownIssue.contains("true"))
    totalErrorCount - totalKnownErrorCount
  }

  private[report] def reportByTool(allViolations: List[Violation], toolInfo: ToolInfo): Tool = {
    val violations  = allViolations.filter(_.tool == toolInfo.tool)
    val uniquePaths = violations.map(_.path).distinct

    val paths: List[Path] = uniquePaths.map { path =>
      val violationsForThisPath = violations.filter(_.path == path)
      val capturedPages         = violationsForThisPath.map(_.capturedPage).distinct
      val pages                 = capturedPages.map { capturedPage =>
        val violationsByCapturedPage = violationsForThisPath.filter(_.capturedPage == capturedPage)
        Page(
          s"Captured Page: ${capturedPages.indexOf(capturedPage) + 1}",
          capturedPage,
          violationsByCapturedPage.size,
          violationsByCapturedPage
        )
      }
      Path(path, pages.size, pages)

    }

    val errorCount = violations.count(_.alertLevel == "ERROR")
    val infoCount  = violations.count(_.alertLevel == "INFO")

    val summary = Summary(violations.size, errorCount, infoCount)
    Tool(toolInfo.name, toolInfo.tool, toolInfo.toolType, toolInfo.version, summary, paths)
  }
}

case class ToolInfo(name: String, tool: String, toolType: String, version: String)

object ToolInfo {
  implicit val toolInfoWrites: Writes[ToolInfo] = Json.writes[ToolInfo]
}

case class Page(name: String, url: String, violationCount: Int, violations: List[Violation])

object Page {
  implicit val pageWrites: Writes[Page] = Json.writes[Page]
}

case class Path(path: String, pageCount: Int, pages: List[Page])

object Path {
  implicit val pathWrites: Writes[Path] = Json.writes[Path]
}

case class Summary(violationCount: Int, errorCount: Int, informationalCount: Int)

object Summary {
  implicit val summaryWrites: Writes[Summary] = Json.writes[Summary]
}

case class Tool(name: String, tool: String, toolType: String, version: String, summary: Summary, paths: List[Path])

object Tool {
  implicit val accessibilityToolWrites: Writes[Tool] = Json.writes[Tool]
}

case class Report(testSuite: String, buildNo: Int, buildUrl: String, numberOfPagesCaptured: Int, totalUnKnownErrorCount:Int, tools: List[Tool])

object Report {
  implicit val reportWrites: Writes[Report] = Json.writes[Report]
}
