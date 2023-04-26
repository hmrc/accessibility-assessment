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

import java.io.File

import scala.io.Source
import scala.util.matching.Regex

trait TestInfo {

  val projectRootDir: String = s"${System.getProperty("user.dir")}/page-accessibility-check"

  val testSuiteName: String
  val capturedPagesLocation: String
  val jenkinsArtifactLocation: String
  val buildUrl: String
  val buildNumber: Int
  def capturedPagesFolders: List[String] = new File(capturedPagesLocation)
    .listFiles()
    .filter(_.isDirectory)
    .map(s"$capturedPagesLocation/" + _.getName)
    .toList

  def numberOfPagesCaptured: Int = capturedPagesFolders.length

  def savedPageUrl(capturedPageFolder: String): String =
    s"$jenkinsArtifactLocation/${new File(capturedPageFolder).getName}/index.html"

  def urlPath(capturedPageFolder: String): String = {
    val bufferedSource = Source.fromFile(s"$capturedPageFolder/data")
    val url            = bufferedSource.getLines().take(1).toList.head
    val pattern        = "http:\\/\\/localhost:[0-9]{4,5}(.*)".r
    val pattern(path)  = url
    bufferedSource.close
    path
  }

}

class A11yTestInfo extends TestInfo {
  //TODO: Replace with sys.props
  override val testSuiteName: String           = System.getProperty("test.suite.name")
  override val capturedPagesLocation: String   = System.getProperty("test.suite.file.location")
  override val jenkinsArtifactLocation: String = System.getProperty("test.suite.artefact.location")
  override val buildUrl: String                = System.getProperty("test.suite.build.url")
  val buildNumberPattern: Regex                = ".*\\/([1-9]+)\\/$".r

  val buildNumber: Int = buildUrl match {
    case buildNumberPattern(buildNumber) => buildNumber.toInt
    case _                               => 0
  }
}
