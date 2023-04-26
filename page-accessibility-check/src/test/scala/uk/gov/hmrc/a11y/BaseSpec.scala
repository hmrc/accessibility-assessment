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

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.a11y.config.TestInfo

class LostCredentialsTestInfo extends TestInfo {
  override val testSuiteName: String           = "lost-credentials-ui-journey-tests"
  override val capturedPagesLocation: String   = s"$projectRootDir/src/test/resources/it/$testSuiteName"
  override val jenkinsArtifactLocation: String = s"$projectRootDir/src/test/resources/it/$testSuiteName"
  override val buildUrl: String                = s"https://build.tax.service.gov.uk/job/$testSuiteName"
  override val buildNumber: Int                = 1
}

class MovementsTestInfo extends TestInfo {
  override val testSuiteName: String           = "movements-a11y-test-build-6"
  override val capturedPagesLocation: String   = s"$projectRootDir/src/test/resources/$testSuiteName"
  override val jenkinsArtifactLocation: String = s"$projectRootDir/src/test/resources/$testSuiteName"
  override val buildUrl: String                = s"https://build.tax.service.gov.uk/job/$testSuiteName"
  override val buildNumber: Int                = 1
}

class TAMCTestInfo extends TestInfo {
  override val testSuiteName: String           = "tamc-accessibility-tests-build-7"
  override val capturedPagesLocation: String   = s"$projectRootDir/src/test/resources/$testSuiteName"
  override val jenkinsArtifactLocation: String = s"$projectRootDir/src/test/resources/$testSuiteName"
  override val buildUrl: String                = s"https://build.tax.service.gov.uk/job/$testSuiteName"
  override val buildNumber: Int                = 1
}

trait BaseSpec extends AnyWordSpec with Matchers {
  val lostCredentialsTestInfo = new LostCredentialsTestInfo()
  val movementsTestInfo       = new MovementsTestInfo()
  val tamcTestInfo            = new TAMCTestInfo()

  case class TestException(message: String) extends RuntimeException(message)
}
