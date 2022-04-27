/*
 * Copyright 2022 HM Revenue & Customs
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

package uk.gov.hmrc.a11y.report

import play.api.libs.json.Json
import uk.gov.hmrc.a11y.BaseSpec
import uk.gov.hmrc.a11y.tools.Violation

class JsonReportSpec extends BaseSpec {

  trait TestSetup {
    val axe        = ToolInfo("Axe", "axe", "accessibility", "3.1.4")
    val vnu        = ToolInfo("VNU", "vnu", "HTML Validator", "19.6.7")
    val jsonReport = new JsonReport()

    val vnuPage1Violation = List(
      Violation(
        tool = "vnu",
        testSuite = "platops-example-a11y-check-with-all-pages-captured",
        path = "/check-your-vat-flat-rate/vat-return-period",
        capturedPage =
          "https://build.tax.service.gov.uk/job/PlatOps/job/Examples/job/platops-example-a11y-check-with-all-pages-captured/4/artifact/pages/1579253296146/index.html",
        code = "UNDEFINED",
        severity = "info",
        alertLevel = "INFO",
        description = """The “type” attribute is unnecessary for JavaScript resources.""",
        snippet = "ror\">\n    <script type=\"text/javascript\">var re",
        helpUrl = "UNDEFINED"
      )
    )

    val vnuPage2Violation = List(
      Violation(
        tool = "vnu",
        testSuite = "platops-example-a11y-check-with-all-pages-captured",
        path = "/check-your-vat-flat-rate/vat-return-period",
        capturedPage =
          "https://build.tax.service.gov.uk/job/PlatOps/job/Examples/job/platops-example-a11y-check-with-all-pages-captured/4/artifact/pages/1579253298451/index.html",
        code = "UNDEFINED",
        severity = "info",
        alertLevel = "INFO",
        description = """The “type” attribute is unnecessary for JavaScript resources.""",
        snippet = "ror\">\n    <script type=\"text/javascript\">var re",
        helpUrl = "UNDEFINED"
      )
    )

    val vnuErrorViolations = List(
      Violation(
        tool = "vnu",
        testSuite = "platops-example-a11y-check-with-all-pages-captured\nt",
        path = "/check-your-vat-flat-rate/turnover",
        capturedPage =
          "https://build.tax.service.gov.uk/job/PlatOps/job/Examples/job/platops-example-a11y-check-with-all-pages-captured/4/artifact/pages/1579253297957/index.html",
        code = "UNDEFINED",
        severity = "error",
        alertLevel = "ERROR",
        description =
          """The value of the “for” attribute of the “label” element must be the ID of a non-hidden form control.""",
        snippet = "<dt><label for=\"vatReturnPeriod\">vatRet",
        helpUrl = "UNDEFINED"
      )
    )

    val axeViolations = List(
      Violation(
        tool = "axe",
        testSuite = "platops-example-a11y-check-with-all-pages-captured\nt",
        path = "/check-your-vat-flat-rate/turnover",
        capturedPage =
          "https://build.tax.service.gov.uk/job/PlatOps/job/Examples/job/platops-example-a11y-check-with-all-pages-captured/4/artifact/pages/1579253297957/index.html",
        code = "UNDEFINED",
        severity = "error",
        alertLevel = "ERROR",
        description =
          """The value of the “for” attribute of the “label” element must be the ID of a non-hidden form control.""",
        snippet = "<dt><label for=\"vatReturnPeriod\">vatRet",
        helpUrl = "UNDEFINED"
      )
    )

    val allViolations: List[Violation] =
      vnuPage1Violation ::: vnuPage2Violation ::: vnuErrorViolations ::: axeViolations

    val axeReport: Tool = jsonReport.reportByTool(allViolations, axe)
    val vnuReport: Tool = jsonReport.reportByTool(allViolations, vnu)
  }

  "JsonReport reportByTool" should {

    "report the errorCount, informationalCount and violationsCount for Axe" in new TestSetup {
      axeReport.summary.violationCount     shouldBe 2
      axeReport.summary.errorCount         shouldBe 1
      axeReport.summary.informationalCount shouldBe 0

    }

    "report the errorCount, informationalCount and violationsCount for VNU" in new TestSetup {
      vnuReport.summary.violationCount     shouldBe 3
      vnuReport.summary.errorCount         shouldBe 1
      vnuReport.summary.informationalCount shouldBe 2

    }

    "report the path information for AXE" in new TestSetup {
      axeReport.paths.size            shouldBe 1
      axeReport.paths.head.pageCount  shouldBe 1
      axeReport.paths.head.pages.size shouldBe 1
      axeReport.paths.head.path       shouldBe "/check-your-vat-flat-rate/turnover"
    }

    "report the path information for VNU" in new TestSetup {
      vnuReport.paths.size                                                                        shouldBe 2
      vnuReport.paths.map {
        _.path
      }                                                                                             should contain("/check-your-vat-flat-rate/vat-return-period")
      vnuReport.paths.map {
        _.path
      }                                                                                             should contain("/check-your-vat-flat-rate/turnover")
      vnuReport.paths.find(_.path == "/check-your-vat-flat-rate/vat-return-period").get.pageCount shouldBe 2
      vnuReport.paths.find(_.path == "/check-your-vat-flat-rate/turnover").get.pageCount          shouldBe 1
    }

    "report the page information for AXE" in new TestSetup {
      axeReport.paths.head.pageCount                 shouldBe 1
      axeReport.paths.head.pages.size                shouldBe 1
      axeReport.paths.head.pages.head.name           shouldBe "Captured Page: 1"
      axeReport.paths.head.pages.head.url            shouldBe axeViolations.head.capturedPage
      axeReport.paths.head.pages.head.violationCount shouldBe axeViolations.size
      axeReport.paths.head.pages.head.violations     shouldBe axeViolations
    }

    "report the page information for VNU" in new TestSetup {
      val path2Pages: List[Page] = vnuReport.paths
        .filter(_.path == "/check-your-vat-flat-rate/turnover")
        .head
        .pages

      path2Pages.size                shouldBe 1
      path2Pages.head.name           shouldBe "Captured Page: 1"
      path2Pages.head.violationCount shouldBe 1
      path2Pages.head.violations     shouldBe vnuErrorViolations
    }

    "report the page information for all the captured pages for the same path" in new TestSetup {
      val path1Pages: List[Page] = vnuReport.paths
        .filter(_.path == "/check-your-vat-flat-rate/vat-return-period")
        .head
        .pages

      path1Pages.size                shouldBe 2
      path1Pages.head.name           shouldBe "Captured Page: 1"
      path1Pages.head.violationCount shouldBe 1
      path1Pages.head.violations     shouldBe vnuPage1Violation
      path1Pages.last.name           shouldBe "Captured Page: 2"
      path1Pages.last.violationCount shouldBe 1
      path1Pages.last.violations     shouldBe vnuPage2Violation

    }
  }

  "JsonReport totalUnKnownErrorCount" should {

    trait TestSetup {
      val vnuInfoViolation = List(
        Violation(
          tool = "vnu",
          testSuite = "",
          path = "",
          capturedPage = "",
          code = "",
          severity = "info",
          alertLevel = "INFO",
          description = "",
          snippet = "",
          helpUrl = "UNDEFINED"
        )
      )

      val axeInfoViolation = List(
        Violation(
          tool = "axe",
          testSuite = "",
          path = "",
          capturedPage = "",
          code = "",
          severity = "info",
          alertLevel = "INFO",
          description = "",
          snippet = "",
          helpUrl = "UNDEFINED"
        )
      )

      val vnuInfoKnownIssueViolation = List(
        Violation(
          tool = "vnu",
          testSuite = "",
          path = "",
          capturedPage = "",
          code = "",
          severity = "info",
          alertLevel = "INFO",
          description = "",
          snippet = "",
          helpUrl = "UNDEFINED",
          knownIssue = Some("true")
        )
      )

      val axeInfoKnownIssueViolation = List(
        Violation(
          tool = "axe",
          testSuite = "",
          path = "",
          capturedPage = "",
          code = "",
          severity = "info",
          alertLevel = "INFO",
          description = "",
          snippet = "",
          helpUrl = "UNDEFINED",
          knownIssue = Some("true")
        )
      )

      val vnuErrorKnownIssueViolation = List(
        Violation(
          tool = "vnu",
          testSuite = "",
          path = "",
          capturedPage = "",
          code = "",
          severity = "error",
          alertLevel = "ERROR",
          description = "",
          snippet = "",
          helpUrl = "UNDEFINED",
          knownIssue = Some("true")
        )
      )

      val axeErrorKnownIssueViolation = List(
        Violation(
          tool = "axe",
          testSuite = "",
          path = "",
          capturedPage = "",
          code = "",
          severity = "minor",
          alertLevel = "ERROR",
          description = "",
          snippet = "",
          helpUrl = "UNDEFINED",
          knownIssue = Some("true")
        )
      )

      val axeErrorViolation = List(
        Violation(
          tool = "axe",
          testSuite = "",
          path = "",
          capturedPage = "",
          code = "",
          severity = "minor",
          alertLevel = "ERROR",
          description = "",
          snippet = "",
          helpUrl = "UNDEFINED"
        )
      )

      val vnuErrorViolation = List(
        Violation(
          tool = "vnu",
          testSuite = "",
          path = "",
          capturedPage = "",
          code = "",
          severity = "error",
          alertLevel = "ERROR",
          description = "",
          snippet = "",
          helpUrl = "UNDEFINED"
        )
      )
    }

    "return 0 totalUnKnownErrorCount when there are only INFO level violations" in new TestSetup {
      new JsonReport().totalUnKnownErrorCount(vnuInfoViolation ::: axeInfoViolation) shouldBe 0
    }

    "return 0 totalUnKnownErrorCount when there are only INFO level knownIssue violations" in new TestSetup {
      new JsonReport().totalUnKnownErrorCount(vnuInfoKnownIssueViolation ::: axeInfoKnownIssueViolation) shouldBe 0
    }

    "return 0 totalUnKnownErrorCount when all ERROR level violations are knownIssues" in new TestSetup {
      new JsonReport().totalUnKnownErrorCount(vnuErrorKnownIssueViolation ::: axeErrorKnownIssueViolation) shouldBe 0
    }

    "return correct totalUnKnownErrorCount when there are ERROR level violations" in new TestSetup {
      new JsonReport().totalUnKnownErrorCount(vnuErrorViolation ::: axeErrorViolation) shouldBe 2
    }

    "return totalUnKnownErrorCount excluding any ERROR that is a knownIssue" in new TestSetup {
      new JsonReport().totalUnKnownErrorCount(vnuErrorKnownIssueViolation ::: axeErrorViolation) shouldBe 1
    }

    "return totalUnKnownErrorCount only for ERROR level Violations" in new TestSetup {
      new JsonReport().totalUnKnownErrorCount(vnuErrorViolation ::: axeInfoKnownIssueViolation) shouldBe 1
    }
  }

  "JsonReport generate" should {
    "return the count of all of the pages captured" in new TestSetup {
      val numberOfPagesInCapturedPagesFolder: Int = 2
      val generatedReport: String                 = new JsonReport().generate(movementsTestInfo, allViolations)

      val actualNumberOfPagesCaptured: Int = (Json.parse(generatedReport) \ "numberOfPagesCaptured").as[Int]

      actualNumberOfPagesCaptured shouldBe numberOfPagesInCapturedPagesFolder
    }

    "return the count of all of the pages captured even when no violations are found" in new TestSetup {
      val numberOfPagesInCapturedPagesFolder: Int = 2
      val generatedReport: String                 = new JsonReport().generate(movementsTestInfo, List.empty)

      val actualNumberOfPagesCaptured: Int = (Json.parse(generatedReport) \ "numberOfPagesCaptured").as[Int]

      actualNumberOfPagesCaptured shouldBe numberOfPagesInCapturedPagesFolder
    }
  }
}
