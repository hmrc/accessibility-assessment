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

import uk.gov.hmrc.a11y.report.Logger

import scala.sys.process._

class ShellClient extends Logger {

  val stdout = new StringBuilder
  val stderr = new StringBuilder

  def runCommand(command: String): (StringBuilder, StringBuilder) = {
    stdout.clear()
    stderr.clear()
    command ! ProcessLogger(stdout append _, stderr append _)
    (stdout, stderr)
  }
}
