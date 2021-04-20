#!/bin/bash -e

echo "INFO: Copying the latest app/ into files"
cp -r app docker/files/

echo "INFO: Building accessibility-assessment:SNAPSHOT"
docker build -t accessibility-assessment:SNAPSHOT ${WORKSPACE}/docker

echo "INFO: Running accessibility-assessment:SNAPSHOT in background"
docker run -d \
    --name a11y \
    -p 6010:6010 \
    accessibility-assessment:SNAPSHOT

echo "INFO: Navigating to accessibility-assessment tests directory"
cd tests/

echo "INFO: Running API specs"
sbt clean "testOnly uk.gov.hmrc.aat.api.*"