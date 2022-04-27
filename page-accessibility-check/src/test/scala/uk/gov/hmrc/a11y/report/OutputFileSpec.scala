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

import uk.gov.hmrc.a11y.BaseSpec
import uk.gov.hmrc.a11y.tools.Violation

class OutputFileSpec extends BaseSpec {

  trait Setup {
    val csvHeaders: String =
      "tool,testSuite,path,capturedPage,code,severity,alertLevel,description,snippet,helpUrl,knownIssue,furtherInformation\n"
  }

  "csvOutput should" should {

    "return Violations in CSV format" in new Setup {
      val violation = List(
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
          snippet = "snippet",
          helpUrl = "UNDEFINED",
          knownIssue = Some("KnownIssue"),
          furtherInformation = Some("furtherInformation")
        )
      )

      OutputFile.csvOutput(violation) shouldBe csvHeaders +
        "vnu," +
        "platops-example-a11y-check-with-all-pages-captured," +
        "/check-your-vat-flat-rate/vat-return-period," +
        "https://build.tax.service.gov.uk/job/PlatOps/job/Examples/job/platops-example-a11y-check-with-all-pages-captured/4/artifact/pages/1579253296146/index.html," +
        "UNDEFINED," +
        "info," +
        "INFO," +
        """The “type” attribute is unnecessary for JavaScript resources.,""" +
        "snippet," +
        "UNDEFINED," +
        "KnownIssue," +
        "furtherInformation\n"
    }

    "return empty string when knownIssue and furtherInformation is not provided" in new Setup {
      val violation = List(
        Violation(
          tool = "axe",
          testSuite = "platops-example-a11y-check-with-all-pages-captured",
          path = "/check-your-vat-flat-rate/vat-return-period",
          capturedPage =
            "https://build.tax.service.gov.uk/job/PlatOps/job/Examples/job/platops-example-a11y-check-with-all-pages-captured/4/artifact/pages/1579253296146/index.html",
          code = "identical-links-same-purpose",
          severity = "info",
          alertLevel = "INFO",
          description = """The “type” attribute is unnecessary for JavaScript resources.""",
          snippet = "snippet",
          helpUrl = "https://dequeuniversity.com/rules/axe/3.5/identical-links-same-purpose?application=webdriverjs"
        )
      )

      OutputFile.csvOutput(violation) shouldBe csvHeaders +
        "axe," +
        "platops-example-a11y-check-with-all-pages-captured," +
        "/check-your-vat-flat-rate/vat-return-period," +
        "https://build.tax.service.gov.uk/job/PlatOps/job/Examples/job/platops-example-a11y-check-with-all-pages-captured/4/artifact/pages/1579253296146/index.html," +
        "identical-links-same-purpose," +
        "info," +
        "INFO," +
        """The “type” attribute is unnecessary for JavaScript resources.,""" +
        "snippet," +
        "https://dequeuniversity.com/rules/axe/3.5/identical-links-same-purpose?application=webdriverjs," +
        "," +
        "\n"
    }

    "return empty csv file with message no violations found" in new Setup {
      val emptyViolations: List[Nothing] = List.empty
      OutputFile.csvOutput(emptyViolations) shouldBe "No accessibility violations reported"
    }
  }

}
