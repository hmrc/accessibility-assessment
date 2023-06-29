# Development
*Note that to date this project has only been developed on Mac OSX.*

## Pre-requisites
For local development you will need to satisfy the following pre-reqs:
- Install [docker](https://docs.docker.com/install), v19.x or above;
- set your `WORKSPACE` environment variable, and ensure that this project is cloned in the root of the workspace.  i.e. ${WORKSPACE}/accessibility-assessment
- accessibility-assessment is run as a docker container in Build. Hence, we recommend testing the changes within Docker.
   The Dockerfile will run a set of jest tests when building the image. This should be sufficient to test the changes.
   To run tests outside the Docker container, you would require Axe and Vnu setup locally.

### To install axe-cli
Install axe-core via `npm`. Note that this version may be different to the version used in [package.json](package.json)
```npm install @axe-core/cli -g```

Create a shell script named `axeRunner` in `/usr/local/bin`. This script name matches the invocation in page-accessibility-check.
```
vi /usr/local/bin/axeRunner
```

And add the below lines. ${WORKSPACE} is the root where the accessibility-assessment is cloned.
```
#!/bin/bash
cd ${WORKSPACE}/accessibility-assessment;npx axe "$@"
```
 
Set the permission to execute the script
```
chmod u+x /usr/local/bin/axeRunner
```

To test the installation, run the command
```
axeRunner --show-errors -v
```

If there is an error related to chromedriver version mismatch, copy the required chromedriver version to the node module manually. 
For example, to copy the latest chromedriver from your `bin`:
```
cp /usr/local/bin/chromedriver ~/.nvm/versions/node/v12.16.1/lib/node_modules/@axe-core/cli/node_modules/chromedriver/lib/chromedriver/chromedriver
```

Now running the below command should not throw an error:
```
axeRunner --show-errors -v
```

### To install VNU
```npm install vnu-jar -g```

and run the below commands to make VNU available from terminal:

####Linux
```
sudo printf "\#\!\/usr\/bin\/env bash\n\njava -jar ${HOME}/.nvm/versions/node/$(node -v)/lib/node_modules/vnu-jar/build/dist/vnu.jar \"\$\@\"" > /usr/bin/vnu  \ 
&& chmod +x /usr/bin/vnu
```
####Mac OS
```
sudo printf '#!/usr/bin/env bash\n\njava -jar /usr/local/lib/node_modules/vnu-jar/build/dist/vnu.jar "$@"' > /usr/local/bin/vnu && chmod +x /usr/local/bin/vnu
```

## Updating axe and vnu versions
To update the versions of axe (axe-core, axe-core/cli) and/or vnu (vnu-jar), update the version number specified within
the [package.json](package.json) file and then follow the steps [here](#building-and-running-the-image-locally-during-development)

`axe-core` requires chromedriver. The [.npmrc](.npmrc) file specifies a npm config property which detects compatibility
of the currently installed chromedriver version before installing a new version. Further information on this npm config
property can be found here https://www.npmjs.com/package/chromedriver

## Building and running the image locally during development
The repository uses a [Makefile](Makefile) to build and run the accessibility-assessment image. 
The Dockerfile also includes a step to RUN npm tests when building the image. This should be sufficient to test the changes.

To build and run the accessibility-assessment container locally, use command:
```makefile
make run_local
```
To list all the available options, run:

```makefile
make
```

## Running accessibility-assessment-service tests locally during development
accessibility-assessment-service includes a set of unit, integration and e2e tests. The tests
have been implemented using [Jest](https://jestjs.io/). The Dockerfile has been setup to run these tests as part of building 
the image. So there may not be a need to run these tests locally. If for any reason, to run these tests locally, then
axe and vnu must be installed locally as the e2e tests require them. See [pre-requisites](#pre-requisites) for further information.

To run the tests locally:

```
npm install
## runs npm test for accessibility-assessment service when building the image. \
## --runInBand disables running test in parallel.
npm run test -- --runInBand
```

## Updating the filters
If you're adding, removing or updating any [`page-accessibility-check` filters](page-accessibility-check/src/main/resources/application.conf)
(eg. to annotate a known issue), you *must* run `sbt test`, which implicitly assembles `page-accessibility-check.jar`.
You must then commit this jar file along with any other changes.  This is due to current limitations of the build environment.

## Updating the image
Most updates can be tested easily by starting the service in your local development environment by executing `node app.js` from the [accessibility-assessment-service/app](accessibility-assessment-service/app/) directory.  However, triggering page assessments requires that you have specific versions of axe-cli and the vnu jar installed and configured appropriately.

As the [Dockerfile](docker/Dockerfile) contains all of the setup/configuration required to get the service running as it would in CI, the most reliable way to develop and test the service is by mounting appropriate files/folders in the [accessibility-assessment-service/app](accessibility-assessment-service/app) directory to the running docker container.

# Testing
## Postman Collection
There is a [Postman](https://www.postman.com/downloads/) collection in the [test](test/postman-collections) directory which contains a list of basic requests for each of the endpoints implemented in the service.

## Acceptance tests
The [accessibility-assessment-tests](https://github.com/hmrc/accessibility-assessment-tests) project contains captured pages from approximately 5 sample builds taken from CI which make a valueable regression test, and can be used to identify the addition/removal of rules when upgrading axe and vnu.

There is also a performance test available which ensures that builds of ~350 pages can be assessed successfully.

## Troubleshooting accessibility-assessment locally

### accessibility-assessment logs
The accessibility-assessment container logs are surfaced from two different sources:
* accessibility-assessment-service
* page-accessibility-check

#### accessibility-assessment-service logs
The logs from accessibility-assessment-service includes information about pages captured and the status of accessibility-assessment-service.
These logs can be obtained from the [GET api/logs/app](README.md#GET api/logs/app) API. This does not include the logs 
generated when assessing the page using Axe and VNU. For logs from Axe and VNU see [page-accessibility-check logs](#page-accessibility-check-logs).

accessibility-assessment-service log can also be accessed by using the docker command where `a11y` is the container name.
```bash
docker logs a11y
```
To follow these logs during an assessment:
```bash
docker logs -f a11y
```
#### page-accessibility-check logs
The logs from [page-accessibility-check](https://github.com/hmrc/page-accessibility-check) includes the logs generated 
when assessing the pages with Axe and VNU.  

Viewing this logs locally requires running the Docker [exec](https://docs.docker.com/engine/reference/commandline/exec/) 
command in the running a11y container. The below command creates a new Bash session in the container.

```bash
#executes an interactive bash shell on the container.
docker exec -it a11y /bin/bash 
```
The logs then can be accessed as below.

```bash
cat output/accessibility-assessment-parser.log 
```
Sample output from `PageAccessibilityCheck` logger.

```logs
{"app":"page-accessibility-check","hostname":"99a45ab6345c","timestamp":"2021-10-06 11:14:34.800Z",
"message":"Starting to parse accessibility reports for test suite: awesome-tests-a11y-tests",
"logger":"uk.gov.hmrc.a11y.PageAccessibilityCheck$","thread":"main","level":"INFO",
"testsuite":"awesome-tests-a11y-tests","type":"accessibility_logs"}

{"app":"page-accessibility-check","hostname":"99a45ab6345c","timestamp":"2021-10-06 11:14:34.890Z",
"message":"Failed to loaded provided config with exception: com.typesafe.config.ConfigException$IO: /home/seluser/global-filters.conf: java.io.FileNotFoundException: /home/seluser/global-filters.conf (No such file or directory).","logger":"uk.gov.hmrc.a11y.config.Configuration$","thread":"main","level":"ERROR","type":"accessibility_logs","testsuite":"awesome-tests-a11y-tests"}

{"app":"page-accessibility-check","hostname":"99a45ab6345c","timestamp":"2021-10-06 11:14:34.891Z",
"message":"Reverting to use default application.conf","logger":"uk.gov.hmrc.a11y.config.Configuration$",
"thread":"main","level":"WARN","type":"accessibility_logs","testsuite":"awesome-tests-a11y-tests"}

{"app":"page-accessibility-check","hostname":"99a45ab6345c","timestamp":"2021-10-06 11:14:36.556Z",
"message":"Completed running AXE for /home/seluser/pages/0000000002","logger":"uk.gov.hmrc.a11y.tools.Axe",
"thread":"scala-execution-context-global-9","level":"INFO","testsuite":"awesome-tests-a11y-tests","type":"accessibility_logs"}

{"app":"page-accessibility-check","hostname":"99a45ab6345c","timestamp":"2021-10-06 11:14:36.853Z",
"message":"Parsing Axe reports completed for test suite awesome-tests-a11y-tests.","logger":"uk.gov.hmrc.a11y.PageAccessibilityCheck$",
"thread":"main","level":"INFO","type":"accessibility_logs","testsuite":"awesome-tests-a11y-tests"}

{"app":"page-accessibility-check","hostname":"99a45ab6345c","timestamp":"2021-10-06 11:14:37.009Z",
"message":"Completed running VNU for /home/seluser/pages/0000000002","logger":"uk.gov.hmrc.a11y.tools.Vnu",
"thread":"scala-execution-context-global-10","level":"INFO","testsuite":"awesome-tests-a11y-tests","type":"accessibility_logs"}

{"app":"page-accessibility-check","hostname":"99a45ab6345c","timestamp":"2021-10-06 11:14:37.065Z",
"message":"Parsing VNU reports completed for test suite awesome-tests-a11y-tests.","logger":"uk.gov.hmrc.a11y.PageAccessibilityCheck$",
"thread":"main","level":"INFO","type":"accessibility_logs","testsuite":"awesome-tests-a11y-tests"}

{"app":"page-accessibility-check","hostname":"99a45ab6345c","timestamp":"2021-10-06 11:14:37.162Z",
"message":"Total no of violations from all tools: 1","logger":"uk.gov.hmrc.a11y.PageAccessibilityCheck$",
"thread":"main","level":"INFO","type":"accessibility_logs","testsuite":"awesome-tests-a11y-tests"}

{"app":"page-accessibility-check","hostname":"99a45ab6345c","timestamp":"2021-10-06 11:14:37.163Z",
"message":"Finished parsing accessibility reports for test suite: awesome-tests-a11y-tests","logger":"uk.gov.hmrc.a11y.PageAccessibilityCheck$",
"thread":"main","level":"INFO","type":"accessibility_logs","testsuite":"awesome-tests-a11y-tests"}
```

### Raw output from Axe and VNU
The output generated by page-accessibility-check is a normalised output generated from the raw Axe and VNU output.
The raw output from Axe and VNU for each page are made available alongside the captured page. The API
[GET /api/report/bundle](README.md#GET api/report/<type>) returns a zipped file which contains all the captured pages,
and the HTML report. The downloaded zip files includes a folder titled `pages` which contains the capture page and 
the raw `vnu-report.json` and `axe-report.json`

This can be viewed from within the container as well, by using the interactive shell.

```bash
#executes an interactive bash shell on the container.
docker exec -it a11y /bin/bash
ls pages 
```
The `pages` directory contain all the pages captured during the assessment in a folder named after the captured timestamp.
Each of these folders include the raw `vnu-report.json` and `axe-report.json` for that page.

## Kibana Integration
### Running up an ELK stack locally

>Integration with Kibana is not an actively supported feature at present. See `PLATUI-1228`. The below steps can be used
for testing Kibana integration locally when any active work is planned.

You will need to have ~6GB of memory allocated to your local docker engine to run the ELK stack.  If you're running Docker Desktop, you can configure this in **Docker -> Preferences**

You will also need to have docker-compose installed (tested on Mac OSX with v1.25.2).

Simply navigate to the [elk](test/elk) directory and execute the command: `docker-compose up -d`

ElasticSearch and Kibana will start on the default ports of 9600 and 5601 respectively.  You will be able to see Kibana in your browser if you navigate to http://localhost:5601.  Note that it may take a minute to initialise.

To kill the stack, execute `docker-compose kill` from the [elk](test/elk) directory.  If you wish to delete all of the data ingested by elk, then delete the esdata directory `rm -r test/elk/esdata/*`

### Visualising Violations in Kibana
Visualisations for the local Kibana instance can be loaded manually using Kibana's Saved Object import UI.  If this is
the first time you've run the `docker-compose` command in the previous section, then please follow the below instructions
to generate the visualisations you'll need to review the results of the assessment:

1. When the log ingestion is successful, Kibana will show the option to create an Index at **Management -> Index Patterns**.
   The index pattern could be set to `logstash-*` and set the *time filter* field to `testRun` when prompted.
2. The exported accessibility dashboards from Management Kibana environment is available [here](https://github.com/hmrc/management-kibana-dashboards/tree/main/saved-objects/management). The current
   local ELK stack setup does not support `.raw` type used in Kibana Management. It supports only type `.keyword`. Hence,
   before importing these objects replace all references to `.raw` type in the exported saved object with `.keyword`
3. Import the modified saved objects by navigating to **Management -> Saved Ojbects** and click **Import**.
4. When prompted to choose an index pattern, select the index pattern created above in step 1
5. Upon successful import, the dashboards can be found under **Dashboard** section
