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

package uk.gov.hmrc.a11y

import uk.gov.hmrc.a11y.config.{A11yTestInfo, Configuration, TestInfo}
import uk.gov.hmrc.a11y.report.Logger
import uk.gov.hmrc.a11y.report.OutputFile._
import uk.gov.hmrc.a11y.tools.{Axe, Violation, Vnu}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

object PageAccessibilityCheck extends Logger with Filters {

  def main(args: Array[String]): Unit = {

    val testInfo = new A11yTestInfo()
    import testInfo._

    logger.info(s"Starting to parse accessibility reports for test suite: $testSuiteName")
    val violations: List[Violation] = runA11yCheck(Configuration.filtersEnabled, testInfo)

    writeOutput(violations)
    closeFileWriter()
    writeToFile(s"$currentDirectoryPath/output/accessibility-assessment-report.json", jsonOutput(testInfo, violations))
    writeToFile(s"$currentDirectoryPath/output/accessibility-assessment-report.csv", csvOutput(violations))

    logger.info(s"Total no of violations from all tools: ${violations.size}")
    logger.info(s"Finished parsing accessibility reports for test suite: $testSuiteName")
  }

  def runA11yCheck(applyFilters: Boolean, testInfo: TestInfo): List[Violation] = {
    import testInfo._

    val axe: Future[List[Violation]] = Future {
      new Axe(testInfo).violations(capturedPagesFolders)
    }

    val vnu: Future[List[Violation]] = Future {
      new Vnu(testInfo).violations(capturedPagesFolders)
    }

    val axeViolations: List[Violation] = Await.result(axe, Duration.Inf)
    logger.info(s"Parsing Axe reports completed for test suite $testSuiteName.")

    val vnuViolations: List[Violation] = Await.result(vnu, Duration.Inf)
    logger.info(s"Parsing VNU reports completed for test suite $testSuiteName.")

    if (applyFilters)
      applyA11yFiltersFor(axeViolations ::: vnuViolations)
    else
      axeViolations ::: vnuViolations
  }

}
