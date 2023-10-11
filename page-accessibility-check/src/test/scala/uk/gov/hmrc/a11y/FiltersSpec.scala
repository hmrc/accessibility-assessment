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

import org.scalatest.prop.{TableDrivenPropertyChecks, TableFor1}
import uk.gov.hmrc.a11y.tools.Violation

class FiltersSpec extends BaseSpec with Filters with TableDrivenPropertyChecks {

  trait TestSetup {
    val defaultViolation: Violation = Violation(
      tool = "tool",
      testSuite = "testSuite",
      path = "path",
      capturedPage = "capturedPage",
      code = "color-contrast",
      severity = "severity",
      alertLevel = "alertLevel",
      description = "description",
      snippet = "snippet",
      helpUrl = "helpUrl"
    )
  }

  "Filters" should {

    "Mutate alerts that match the incomplete color-contrast violations surfaced by axe" in new TestSetup {

      val violations: List[Violation] = List[Violation](
        defaultViolation.copy(
          tool = "axe",
          description =
            """Incomplete Alert: Ensures the contrast between foreground and background colors meets WCAG 2 AA contrast ratio thresholds""",
          snippet = "<input type=number >"
        )
      )

      val actualViolations: List[Violation] = applyA11yFiltersFor(violations)

      actualViolations.size                              shouldBe 1
      actualViolations.head.alertLevel                   shouldBe "INFO"
      actualViolations.head.furtherInformation.isDefined shouldBe true
      actualViolations.head.knownIssue.isEmpty           shouldBe true

    }

    "Remove alerts that match the duplicate readonly attribute not allowed violation surfaced by vnu" in new TestSetup {

      val violations: List[Violation] = List[Violation](
        defaultViolation.copy(
          tool = "vnu",
          description = """Attribute “readonly” not allowed on element “input” at this point.""",
          snippet = """pan>
                             |
                             |
                             |
                             |<input type="hidden" id="totalPayable1NoUnder" readonly="" name="totalPayable1NoUnder" value="200">
                             |
                             |
                             |</d""".trim()
        )
      )

      val actualViolations: List[Violation] = applyA11yFiltersFor(violations)

      actualViolations.size shouldBe 0
    }

    "Mutate aria violations surfaced by axe that highlight misuse of the aria-expanded attribute" in new TestSetup {

      val violations: List[Violation] = List[Violation](
        defaultViolation.copy(
          tool = "axe",
          description = "Fix any of the following:\n  ARIA attribute is not allowed: aria-expanded=\"false\"",
          snippet =
            """<input id="organisationType3" name="organisationType" value="3" type="radio" aria-controls="helptext-organisationType3" aria-expanded="false">"""
              .trim()
        )
      )

      val actualViolations: List[Violation] = applyA11yFiltersFor(violations)

      actualViolations.size shouldBe 0
    }

    "Mutate the violation reporting the misuse of the pattern attribute on an input tag, surfaced by vnu" in new TestSetup {

      val violations: List[Violation] = List[Violation](
        defaultViolation.copy(
          tool = "vnu",
          description =
            """Attribute “pattern” is only allowed when the input type is “email”, “password”, “search”, “tel”, “text”, or “url”.""",
          snippet =
            """          <input class="form-control " id="verifier_IREFFREGDATE_MONTH" name="verifier_IREFFREGDATE_MONTH" type="number" pattern="[0-9]*" value="">
                             | """.trim()
        )
      )

      val actualViolations: List[Violation] = applyA11yFiltersFor(violations)

      actualViolations.size                              shouldBe 1
      actualViolations.head.alertLevel                   shouldBe "alertLevel"
      actualViolations.head.furtherInformation.isDefined shouldBe true
      actualViolations.head.knownIssue.isDefined         shouldBe true
    }

    "Mutate violation reporting element image is missing required attribute width, surfaced by vnu" in new TestSetup {

      val violations: List[Violation] = List[Violation](
        defaultViolation.copy(
          tool = "vnu",
          description = """Element “image” is missing required attribute “width”.""",
          snippet =
            """<image src="/assets/images/govuk-logotype-crown.png" class="govuk-header__logotype-crown-fallback-image"></imag"""
              .trim()
        )
      )

      val actualViolations: List[Violation] = applyA11yFiltersFor(violations)

      actualViolations.size                              shouldBe 1
      actualViolations.head.alertLevel                   shouldBe "alertLevel"
      actualViolations.head.furtherInformation.isDefined shouldBe true
    }

    "Mutate violation reporting element image is missing required attribute height, surfaced by vnu" in new TestSetup {

      val violations: List[Violation] = List[Violation](
        defaultViolation.copy(
          tool = "vnu",
          description = """Element “image” is missing required attribute “height”.""",
          snippet =
            """<image src="/assets/images/govuk-logotype-crown.png" class="govuk-header__logotype-crown-fallback-image"></imag"""
              .trim()
        )
      )

      val actualViolations: List[Violation] = applyA11yFiltersFor(violations)

      actualViolations.size                              shouldBe 1
      actualViolations.head.alertLevel                   shouldBe "alertLevel"
      actualViolations.head.furtherInformation.isDefined shouldBe true
    }

    "Mutate violation reporting list element has direct children that are not allowed inside <dt> or <dd> elements, surfaced by axe" in new TestSetup {

      val violations: List[Violation] = List[Violation](
        defaultViolation.copy(
          tool = "axe",
          description =
            "Fix all of the following:\n List element has direct children that are not allowed inside <dt> or <dd> elements",
          snippet = """<dl class="govuk-summary-list govuk-!-margin-bottom-9">""".trim()
        )
      )

      val actualViolations: List[Violation] = applyA11yFiltersFor(violations)

      actualViolations.size                              shouldBe 1
      actualViolations.head.alertLevel                   shouldBe "alertLevel"
      actualViolations.head.furtherInformation.isDefined shouldBe true
    }

    "Mutate violation reporting the use of role=main inside a <main> element, surfaced by vnu" in new TestSetup {

      val violations: List[Violation] = List[Violation](
        defaultViolation.copy(
          tool = "vnu",
          description = """The “main” role is unnecessary for element “main”.""",
          snippet =
            """<main class="govuk-main-wrapper govuk-main-wrapper--auto-spacing" id="main-content" role="main">"""
              .trim()
        )
      )

      val actualViolations: List[Violation] = applyA11yFiltersFor(violations)

      actualViolations.size shouldBe 0
    }

    "Mutate violation reporting CSS: margin-right: Parse Error surfaced by vnu" in new TestSetup {

      val violations: List[Violation] = List[Violation](
        defaultViolation.copy(
          tool = "vnu",
          description = """CSS: “margin-right”: Parse Error.""",
          snippet = """a-inset-right)));margin-left:m"""
            .trim()
        )
      )

      val actualViolations: List[Violation] = applyA11yFiltersFor(violations)

      actualViolations.size shouldBe 0
    }

    "Mutate violation reporting CSS: margin-left: Parse Error surfaced by vnu" in new TestSetup {

      val violations: List[Violation] = List[Violation](
        defaultViolation.copy(
          tool = "vnu",
          description = """CSS: “margin-left”: Parse Error.""",
          snippet = """ea-inset-left)))}}}@media (min"""
            .trim()
        )
      )

      val actualViolations: List[Violation] = applyA11yFiltersFor(violations)

      actualViolations.size shouldBe 0
    }

    "Mutate violation reporting Element “span” not allowed as child of element “div” in this context by vnu" in new TestSetup {
      val summaryListSnippet: TableFor1[String] = Table(
        "snippet",
        """<span class="govuk-summary-list__actions"></span""",
        """<span class="govuk-summary-list__actions">""",
        """<span class="govuk-summary-list__actions govuk-no-border">"""
      )
      forAll(summaryListSnippet) { (snippet: String) =>
        val violations: List[Violation] = List[Violation](
          defaultViolation.copy(
            tool = "vnu",
            description =
              """Element “span” not allowed as child of element “div” in this context. (Suppressing further errors from this subtree.)""",
            snippet = snippet.trim()
          )
        )

        val actualViolations: List[Violation] = applyA11yFiltersFor(violations)

        assertResult(1, ". violations count did not match.")(actualViolations.size)
        assertResult("alertLevel", ". alertLevel did not match in violation.")(actualViolations.head.alertLevel)
        assertResult(true, ". furtherInformation is not provided in violation")(
          actualViolations.head.furtherInformation.isDefined
        )
      }
    }

    "Mutate violation reporting 'Bad value “” for attribute “name” on element “input”: Must not be empty.' by VNU for autocomplete__input" in new TestSetup {
      val snippets: TableFor1[String] = Table(
        "snippet",
        """div></div><input aria-expanded="false" aria-owns="country__listbox" aria-autocomplete="both" aria-describedby="country__assistiveHint" autocomplete="off" class="autocomplete__input autocomplete__input--show-all-values" id="country" name="" placeholder="" type="text" role="combobox"><svg v""",
        """div></div><input aria-expanded="false" aria-owns="value__listbox" aria-autocomplete="both" aria-describedby="value__assistiveHint" autocomplete="off" class="autocomplete__input autocomplete__input--show-all-values" id="value" name="" placeholder="" type="text" role="combobox"><svg v""",
        """div></div><input aria-expanded="false" aria-owns="details_address_country__listbox" aria-autocomplete="list" aria-describedby="details_address_country__assistiveHint" autocomplete="off" class="autocomplete__input autocomplete__input--show-all-values" id="details_address_country" name="" placeholder="" type="text" role="combobox"><svg v""",
        """div></div><input aria-expanded="false" aria-owns="details_address_country__listbox" aria-autocomplete="both" aria-describedby="details_address_country__assistiveHint" autocomplete="off" class="autocomplete__input autocomplete__input--default" id="details_address_country" name="" placeholder="" type="text" role="combobox"><ul cl"""
      )
      forAll(snippets) { (snippet: String) =>
        val violations: List[Violation] = List[Violation](
          defaultViolation.copy(
            tool = "vnu",
            description = """Bad value “” for attribute “name” on element “input”: Must not be empty."""".stripMargin,
            snippet = snippet
          )
        )

        val actualViolations: List[Violation] = applyA11yFiltersFor(violations)

        actualViolations.size shouldBe 0
      }
    }

    "Mutate violation reporting 'Element “input” is missing required attribute “role”.' for govuk-checkboxes__input by VNU" in new TestSetup {
      val snippets: TableFor1[String] = Table(
        "snippet",
        """> <input class="govuk-checkboxes__input" id="value" name="value[]" type="checkbox" value="01" aria-controls="conditional-value" aria-expanded="false">""",
        """tem"> <input type="checkbox" class="govuk-checkboxes__input" id="evidence_item-passport" name="evidence_item[]" value="passport" aria-describedby="passport-hint" aria-controls="conditional-passport-evidence-option-content" aria-expanded="false"> <""",
        """tem"> <input type="checkbox" class="govuk-checkboxes__input" id="evidence_item-p60-or-payslip" name="evidence_item[]" value="p60-or-payslip" aria-describedby="p60-or-payslip-hint" aria-controls="conditional-p60-or-payslip-selection" aria-expanded="false"> <""",
        """tem"> <input type="checkbox" class="govuk-checkboxes__input" id="evidence_item-call-credit" aria-describedby="call-credit-hint" name="evidence_item[]" value="call-credit" aria-controls="conditional-call-credit-evidence-option-content" aria-expanded="false"> <""",
        """tem"> <input type="checkbox" class="govuk-checkboxes__input" id="evidence_item-call-credit" aria-describedby="call-credit-hint" name="evidence_item[]" value="call-credit" checked="checked" aria-controls="conditional-call-credit-evidence-option-content" aria-expanded="true"> <"""
      )
      forAll(snippets) { (snippet: String) =>
        val violations: List[Violation] = List[Violation](
          defaultViolation.copy(
            tool = "vnu",
            description = """Element “input” is missing required attribute “role”."""".stripMargin,
            snippet = snippet.stripMargin.trim()
          )
        )

        val actualViolations: List[Violation] = applyA11yFiltersFor(violations)

        actualViolations.size shouldBe 0
      }
    }

    "Mutate violation reporting 'The role attribute must not be used on a table element' by VNU" in new TestSetup {
      val descriptions: TableFor1[String] = Table(
        "description",
        """The “role” attribute must not be used on a “tr” element which has a “table” ancestor with no “role” attribute, or with a “role” attribute whose value is “table”, “grid”, or “treegrid”.""",
        """The “role” attribute must not be used on a “td” element which has a “table” ancestor with no “role” attribute, or with a “role” attribute whose value is “table”, “grid”, or “treegrid”.""",
        """The “role” attribute must not be used on a “th” element which has a “table” ancestor with no “role” attribute, or with a “role” attribute whose value is “table”, “grid”, or “treegrid”."""
      )
      forAll(descriptions) { (description: String) =>
        val violations: List[Violation] = List[Violation](
          defaultViolation.copy(
            tool = "vnu",
            description = description
          )
        )

        val actualViolations: List[Violation] = applyA11yFiltersFor(violations)

        actualViolations.size            shouldBe 1
        actualViolations.head.alertLevel shouldBe "alertLevel"
        assert(actualViolations.head.furtherInformation.isDefined, ". FurtherInformation is not provided")
        assert(actualViolations.head.knownIssue.getOrElse("") == "true", ". KnownIssue is not set to true")
      }
    }

    "Mutate violation reporting 'Incomplete Alert: Ensures ARIA attributes are allowed for an element's role' by axe" in new TestSetup {
      val violations: List[Violation] = List[Violation](
        defaultViolation.copy(
          tool = "axe",
          description = "Incomplete Alert: Ensures ARIA attributes are allowed for an element's role",
          snippet =
            """<div id="accordion-incorrect-details-content-1" class="govuk-accordion__section-content" aria-labelledby="accordion-incorrect-details-heading-1">"""
        )
      )

      val actualViolations: List[Violation] = applyA11yFiltersFor(violations)

      actualViolations.size            shouldBe 1
      actualViolations.head.alertLevel shouldBe "alertLevel"
      assert(actualViolations.head.furtherInformation.isDefined, ". FurtherInformation is not provided")
      assert(actualViolations.head.knownIssue.getOrElse("") == "true", ". KnownIssue is not set to true")
    }

    "Mutate axe violation 'Fix all of the following: aria-labelledby attribute cannot be used on a div with no valid role attribute" in new TestSetup {
      val violations: List[Violation] = List[Violation](
        defaultViolation.copy(
          tool = "axe",
          description =
            "Fix all of the following: aria-labelledby attribute cannot be used on a div with no valid role attribute.",
          snippet =
            """<div class="govuk-accordion__section-content" id="accordion-incorrect-details-content-1" aria-labelledby="accordion-incorrect-details-heading-1">"""
        )
      )

      val actualViolations: List[Violation] = applyA11yFiltersFor(violations)

      actualViolations.size            shouldBe 1
      actualViolations.head.alertLevel shouldBe "alertLevel"
      assert(actualViolations.head.furtherInformation.isDefined, ". FurtherInformation is not provided")
      assert(actualViolations.head.knownIssue.getOrElse("") == "true", ". KnownIssue is not set to true")
    }

    "Mutate vnu violation 'Bad value “until-found” for attribute “hidden” on element “div”" in new TestSetup {
      val violations: List[Violation] = List[Violation](
        defaultViolation.copy(
          tool = "vnu",
          description = "Bad value “until-found” for attribute “hidden” on element “div”",
          snippet = """<div class="govuk-accordion__section-content" hidden="until-found">"""
        )
      )

      val actualViolations: List[Violation] = applyA11yFiltersFor(violations)

      actualViolations.size            shouldBe 1
      actualViolations.head.alertLevel shouldBe "alertLevel"
      assert(actualViolations.head.furtherInformation.isDefined, ". FurtherInformation is not provided")
      assert(actualViolations.head.knownIssue.getOrElse("") == "true", ". KnownIssue is not set to true")
    }

    "Mutate axe violation 'Fix any of the following: aria-label attribute does not exist or is empty' in autocomplete__menu class" in new TestSetup {
      val violations: List[Violation] = List[Violation](
        defaultViolation.copy(
          tool = "axe",
          description =
            "Fix any of the following: aria-label attribute does not exist or is empty aria-labelledby attribute does not exist, references elements that do not exist or references elements that are empty Element has no title attribute",
          snippet =
            """<ul class="autocomplete__menu autocomplete__menu--inline autocomplete__menu--visible" id="nationality__listbox" role="listbox"><li aria-selected="false" class="autocomplete__option" id="nationality__option--0" role="option" tabindex="-1" aria-posinset="1" aria-setsize="1">Spain</li></ul>"""
        )
      )

      val actualViolations: List[Violation] = applyA11yFiltersFor(violations)

      actualViolations.size shouldBe 0
    }

    "Mutate vnu violation: 'The “contentinfo” role is unnecessary for element “footer”'" in new TestSetup {
      val violations: List[Violation] = List[Violation](
        defaultViolation.copy(
          tool = "vnu",
          description = "The “contentinfo” role is unnecessary for element “footer”.",
          snippet = """div> <footer class="govuk-footer " role="contentinfo"> <div"""
        )
      )

      val actualViolations: List[Violation] = applyA11yFiltersFor(violations)

      actualViolations.size shouldBe 0
    }

    "Mutate vnu violation: 'The “banner” role is unnecessary for element “header”'" in new TestSetup {
      val violations: List[Violation] = List[Violation](
        defaultViolation.copy(
          tool = "vnu",
          description = "The “banner” role is unnecessary for element “header”.",
          snippet = """<header role="banner"> <"""
        )
      )

      val actualViolations: List[Violation] = applyA11yFiltersFor(violations)

      actualViolations.size shouldBe 0
    }

    "Mutate vnu violation: 'Attribute “src” not allowed on element “image” at this point.'" in new TestSetup {
      val violations: List[Violation] = List[Violation](
        defaultViolation.copy(
          tool = "vnu",
          description = "Attribute “src” not allowed on element “image” at this point.",
          snippet =
            """<image src="/vat-through-software/hmrc-frontend/assets/govuk/images/govuk-logotype-crown.png" xlink:href="" class="govuk-header__logotype-crown-fallback-image" width="36" height="32"></imag"""
        )
      )

      val actualViolations: List[Violation] = applyA11yFiltersFor(violations)

      actualViolations.size shouldBe 0
    }

    "Mutate vnu violation: 'The “group” role is unnecessary for element “fieldset”'" in new TestSetup {
      val violations: List[Violation] = List[Violation](
        defaultViolation.copy(
          tool = "vnu",
          description = "The “group” role is unnecessary for element “fieldset”.",
          snippet = """<fieldset class="govuk-fieldset" role="group" aria-describedby="dateOfBirth-hint">"""
        )
      )

      val actualViolations: List[Violation] = applyA11yFiltersFor(violations)

      actualViolations.size shouldBe 0
    }

    "Mutate vnu violation: 'Attribute “aria-expanded” not allowed on element “input” at this point'" in new TestSetup {
      val violations: List[Violation] = List[Violation](
        defaultViolation.copy(
          tool = "vnu",
          description = "Attribute “aria-expanded” not allowed on element “input” at this point.",
          snippet =
            """<input class="govuk-radios__input" id="continueUsingGoods1" name="continueUsingGoods" type="radio" value="1" data-label="No" aria-controls="conditional-continueUsingGoods1" aria-expanded="false">"""
        )
      )

      val actualViolations: List[Violation] = applyA11yFiltersFor(violations)

      actualViolations.size shouldBe 0
    }

    "Mutate the violation reporting the use of the inputmode attribute, surfaced by vnu" in new TestSetup {
      val violations: List[Violation] = List[Violation](
        defaultViolation.copy(
          tool = "vnu",
          description =
            """The “inputmode” attribute is not supported in all browsers. Please be sure to test, and consider using a polyfill.""",
          snippet =
            """<input class="govuk-input govuk-date-input__input govuk-input--width-2" id="dateTimeOfArrival.dateOfArrival.day" name="dateTimeOfArrival.dateOfArrival.day" type="text" inputmode="numeric"> <"""
        )
      )

      val actualViolations: List[Violation] = applyA11yFiltersFor(violations)

      actualViolations.size                              shouldBe 1
      actualViolations.head.alertLevel                   shouldBe "alertLevel"
      actualViolations.head.furtherInformation.isDefined shouldBe true
      actualViolations.head.knownIssue.isDefined         shouldBe true
    }

    """Mutate the violation reporting the use of async="true" on script tags injected by Google, surfaced by vnu""" in new TestSetup {
      val violations: List[Violation] = List[Violation](
        defaultViolation.copy(
          tool = "vnu",
          description = """Bad value “true” for attribute “async” on element “script”.""",
          snippet =
            """IE]>--> <script async="true" type="text/javascript" src="https://www.googletagmanager.com/gtag/js?id=G-93XB80XCLF&amp;l=dataLayer&amp;cx=c" nonce=""></scri"""
        ),
        defaultViolation.copy(
          tool = "vnu",
          description = """Bad value “true” for attribute “async” on element “script”.""",
          snippet = """<script src="https://www.googleishdomain.com/gtag/js?foo=bar" async="true" nonce="">"""
        )
      )

      val actualViolations: List[Violation] = applyA11yFiltersFor(violations)

      actualViolations.size                                  shouldBe 2
      actualViolations.count(_.alertLevel == "alertLevel")   shouldBe 2
      actualViolations.count(_.furtherInformation.isDefined) shouldBe 2
      actualViolations.count(_.knownIssue.isDefined)         shouldBe 2
    }

    """Mutate the violation reporting the use of http-equiv="origin-trial" on script tags injected by Google, surfaced by vnu""" in new TestSetup {
      val violations: List[Violation] = List[Violation](
        defaultViolation.copy(
          tool = "vnu",
          description = """Bad value “origin-trial” for attribute “http-equiv” on element “meta”.""",
          snippet = """e.png"> <meta http-equiv="origin-trial" content="AymqwRC7u88Y4JPvfIF2F37QKylC04248hLCdJAsh8xgOfe/dVJPV3XS3wLFca1ZMVOtnBfVjaCMTVudWM//5g4AAAB7eyJvcmlnaW4iOiJodHRwczovL3d3dy5nb29nbGV0YWdtYW5hZ2VyLmNvbTo0NDMiLCJmZWF0dXJlIjoiUHJpdmFjeVNhbmRib3hBZHNBUElzIiwiZXhwaXJ5IjoxNjk1MTY3OTk5LCJpc1RoaXJkUGFydHkiOnRydWV9"></head"""
        )
      )

      val actualViolations: List[Violation] = applyA11yFiltersFor(violations)

      actualViolations.size                                  shouldBe 1
      actualViolations.count(_.alertLevel == "alertLevel")   shouldBe 1
      actualViolations.count(_.furtherInformation.isDefined) shouldBe 1
      actualViolations.count(_.knownIssue.isDefined)         shouldBe 1
    }
  }
}
