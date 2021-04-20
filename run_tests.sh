#!/bin/bash -e

echo "INFO: Building accessibility-assessment:SNAPSHOT"
PROJECT_DIR=${WORKSPACE}/accessibility-assessment
DOCKER_FILES_DIR=${PROJECT_DIR}/docker/files

# cp the latest app/ into files
cp -r app docker/files/

docker build -t accessibility-assessment:SNAPSHOT ${PROJECT_DIR}/docker

echo "INFO: Running accessibility-assessment:SNAPSHOT in background"
PROJECT_DIR=${WORKSPACE}/accessibility-assessment

docker run -d \
    --name a11y \
    -p 6010:6010 \
    accessibility-assessment:SNAPSHOT

echo "INFO: Navigating to accessibility-assessment tests directory"
cd tests/

echo "INFO: Running API specs"
sbt clean "testOnly uk.gov.hmrc.aat.api.*"