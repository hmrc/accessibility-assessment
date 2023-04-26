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

package uk.gov.hmrc.a11y.config

import com.typesafe.config.{Config, ConfigFactory}
import uk.gov.hmrc.a11y.report.Logger

import scala.collection.JavaConverters._
import scala.util.matching.Regex

object Configuration extends Logger {

  lazy val a11yConfig: Config =
    try ConfigFactory.defaultApplication().getConfig("a11y-assessment-config")
    catch {
      case exception: com.typesafe.config.ConfigException.IO =>
        logger.error(s"Failed to loaded provided config with exception: $exception.")
        logger.warn("Reverting to use default application.conf")
        ConfigFactory.parseResources("application.conf").getConfig("a11y-assessment-config")
    }

  def a11yFilters(a11yConfig: Config = a11yConfig): List[A11yFilter] =
    a11yConfig.getConfigList("a11y-filters").asScala.toList.map { config =>
      A11yFilter(
        config.getString("tool"),
        config.getString("descriptionRegex").r,
        config.getString("snippetRegex").r,
        findFilterActions(config.getConfig("action"))
      )
    }

  def filtersEnabled: Boolean = a11yConfig.getBoolean("enable-filters")

  def retryEnabled: Boolean = a11yConfig.getBoolean("enable-retry")

  private def findFilterActions(actionConfig: Config): FilterAction = {
    def actionValue(path: String): Option[String] = if (actionConfig.hasPath(path))
      Some(actionConfig.getString(path))
    else
      None

    FilterAction(
      actionValue("filterGlobally"),
      actionValue("knownIssue"),
      actionConfig.getString("furtherInformation"),
      actionValue("alertLevel")
    )
  }
}

case class A11yFilter(tool: String, descriptionRegex: Regex, snippetRegex: Regex, action: FilterAction)

case class FilterAction(
  filterGlobally: Option[String],
  knownIssue: Option[String],
  furtherInformation: String,
  alertLevel: Option[String]
)
