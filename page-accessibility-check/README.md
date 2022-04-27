# page-accessibility-check
`page-accessibility-check` is a scala project used by
 [accessibility-assessment docker image](https://github.com/hmrc/accessibility-assessment).
  `page-accessibility-check` assesses the provided HTML pages for accessibility violations with tools:
  [axe-cli](https://www.npmjs.com/package/@axe-core/cli) and [vnu](https://www.npmjs.com/package/vnu-jar). 
  `page-accessibility-check` generates below output:
   - Log file with the violations in the json format that can be consumed by ELK stack
   - CSV report
   - JSON report for accessibility-assessment service to [generate html report](https://github.com/hmrc/accessibility-assessment/blob/ff8d665ae2a9b6ac9fbe776adbc19766864e4318/app/services/htmlReport.js#L10).
   - App logs in the json format that can be consumed by ELK stack  
   
## Local Development

### Updating Global Filters
The parser uses a [configuration file](src/main/resources/application.conf) to apply filters on the generated
violations. This is applied globally for every generated report. The violations are matched with `tool` name and
with a regular expression for the violation's `description` and `snippet`. For the matched violations, the actions specified under each filter, in the
configuration file, will be applied. Below actions can be configured in the filter.

- `filterGlobally`: Suppress matching violations in the report. Violations that are not relevant on the platform.
- `alertLevel`: Modify the alert levels of the matching violations. e.g. `ERROR` to `WARNING`
- `knownIssue`: Tag Violations resulting from known issues with a `knownIssue` boolean flag
- `furtherInformation`: Add furtherInformation for the matching violation

>Be aware the `descriptionRegex` values are taken from the output of the tools, and in the case of VNU
contain non-standard double quote characters.

An example filter to modify the matching violation's `alertLevel` and  add `furtherInformation` :

 ```
       {
          tool = "vnu"
          descriptionRegex = """Attribute “readonly” is only allowed when the input type is “date”, “datetime-local”, “email”, “month”, “number”, “password”, “search”, “tel”, “text”, “time”, “url”, or “week”\."""
          snippetRegex = """.*<input.*(type="hidden".*readonly=""|readonly="".*type="hidden"){1}.*>"""
          action {
            knownIssue = false
            furtherInformation = "While this is a valid finding, readonly attributes on input tags of type 'hidden' have no effect on the page usability or accessibility."
            alertLevel = "WARNING"
          }
        }
```

An example filter to suppress the violation from the generated report:
```
    {
      tool = "vnu"
      descriptionRegex = """Attribute “readonly” not allowed on element “input” at this point\."""
      snippetRegex = """.*<input.*(type="hidden".*readonly=""|readonly="".*type="hidden"){1}.*>"""
      action {
        filterGlobally = true
      }
    }
```

### Running unit tests
```sbt
sbt clean test
```
These tests do not invoke Axe and VNU. A pre-generated Axe and VNU report is used to test parsing functionality. 
These reports are placed along with their respective captured pages [here](/src/test/resources/movements-a11y-test-build-6) and 
[here](/src/test/resources/tamc-accessibility-tests-build-7).
 
### To generate output using Axe and VNU locally
 
#### Pre-requisites
axe-cli and vnu should be installed locally and available to be executed from within the repo. 
The instruction to install axe and VNU are made available in accessibility-assessment
[DEVELOPMENT.md](https://github.com/hmrc/accessibility-assessment/blob/main/DEVELOPMENT.md#pre-requisites)

#### To generate output
To generate output for an existing set of captured pages run
```shell script
./page-accessibility-check/generate_parser_output.sh
```
The output is generated for the TESTSUITE_NAME defined [here](generate_parser_output.sh). The captured pages of various
tests suites are located [here](src/test/resources/it). To generate output for a different test suite, download the captured
pages from Build Jenkins for that test suite and placed it under `src/test/resources/it` and update the TESTSUITE_NAME in the script. 

The script does not perform any checks. The script is used only to manually check if the output is being generated after any changes 
or to investigate any failures locally.

>Running `./generate_parser_output.sh` uses Axe and VNU installed on the host machine. This version may not necessarily
be the same as used in [accessibility-assessment](https://github.com/hmrc/accessibility-assessment)

### Building the project
The parser is distributed as an executable fat jar (using the [sbt-assembly plugin](https://github.com/sbt/sbt-assembly)).  You can build the jar manually with the following sbt command:

`sbt clean assembly`

As the page-accessibility-check is a submodule of accessibility-assessment project,
by running 

`sbt clean test`

The jar file will be automatically generated in the correct location after tests have successfully passed.

### End-to-end tests
For end-to-end test validation build 
[accessibility-assessment image locally](https://github.com/hmrc/accessibility-assessment/blob/main/DEVELOPMENT.md#building-and-running-the-image-locally-during-development)
and follow the instructions to [test the accessibility-assessment image locally](https://github.com/hmrc/accessibility-assessment/blob/main/DEVELOPMENT.md#acceptance-tests) 

### Formatting code
This repository uses [Scalafmt](https://scalameta.org/scalafmt/), a code formatter for Scala. The formatting rules configured for this repository are defined within [.scalafmt.conf](.scalafmt.conf). Prior to checking in any changes to this repository, please make sure all files are formatted correctly.

To apply formatting to this repository using the configured rules in [.scalafmt.conf](.scalafmt.conf) execute:

```
sbt scalafmtSbt scalafmtAll
```

To check files have been formatted as expected execute:

```
sbt scalafmtCheckAll scalafmtSbtCheck
```
