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

import uk.gov.hmrc.a11y.config.A11yFilter
import uk.gov.hmrc.a11y.config.Configuration.a11yFilters
import uk.gov.hmrc.a11y.tools.{AlertLevel, Violation}

trait Filters {

  def applyA11yFiltersFor(violations: List[Violation]): List[Violation] =
    violations
      .filterNot(globallySuppressibleViolation)
      .map(setNewAlertLevels())
      .map(addFurtherInformation())
      .map(markKnownIssues())

  def globallySuppressibleViolation: Violation => Boolean = (violation: Violation) => {
    a11yFilters().exists(a11yFilter =>
      filterMatchingViolation(a11yFilter, violation) &&
        a11yFilter.action.filterGlobally.contains("true")
    )
  }

  def addFurtherInformation(): Violation => Violation = (violation: Violation) => {
    a11yFilters().find(a11yFilter =>
      filterMatchingViolation(a11yFilter, violation) &&
        a11yFilter.action.filterGlobally.isEmpty
    ) match {
      case Some(matchedFilter) => violation.addFurtherInformation(Some(matchedFilter.action.furtherInformation))
      case None                => violation
    }
  }

  def markKnownIssues(): Violation => Violation = (violation: Violation) => {
    a11yFilters().find(a11yFilter =>
      filterMatchingViolation(a11yFilter, violation) &&
        a11yFilter.action.filterGlobally.isEmpty &&
        a11yFilter.action.knownIssue.contains("true")
    ) match {
      case Some(matchedFilter) => violation.setKnownIssue(matchedFilter.action.knownIssue)
      case None                => violation
    }
  }

  def setNewAlertLevels(): Violation => Violation = (violation: Violation) => {
    a11yFilters().find(a11yFilter =>
      filterMatchingViolation(a11yFilter, violation) &&
        a11yFilter.action.filterGlobally.isEmpty &&
        a11yFilter.action.alertLevel.isDefined
    ) match {
      case Some(matchedFilter) =>
        violation.updateAlertLevel(AlertLevel(matchedFilter.action.alertLevel.get.toLowerCase))
      case None                => violation
    }
  }

  private def filterMatchingViolation(a11yFilter: A11yFilter, violation: Violation): Boolean =
    violation.tool == a11yFilter.tool &&
      a11yFilter.descriptionRegex.findFirstIn(violation.description).isDefined &&
      a11yFilter.snippetRegex.findFirstIn(violation.snippet).isDefined
}
