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

import play.api.libs.json.{Json, Writes}

case class Violation(
  tool: String,
  testSuite: String,
  path: String,
  capturedPage: String,
  code: String,
  severity: String,
  alertLevel: String,
  description: String,
  snippet: String,
  helpUrl: String,
  knownIssue: Option[String] = None,
  furtherInformation: Option[String] = None
) {

  def addFurtherInformation(furtherInformation: Option[String]): Violation =
    this.copy(furtherInformation = furtherInformation)

  def updateAlertLevel(newAlertLevel: String): Violation =
    this.copy(alertLevel = newAlertLevel)

  def setKnownIssue(knownIssue: Option[String]): Violation =
    this.copy(knownIssue = knownIssue)
}

object Violation {
  implicit val violationWrites: Writes[Violation] = Json.writes[Violation]
}
