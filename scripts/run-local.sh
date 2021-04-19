#!/bin/bash

################################################################################
# Use this script to run the accessibility-assessment image locally
################################################################################

PROJECT_DIR=${WORKSPACE}/accessibility-assessment
rm -rf ${PROJECT_DIR}/output/*
docker rm a11y

docker run --cpus 3  \
    --name a11y \
    -v ${PROJECT_DIR}/output:/home/seluser/output \
    -p 6010:6010 \
    accessibility-assessment:SNAPSHOT

# For local development, include a mount similar to one of the commented out lines
#    below (which mount various files/folders within the ./app directory)
#    within the image).
#
#    Be aware that if you mount the entire app/ directory you will include the application's
#    ${PROJECT_DIR}/app/node_modules .  This may result in an incompatibility between
#    the node modules required by the image's linux dist, versus those required by your
#    local development environment (i.e. OSX)

# -v ${PROJECT_DIR}/app/routes:/home/seluser/app/routes \
# -v ${PROJECT_DIR}/app/app.js:/home/seluser/app/app.js \
# -v ${PROJECT_DIR}/app/services/globals.js:/home/seluser/app/services/globals.js \
# -v ${PROJECT_DIR}/pages:/home/seluser/pages \