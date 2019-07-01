
# Spike: Automating accessibility testing in the build
This project contains the output of the investigation conducted by Platform Test into automating accessibility testing in the build.

## Capturing pages

### Start the server
Start the page capturing service from the `./server` directory with:
```bash
node server.js
```
The server will start on port 6001 (as we allow access to localhost:6001 from our build slave side car containers).

### Executing the tests
For the purpose of the spike, test suites were run using the `local-build-emulator` which, as the name suggests, emulates the containerisation of our jenkins build slaves using a combination of minikube and docker-compose...

To capture pages during a test run without the need to update test code (one of the primary goals of the spike), we created a simple chrome extension and baked our own selenium-standalone-chrome image which had the plugin present. We also setup a man in the middle proxy running on localhost:4444 (the default localtion of webdriver hub) which captures webdriver session creation requests and re-writes them to include the `--load-extension=<our_extension>` chrome command line option.

With this in place, as the tests execute a page's html and css is captured, bundled and POSTed to the node server service running on localhost:6001.

### Filter for unique pages by path
Given there is a lot of duplication in our tests (the nature of automating user journey tests) we made an assumption that only 1 instance of each page by path need be captured and assessed by the tools.  We may choose to change this in the future, but for the purpose of the spike it reduces the noise by around a factor of 10.

Pages are filter using a basic bash script that references the `server.log` file for page url and timestamp, and the `output` directory for the page html and css. cd to `./server` and edit the `identifyUniqueOutputFoldersByUrl.sh` script - we didn't parametise this script so you'll need to update the line that reads into the array of **uniqueUrls**.

When run, the script will copy the page directories from `./output` to the `./<uniquePageDirectory>` folder.  A `data` file is also generated at this point which only contains the pageUrl.  This value is included in each alert generated by the parser.

## Accessibility Assessment
Once the pages from a test run have been captured and filtered, copy the directory containing the filtered pages from `./server/` to `./pages/` then run the `./assessAllPages.sh` script.

For example, to assess all of the pages within the `./pages/trusts-ui-tests` directory, execute `./assessAllPages.sh trusts-unique-pages`.  All folders in the `./pages/trusts-ui-tests` directory will have their `index.html` file assessed by:
- axe
- pa11y; and
- nu html validator

Reports will be stored in the pages' folder alongside the **index.html**

### Test Pages (for the spike)
To properly test the value of each tool we capture unique pages (by path) visited by the following acceptance test suites:
1. trusts-ui-tests
1. business-multi-factor-authentication-acceptance-tests
1. passcode-acceptance-tests
1. personal-details-validation-acceptance-tests
1. business-tax-account-acceptance-tests

Other candidates (passing in jenkins, small sm profiles) if we need to increase the amount of test data:
1. add-taxes-acceptance-tests
1. business-rates-challenge-ui-tests
1. cest-ui-sub-optimised-tests
1. common-transit-convention-ui-acceptance-tests
1. fset-faststream-admin-acceptance-tests

## Visualising the alerts
The tools (axe, pa11y and vny) generate a **LOT** of noise.  So we needed an effective way of visualising each tools' output.  We decided to normalise the tool's output into a collection of "alerts", store these records in elasticsearch, then visualise the alerts with Kibana.  This might not be the optimal approach to visualising the alerts generated by each test suite, however these are all technologies that we're familiar with that are in use on the platform...

Each alert stored in ElasticSearch also contains a link back to the page that was assessed, which is served via a separate nginx instance on http://localhost:6010

### Getting up and running locally
To start elasticsearch, kibana and the http server (nginx) execute: `./start-apps.sh` from the root directory.

To generate an file for bulk uploading alerts to ElasticSearch, execute `./generate-alert-data.sh` from the root directory.  The files (meta data and a11y tool report files) for each page captured in the `./pages` directory will be parsed, and alert records will be appended to a file named something like `report-1560872696` in the root directory.

To load the data using the json schema detailed below, run `./load-alert-data.sh report-<epoch>`

In the [Kibana UI](http://localhost:5601/app/kibana#/management/kibana/objects?_g=()), go to **Management -> Saved Objects** and click **Import**.  Select the `apps/kibana-saved-objects.json` file to import some basic dashboards.

### Accessibility Alert json schema
The each violation found will be stored as the below json object:
```json
{
    "tool": "String",
    "test_suite": "String",
    "test_run": "String",
    "path": "String",
    "timestamp": INT,
    "code": "String",
    "severity": "String",
    "description": "String",
    "selector": "String",
    "snippet": "String"
}
```
**tool**: the tool the violation was found with.  i.e. aXe, vnu or pa11y

**test_suite**: the test suite executed.  i.e. trusts-ui-test

**test_run**: lowest timestamp/folder value generated during the test, converted to the date format 'yyyy-MM-dd_HH:mm:ss'

**path**: the path part of the url stored in each page directory's `data` file.  i.e. `/trusts-registration/trust-registered-online`

**timestamp**: the timestamp integer used to name the directory

The following fields will be taken from the json reports generated by each tool:
**code**:
- pa11y -> code
- vnu -> there is no unique code for the issues raised. (!)
- aXe -> node.any[].id

**severity**:
- pa11y -> type
- vnu -> type
- aXe -> node.any[].impact

**description**:
- pa11y -> message
- vnu -> message
- aXe -> node.any[].message

**selector**:
- pa11y -> selector
- vnu -> there is no selector given in the vnu report (!)
- aXe -> node.any[].data{}

**snippet**:
- pa11y -> context
- vnu -> extract
- aXe -> nodes[].html


*Note:  In axe, capture only violations and incomplete scans, prefix incomplete scans with INCOMPLETE*

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
