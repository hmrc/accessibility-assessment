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

import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.a11y.{BaseSpec, ShellClient}

import scala.io.Source

class AxeSpec extends BaseSpec {

  trait Setup {
    val axeTool                       = new Axe(tamcTestInfo)
    val axeResponse: JsValue          = readAxeReport(s"${tamcTestInfo.capturedPagesLocation}/1589186942193/axe-report.json")
    val parsedReport: List[Violation] = axeTool.parseReport("/test/path", "https://build.com/url", Some(axeResponse))

    val incompleteViolation: Violation =
      parsedReport.find(_.code == "identical-links-same-purpose") match {
        case Some(violation) => violation
        case None            => throw TestException("Incomplete Violation with provided code not found")
      }

    val violation: Violation =
      parsedReport.find(_.code == "duplicate-id-active") match {
        case Some(violation) => violation
        case None            => throw TestException("Violation with provided code not found")
      }

  }

  "Axe parseReport violations should" should {
    "not fail when Axe report is not available" in {
      val axeTool = new Axe(lostCredentialsTestInfo)
      axeTool.parseReport("/test/path", "https://build.com/url", None) shouldBe empty
    }

    "return no violations when Axe report has empty violations and incomplete list" in {
      val axeTool              = new Axe(movementsTestInfo)
      val axeResponse: JsValue =
        readAxeReport(s"${movementsTestInfo.capturedPagesLocation}/1586243903262/axe-report.json")
      axeTool.parseReport("/test/path", "https://build.com/url", Some(axeResponse)) shouldBe empty
    }

    "return correct count of complete and incomplete violations" in new Setup {
      assertResult(7, ". Axe total violation count does not match expected count.")(parsedReport.size)
      assertResult(6, ". Axe incomplete violation count does not match expected count.") {
        parsedReport.count(_.description.startsWith("Incomplete"))
      }
      assertResult(1, ". Axe total violation count does not match expected count.") {
        parsedReport.count(!_.description.startsWith("Incomplete"))
      }
    }

    "return correct count of alert levels" in new Setup {
      assertResult(7, ". Axe ERROR count does not match expected count.")(parsedReport.count(_.alertLevel == "ERROR"))
    }

    "return correct count of severity " in new Setup {
      assertResult(1, ". Axe severity count does not match expected count.")(parsedReport.count(_.severity == "minor"))
      assertResult(6, ". Axe severity count does not match expected count.")(
        parsedReport.count(_.severity == "serious")
      )
    }

    "match expected value for testSuite" in new Setup {
      assertResult(tamcTestInfo.testSuiteName, "Axe violation testSuite value is incorrect")(violation.testSuite)
      assertResult(tamcTestInfo.testSuiteName, "Axe incomplete violation testSuite value is incorrect")(
        incompleteViolation.testSuite
      )
    }

    "match violation path to provided path value" in new Setup {
      assertResult("/test/path", "Axe violation path is incorrect")(violation.path)
      assertResult("/test/path", "Axe incomplete violation path is incorrect")(incompleteViolation.path)
    }

    "match capturedPage to provided url" in new Setup {
      assertResult("https://build.com/url", "Axe violation capturedPage is incorrect")(violation.capturedPage)
      assertResult("https://build.com/url", "Axe incomplete violation capturedPage is incorrect")(
        incompleteViolation.capturedPage
      )
    }

    "map violation code to id field from Axe report" in new Setup {
      assertResult(5, ". Axe violation code does not match Axe report's code.")(
        parsedReport.count(_.code == "color-contrast")
      )
      assertResult(1, ". Axe violation code does not match Axe report's code.")(
        parsedReport.count(_.code == "identical-links-same-purpose")
      )
      assertResult(1, ". Axe violation code does not match Axe report's code.")(
        parsedReport.count(_.code == "duplicate-id-active")
      )
    }

    "match violation severity to impact field from Axe report " in new Setup {
      assertResult("minor", ". Axe violation severity does not match Axe report's impact field.") {
        incompleteViolation.severity
      }

      assertResult("serious", ". Axe violation severity does not match Axe report's impact field.") {
        violation.severity
      }
    }

    "map alertLevel to ERROR when Axe report impact value is minor" in new Setup {
      assertResult("ERROR", ". Axe violation alert level does not match Axe report's impact field.") {
        incompleteViolation.alertLevel
      }
    }

    "map alertLevel to ERROR when Axe report impact value is serious" in new Setup {
      assertResult("ERROR", ". Axe violation alert level does not match Axe report's impact field.") {
        violation.alertLevel
      }
    }

    "map violation description to Axe report's failure summary" in new Setup {
      assertResult(
        "Fix any of the following:\n  Document has active elements with the same id attribute: edit-field",
        ". Axe violation description does not match Axe report's failure summary."
      ) {
        violation.description
      }
    }

    "prefix 'Incomplete Alert' in the description of incomplete violations" in new Setup {
      assertResult(
        "Incomplete Alert: Ensure that links with the same accessible name serve a similar purpose",
        ". Axe parsed report description for incomplete violation is not as expected."
      ) {
        incompleteViolation.description
      }
    }

    "map violation snippet to Axe report's HTML field" in new Setup {
      assertResult(
        "<a href=\"/marriage-allowance-application/divorce-enter-year\" id=\"edit-field\">Change</a>",
        ". Axe violation snippet does not match Axe report's html."
      ) {
        violation.snippet
      }

      assertResult(
        "<a href=\"/marriage-allowance-application/divorce-enter-year\" id=\"edit-field\">Change</a>",
        ". Axe incomplete violation snippet does not match Axe report's html."
      ) {
        incompleteViolation.snippet
      }
    }

    "map helpUrl to Axe report's help URL" in new Setup {
      assertResult(
        "https://dequeuniversity.com/rules/axe/3.5/duplicate-id-active?application=webdriverjs",
        ". Axe violation helpUrl does not match Axe report's help url."
      ) {
        violation.helpUrl
      }

      assertResult(
        "https://dequeuniversity.com/rules/axe/3.5/identical-links-same-purpose?application=webdriverjs",
        ". Axe incomplete violation helpUrl does not match Axe report's helpUrl."
      ) {
        incompleteViolation.helpUrl
      }
    }
  }

  "axeVersion" should {
    "return ERROR_DETERMINING_AXE_VERSION version when stdout from the shellClient is empty" in {
      val mockShellClient = new ShellClient {
        override def runCommand(command: String): (StringBuilder, StringBuilder) = {
          stdout.clear()
          stdout.append("")
          (stdout, stderr)
        }
      }
      assertResult("ERROR_DETERMINING_AXE_VERSION", "axe-core version is not as expected") {
        Axe.axeVersion(mockShellClient)
      }
    }

    "return expected version when stdout from the shellClient matches the expected format" in {
      val mockShellClient = new ShellClient {
        override def runCommand(command: String): (StringBuilder, StringBuilder) = {
          stdout.clear()
          stdout.append(
            """Running axe-core 4.3.3 in chrome-headlessTesting complete of 0 pagesPlease note that only 20% to 50% of all accessibility issues can automatically be detected. Manual testing is always required. For more information see:https://dequeuniversity.com/curriculum/courses/testingmethods"""
          )
          (stdout, stderr)
        }
      }
      assertResult("4.3.3", "axe-core version is not as expected") {
        Axe.axeVersion(mockShellClient)
      }
    }
  }

  def readAxeReport(reportPath: String): JsValue = {
    val bufferedSource    = Source.fromFile(reportPath)
    val response: JsValue = Json.parse(bufferedSource.getLines().mkString)
    bufferedSource.close
    response
  }

}
