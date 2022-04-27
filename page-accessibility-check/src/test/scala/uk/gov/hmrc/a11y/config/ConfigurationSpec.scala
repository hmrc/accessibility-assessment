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

package uk.gov.hmrc.a11y.config

import com.typesafe.config.{Config, ConfigException, ConfigFactory}
import uk.gov.hmrc.a11y.BaseSpec
import uk.gov.hmrc.a11y.config.Configuration.a11yFilters

class ConfigurationSpec extends BaseSpec {

  "Configuration" should {

    "should throw an exception when furtherInformation is not defined for a filter" in {
      val a11yTestConfig: Config =
        ConfigFactory
          .load("test-application.conf")
          .getConfig("a11y-assessment-config")

      assertThrows[ConfigException.Missing] {
        a11yFilters(a11yTestConfig)
      }
    }

    "should load the a11y-filters when required filter information is defined" in {
      val filters = a11yFilters()
      assert(filters.nonEmpty, ". a11y-filters is not loaded.")
    }
  }
}
