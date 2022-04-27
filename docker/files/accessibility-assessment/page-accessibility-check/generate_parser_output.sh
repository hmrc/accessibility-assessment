#!/usr/bin/env bash

TESTSUITE_NAME=address-lookup-acceptance-tests

sbt -Dtest.suite.name=$TESTSUITE_NAME \
    -Dtest.suite.file.location="$PWD/page-accessibility-check/src/test/resources/it/$TESTSUITE_NAME" \
    -Dtest.suite.artefact.location="$PWD/page-accessibility-check/src/test/resources/it/$TESTSUITE_NAME" \
    -Dtest.suite.build.url="https://build.tax.service.gov.uk/job/PlatOps/job/Examples/job/platops-example-a11y-check-with-all-pages-captured/4/" \
    -Dconfig.file="$PWD/page-accessibility-check/src/main/resources/application.conf" \
    "pagecheck/run"
