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

import play.api.libs.json.Json
import uk.gov.hmrc.a11y.{BaseSpec, ShellClient}

import scala.io.Source

class VnuSpec extends BaseSpec {

  trait TestSetup {
    val vnuTool                       = new Vnu(movementsTestInfo)
    val vnuAlert: List[VnuAlert]      = readVnuReport(
      s"${movementsTestInfo.capturedPagesLocation}/1586243984591/vnu-report.json"
    )
    val parsedReport: List[Violation] = vnuTool.parseReport("/test/path", "https://build.com/url", vnuAlert)
    val vnuError: Violation           = parsedReport
      .find(_.description == "Attribute “src” not allowed on element “image” at this point.") match {
      case Some(violation) => violation
      case None            => throw TestException("VNU ERROR violation not found")
    }
    val vnuInfo: Violation            = parsedReport
      .find(_.description == "The “type” attribute is unnecessary for JavaScript resources.") match {
      case Some(violation) => violation
      case None            => throw TestException("VNU INFO violation not found")
    }
  }

  "Vnu parseReport should" should {
    "not fail when the vnu report is not available" in {
      val vnuTool = new Vnu(lostCredentialsTestInfo)
      vnuTool.parseReport("/test/path", "https://build.com/url", List.empty) shouldBe empty
    }

    "return violations when vnu report has messages" in new TestSetup {
      assertResult(10, "VNU total violations count does not match")(parsedReport.size)
      assertResult(5, "VNU error count does not match")(parsedReport.count(_.alertLevel == "ERROR"))
      assertResult(5, "VNU info count does not match")(parsedReport.count(_.alertLevel == "INFO"))
    }

    "include provided testSuite and path" in new TestSetup {
      assertResult(movementsTestInfo.testSuiteName, "VNU violations count does not match")(vnuError.testSuite)
      assertResult("/test/path", "VNU violations count does not match")(vnuError.path)
    }

    "map capturedPage to provided URL" in new TestSetup {
      assertResult(movementsTestInfo.testSuiteName, "VNU violations count does not match")(vnuError.testSuite)
      assertResult("/test/path", "VNU violations count does not match")(vnuError.path)
      assertResult("https://build.com/url", "VNU violations count does not match")(vnuError.capturedPage)
    }

    "return UNDEFINED for code and helpUrl" in new TestSetup {
      assertResult("UNDEFINED", "VNU code not set to undefined")(vnuError.code)
      assertResult("UNDEFINED", "VNU helpUrl not set to undefined")(vnuError.helpUrl)
    }

    "map severity to type field in vnu report" in new TestSetup {
      assertResult("error", "VNU severity not mapped to ERROR as expected")(vnuError.severity)
      assertResult("info", "VNU severity not mapped to INFO as expected")(vnuInfo.severity)
    }

    "map alertLevel based on type field in vnu report" in new TestSetup {
      assertResult("ERROR", "VNU severity not mapped to ERROR as expected")(vnuError.alertLevel)
      assertResult("INFO", "VNU severity not mapped to INFO as expected")(vnuInfo.alertLevel)
    }

    "map snippet to extract field in vnu report" in new TestSetup {
      assertResult(
        "          <image src=\"/assets/images/govuk-logotype-crown.png\" xlink:href=\"\" class=\"govuk-header__logotype-crown-fallback-image\" width=\"36\" height=\"32\"></imag",
        "VNU snippet is incorrect"
      )(vnuError.snippet)
    }

  }

  "vnuVersion" should {
    "return ERROR_DETERMINING_AXE_VERSION version when stdout from the shellClient is empty" in {
      val mockShellClient = new ShellClient {
        override def runCommand(command: String): (StringBuilder, StringBuilder) = {
          stdout.clear()
          stdout.append("")
          (stdout, stderr)
        }
      }
      assertResult("ERROR_DETERMINING_VNU_VERSION", "vnu version is not as expected") {
        Vnu.vnuVersion(mockShellClient)
      }
    }

    "return expected version" in {
      val mockShellClient = new ShellClient {
        override def runCommand(command: String): (StringBuilder, StringBuilder) = {
          stdout.clear()
          stdout.append("20.5.29")
          (stdout, stderr)
        }
      }
      assertResult("20.5.29", "vnu version is not as expected") {
        Vnu.vnuVersion(mockShellClient)
      }
    }
  }

  def readVnuReport(reportPath: String): List[VnuAlert] = {
    val bufferedSource           = Source.fromFile(reportPath)
    val response: List[VnuAlert] = (Json.parse(bufferedSource.getLines().mkString) \ "messages").as[List[VnuAlert]]
    bufferedSource.close
    response
  }

}
