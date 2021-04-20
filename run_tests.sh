#!/bin/bash -e

echo "INFO: Building accessibility-assessment:SNAPSHOT"
PROJECT_DIR=${WORKSPACE}/accessibility-assessment
DOCKER_FILES_DIR=${PROJECT_DIR}/docker/files

# cp the latest app/ into files

cp ${PROJECT_DIR}/app/*.js  $DOCKER_FILES_DIR/app
cp ${PROJECT_DIR}/app/package.json  $DOCKER_FILES_DIR/app/package.json
cp ${PROJECT_DIR}/app/package-lock.json  $DOCKER_FILES_DIR/app/package-lock.json
cp -r ${PROJECT_DIR}/app/resources $DOCKER_FILES_DIR/app/
cp -r ${PROJECT_DIR}/app/routes $DOCKER_FILES_DIR/app/
cp -r ${PROJECT_DIR}/app/services $DOCKER_FILES_DIR/app/

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