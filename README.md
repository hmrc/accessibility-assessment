# *accessibility-assessment*
The accessibility-assessment tool is published as a docker image, and runs as a sidecar container to our jenkins agents.  It exposes a REST API for capturing complete web pages (HTML, js, css) which are then assessed with [axe](https://www.deque.com/axe/) and [Nu HTML Checker](https://validator.github.io/validator/).  
The service then creates a [basic HTML report](docs/READING-THE-REPORT.md) of the violations found which is archived in Jenkins, and in our Management instance of Kibana.

### Table of Contents
* [Accessibility Assessment Service Components](#accessibility-assessment-service-components)
* [Running accessibility-assessment tests locally](#running-accessibility-assessment-tests-locally)
* [Building the Image in CI](#building-the-image-in-ci)
* [Development](#development)

# Accessibility Assessment Service Components

At present the image is made up of the following components:
- **accessibility-assessment-service**
- **page-accessibility-check**

## accessibility-assessment service
A [node express](https://expressjs.com/) service exposing endpoints for orchestrating page assessment. The service 
is available at http://localhost:6010.

### Endpoints
Below is the list of endpoints exposed by the accessibility-assessment-service. The example request and responses are 
generated using Postman. See [Postman collection](#Postman Collection) section for testing the service with Postman.   

#### GET /api/status
Returns the status of the accessibility-assessment container. The status can be one of: 
- READY 
- PAGES_CAPTURED 
- REPORT_READY 
- PAGE_ASSESSMENT_FAILED 
- ASSESSING_PAGES

**Request:**
```
GET /api/status HTTP/1.1
Host: localhost:6010
```
**RESPONSE:**
```
HTTP/1.1 200 OK
X-Powered-By: Express
Content-Type: text/html; charset=utf-8
Content-Length: 5
ETag: W/"5-/FVNArjFa9Xm5QWjlKISD/c6L7A"
Date: Wed, 25 Nov 2020 11:27:48 GMT
Connection: keep-alive

READY
```

#### POST /api/app/initialise
Initialises the assessment with the test suite name and Build Url. The generated report uses this information to link 
back to the captured HTML page.

**Request:**
```
POST /api/app/initialise HTTP/1.1
Host: localhost:6010
Content-Type: application/json

{
    "testSuite": "awesome-tests-a11y-tests",
    "buildUrl": "https://build.tax.service.gov.uk/job/ACE/job/awesome-tests-a11y-tests/19/"
}
```
**Response:**
```
HTTP/1.1 200 OK
X-Powered-By →Express
Content-Type →application/json; charset=utf-8
Content-Length →155
ETag →W/"9b-Zi4QVQl7/I0LAEH2xvGQWqLdMWE"
Date →Wed, 25 Nov 2020 11:37:30 GMT
Connection →keep-alive

{
    "applicationStatus": "READY",
    "testSuite": "awesome-tests-a11y-tests",
    "buildUrl": "https://build.tax.service.gov.uk/job/ACE/job/awesome-tests-a11y-tests/19/"
}
```

#### POST /api/app/global-filters
Accepts a config file containing filters which is used by the page-accessibility-check.
 The config should adhere to the [lightbend/config](https://github.com/lightbend/config) format. Refer to 
 [page-accessibility-check](https://github.com/hmrc/page-accessibility-check#global-filters) for more details on this config.     

**Request:**
```
POST /api/app/global-filters HTTP/1.1
Host: localhost:6010
Content-Type: multipart/form-data;
Content-Disposition: form-data; 
name="filter"; 
filename="application.conf
```

**Response:**
```
HTTP/1.1 201 Created
X-Powered-By →Express
Date →Wed, 25 Nov 2020 11:49:39 GMT
Connection →keep-alive
Content-Length →0
```

#### POST /api/capture-page
Used by the [page-capture-chrome-extension](https://github.com/hmrc/page-capture-chrome-extension/blob/main/page-capture/content.js)
 to post the HTML page and assets (excluding images) for assessment.
 
##### Criteria for capturing pages:
Capture the page for assessment if:
  - the page hasn't already been captured (Duplicate pages are captured only if `captureAllPages` [config](app/config.js) is true)
  - the page URL does not contain the text `stub` in the path
  - the page URL does not contain the text `test-only` in the path
  - the page contains valid HTML tags (This is to avoid capturing any non-html pages accessed during the test)
See [capturePage.js](app/routes/capturePage.js) for more details.

**Request:**
**Request without Errors:**
```
POST /api/capture-page HTTP/1.1
Host: localhost:6010
Content-Type: application/json

{
    "pageURL": "http://localhost:1234/simple/page/capture",
    "pageHTML": "<html><head><title>Some title</title></head><main>The contents of the page</main></html>",
    "timestamp": "0000000002",
    "files": { "file1": "some contents" } 
}
```
_pageURL_: The actual URL of the page under assessment. This is written into a file called `data` which is used in the report.   
_pageHTML_: The captured page's HTML which is saved as `index.html`  
_timestamp_: The time when the page was captured. Used as folder name for each captured paged under which the relevant files are saved.  
_files_: Assets for the page. No images are captured by the page-capture-chrome-extension.   

**Response:**
```
HTTP/1.1 201 Created
X-Powered-By →Express
Date →Wed, 25 Nov 2020 15:54:05 GMT
Connection →keep-alive
Content-Length →0
```
When the page-capture-chrome-extension failed to download the assets for a captured page, the extension passes this information 
to the service using the `errors` field. The service then surfaces this information in `/api/logs/urls` for teams to investigate failed jobs.

**Request with Error Information:**
```
POST /api/capture-page HTTP/1.1
Host: localhost:6010
Content-Type: application/json

{
    "pageURL": "http://localhost:1234/path/to/page/with/lots/of/errors",
    "pageHTML": "<html><head><title>Some title</title></head><main>The contents of the page</main></html>",
    "timestamp": "0000000002",
    "files": {"file1":"some contents"},
    "errors":  [ { "failedUrl": "/pay/assets/js/monitoring.ga-events.js",
    				"message": "Request to the URL failed to return a 2XX response",
    				"statusReceived": 404 },
    				{ "failedUrl": "/pay/assets/js/another.js",
    				"message": "Request to the URL failed to return a 2XX response",
    				"statusReceived": 404 }]
}
```
**Response:**
```
HTTP/1.1 201 Created
X-Powered-By →Express
Date →Wed, 25 Nov 2020 15:54:05 GMT
Connection →keep-alive
Content-Length →0
```
#### POST /api/assess-pages
Triggers the assessment on the pages captured using `/api/capture-page`. This POST request does not take a body.  
If the assessment is completed, an HTML report is generated which then sets the status to `REPORT_READY`.  
If the assessment failed to complete for any reason, the status is set to `PAGE_ASSESSMENT_FAILED`.   

**Request**
```
POST /api/assess-pages HTTP/1.1
Host: localhost:6010
```
**Response:**
```
HTTP/1.1 202 Accepted
X-Powered-By →Express
Content-Type →application/json; charset=utf-8
Content-Length →40
ETag →W/"28-7mB1WmtoHr1duk7uGpg2HLtp1nw"
Date →Wed, 25 Nov 2020 16:01:40 GMT
Connection →keep-alive
{
    "message": "Page assessment triggered."
}
```

#### GET /api/logs/urls
Returns URLs captured for assessment, URLs excluded from the assessment and any URLs listed in the `errors` field of the
`/api/capture-page` endpoint.

**Request**
```
GET /api/logs/urls HTTP/1.1
Host: localhost:6010
cache-control: no-cache
```
**Response:**
```
HTTP/1.1 200 OK
X-Powered-By →Express
Content-Type →application/json; charset=utf-8
Content-Length →366
ETag →W/"16e-CX7a1vkmgCs6t1IxPHE/SzKg3gU"
Date →Wed, 25 Nov 2020 16:05:37 GMT
Connection →keep-alive

{
    "capturedUrls": [
        "http://localhost:1234/path/to/page/with/lots/of/errors",
        "http://localhost:1234/simple/page/capture",
        null
    ],
    "excludedUrls": [],
    "errors": [
        {
            "url": "http://localhost:1234/path/to/page/with/lots/of/errors",
            "path": "/pay/assets/js/monitoring.ga-events.js"
        },
        {
            "url": "http://localhost:1234/path/to/page/with/lots/of/errors",
            "path": "/pay/assets/js/another.js"
        }
    ]
}
```

#### GET api/logs/app
Provides the service logs of the accessibility-assessment-service. This does not include logs from page-accessibility-check.

**Request:**
```
GET /api/logs/app HTTP/1.1
Host: localhost:6010
```

**Response:**
```
HTTP/1.1 200 OK
X-Powered-By →Express
Date →Wed, 25 Nov 2020 16:31:28 GMT
Connection →keep-alive
Transfer-Encoding →chunked

{"level": "INFO", "message": "Accessibility assessment service running on port 6010", "type": "accessibility_logs", "app": "accessibility-assessment-service", "testSuite": "not-set"}
{"level": "INFO", "message": "Returning Accessibility assessment service status:READY", "type": "accessibility_logs", "app": "accessibility-assessment-service", "testSuite": "not-set"}
```
 
#### GET api/report/<type>
Provides the accessibility assessment report. The valid types are:
- `json` - Returns a JSON report which is used by the [htmlReport.js](app/services/htmlReport.js) to generate a HTML report 
- `html` - Returns an HTML report
- `csv`  - Returns the report in CSV format
- `bundle` - Returns a zipped file which contains all the captured pages, and the HTML report

> Guidance on how to understand the generated HTML report can be found [here](docs/READING-THE-REPORT.md).

#### POST api/app/reset
An endpoint to reset the app by removing the captured pages and any previously generated reports. Used in 
[accessibility-assessment-tests](https://github.com/hmrc/accessibility-assessment-tests) for resetting the service 
in between tests.  

**Request:**
```
POST /api/app/reset HTTP/1.1
Host: localhost:6010
Content-Type: application/json
```

**Response:**
```
HTTP/1.1 200 OK
X-Powered-By →Express
Date →Wed, 25 Nov 2020 16:41:27 GMT
Connection →keep-alive
Content-Length →0
```

## page-accessibility-check
A jar published by [page-accessibility-check](https://github.com/hmrc/page-accessibility-check) which wraps execution of axe
 and vnu assessments against a collection of pages. accessibility-assessment-service triggers this jar to run the assessment
  once pages have been captured.


## Running accessibility-assessment tests locally
To run accessibility-assessment tests locally, you can use the latest available version of the accessibility-assessment Docker image from Artifactory. 
This is also the version used in Build Jenkins.

> ### Prerequisite:
>In order to run the accessibility-assessment tests locally you will need to ensure:
>1. webdriver-factory 0.20.0 or later is used within your UI journey test repository dependencies
>2. You have started the services that should be tested

### Running the tests:
1. Run locally the latest available version of the accessibility-assessment Docker image from Artifactory:
```
   A11Y='artefacts.tax.service.gov.uk/accessibility-assessment:latest'
   docker pull ${A11Y} && docker run --rm --name 'a11y' -p 6010:6010 -e TARGET_IP='host.docker.internal' ${A11Y}
```

2. In order to configure the accessibility tests to run you will need to pass a 
   system property `accessibility.test=true`.

e.g. `sbt -Dbrowser='chrome' -Denvironment='local' -Daccessibility.test=true 'testOnly uk.gov.hmrc.test.ui.specs.*'`

3. Next, [trigger the assessment endpoint](#post-apiassess-pages).
   This will assess the pages which have been captured. 
   
   Depending on the number of pages being assessed this can take a little while.
   You can find out the status of the accessibility-assessment at anytime by [querying the status endpoint](#get-apistatus).


4. Finally, [get the report](#get-apireport) to see any violations which have been found.
5. See [troubleshooting accessibility-assessment locally](DEVELOPMENT.md#troubleshooting-accessibility-assessment-locally) 
   section for any issues with running the assessment locally. 

# Building the Image in CI
The [Makefile](Makefile) at the root of this project is used by Jenkins to build and publish new versions of this image to artefactory for use in CI.

# Development
See [DEVELOPMENT.md](DEVELOPMENT.md) for guidelines to make changes to accessibility-assessment. 

# License
This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
