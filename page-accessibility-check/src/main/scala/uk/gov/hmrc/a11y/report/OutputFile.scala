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

package uk.gov.hmrc.a11y.report

import java.io.FileWriter
import org.apache.commons.lang3.StringEscapeUtils.ESCAPE_JSON
import org.apache.commons.lang3.StringEscapeUtils.ESCAPE_CSV
import play.api.libs.json.{Json, OWrites, Writes}
import uk.gov.hmrc.a11y.config.TestInfo
import uk.gov.hmrc.a11y.tools.{Axe, Violation, Vnu}

object OutputFile extends Logger {

  lazy val currentDirectoryPath: String = s"${System.getProperty("user.dir")}"

  lazy val reportFileName   = s"accessibility-output-${System.currentTimeMillis / 1000}-json.log"
  lazy val outputFileWriter = new FileWriter(s"$currentDirectoryPath/output/$reportFileName", true)

  //  refactor this to use writeToFile method once we understand FluentBit log setup in Build Jenkins
  def writeOutput(violationsList: List[Violation]): Unit = {
    implicit val reportWrites: OWrites[Violation] = Json.writes[Violation]
    violationsList.foreach { v =>
      outputFileWriter.write(
        s"""{"type" : "accessibility_audit", "log":"${ESCAPE_JSON.translate(Json.toJson(v).toString())}"}\n"""
      )
    }
  }

  def csvOutput(violationsList: List[Violation]): String = {

    val csvHeader: String = violationsList.headOption match {
      case Some(violation) => violation.getClass.getDeclaredFields.map(_.getName).toList.mkString(",") + "\n"
      case None            => "No accessibility violations reported"
    }

    val contents: String = violationsList.map { violation =>
      violation.productIterator.to.toList
        .map {
          case Some(value) => ESCAPE_CSV.translate(value.toString)
          case None        => ""
          case value       => ESCAPE_CSV.translate(value.toString)
        }
        .mkString(",") + "\n"
    }.mkString
    csvHeader + contents
  }

  def writeToFile(location: String, contents: String): Unit = {
    val writer = new FileWriter(s"$location", false)
    writer.write(contents)
    writer.close()
  }

  def closeFileWriter(): Unit =
    outputFileWriter.close()

  case class ToolInfo(name: String, tool: String, toolType: String, version: String)

  object ToolInfo {
    implicit val toolInfoWrites: Writes[ToolInfo] = Json.writes[ToolInfo]
  }

  lazy val axe: ToolInfo = ToolInfo("Axe-core", "axe", "accessibility", Axe.axeVersion())
  lazy val vnu: ToolInfo = ToolInfo("VNU", "vnu", "HTML Validator", Vnu.vnuVersion())

  def jsonOutput(testInfo: TestInfo, violations: List[Violation]): String =
    new JsonReport().generate(testInfo, violations, axe, vnu)
}
