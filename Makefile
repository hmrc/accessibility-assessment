SHELL := /usr/bin/env bash
PYTHON_VERSION := $(shell cat .python-version)
ACCESSIBILITY_ASSESSMENT_BASE = docker/files/accessibility-assessment
ACCESSIBILITY_ASSESSMENT_OUTPUT = docker/files/accessibility-assessment/output

.PHONY: check_docker copy_files clean_local build_local run_local stop_local build authenticate_to_artifactory push_image prep_version_incrementor clean help compose
.DEFAULT_GOAL := help

make_dirs:
	$(shell mkdir -p $(ACCESSIBILITY_ASSESSMENT_BASE))
	$(shell mkdir -p $(ACCESSIBILITY_ASSESSMENT_OUTPUT))

copy_files: make_dirs ## Copies files required for building image
	@cp -r ./page-accessibility-check docker/files/accessibility-assessment/page-accessibility-check
	@cp -r accessibility-assessment-service docker/files/accessibility-assessment/accessibility-assessment-service
	@cp -r project docker/files/accessibility-assessment/project
	@cp -r build.sbt docker/files/accessibility-assessment/
	@cp -r repository.yaml docker/files/accessibility-assessment/
	@cp -r LICENSE docker/files/accessibility-assessment/
	@cp -r package.json docker/files/accessibility-assessment/accessibility-assessment-service/package.json
	@cp -r package-lock.json docker/files/accessibility-assessment/accessibility-assessment-service/package-lock.json
	@cp -r .npmrc docker/files/accessibility-assessment/accessibility-assessment-service/.npmrc

clean_local: stop_local ## Clean up local environment
	@echo '********** Cleaning local docker environment ************'
	@docker rmi -f accessibility-assessment:SNAPSHOT
	@rm -rf docker/files/accessibility-assessment

build_local: clean_local copy_files ## Builds the accessibility-assessment image locally
	@echo '********** Building docker image for local use ************'
	@docker build --progress=plain --no-cache --tag accessibility-assessment:SNAPSHOT docker \
 	|| (echo "Build failed. Removing files used for building"; rm -rf docker/files/accessibility-assessment/accessibility-assessment-service; exit 1)
	@echo '********** Image Built. Removing files used for building image ************'
	@rm -rf docker/files/accessibility-assessment

run_local: build_local ## Builds and runs the accessibility-assessment container locally
	@echo '********** Starting a11y container for local use ************'
	@docker run -d --rm --name a11y -p 6010:6010 accessibility-assessment:SNAPSHOT

stop_local: ## Stops the a11y container
	@echo '********** Stopping a11y container running locally ************'
	@docker stop a11y || (echo "a11y container not running. Nothing to stop."; exit 0)

build: copy_files prep_version_incrementor ## Build the docker image
	@echo '********** Building docker image ************'
	@pipenv run prepare-release
	@umask 0022
	@docker build --no-cache --tag artefacts.tax.service.gov.uk/accessibility-assessment:$$(cat .version) docker

authenticate_to_artifactory:
	@docker login --username ${ARTIFACTORY_USERNAME} --password "${ARTIFACTORY_PASSWORD}"  artefacts.tax.service.gov.uk

push_image: ## Push the docker image to artifactory
	@docker push artefacts.tax.service.gov.uk/accessibility-assessment:$$(cat .version)
	@pipenv run cut-release

push_labs: ## Push latest tag to artifactory
	@docker tag artefacts.tax.service.gov.uk/accessibility-assessment:$$(cat .version) artefacts.tax.service.gov.uk/accessibility-assessment:labs
	@docker push artefacts.tax.service.gov.uk/accessibility-assessment:labs

push_experimental_tag: ## Push latest experimental tag to artifactory
	@docker tag artefacts.tax.service.gov.uk/accessibility-assessment:$$(cat .version) artefacts.tax.service.gov.uk/accessibility-assessment:experimental
	@docker push artefacts.tax.service.gov.uk/accessibility-assessment:experimental

prep_version_incrementor:
	@echo "Renaming requirements to prevent pipenv trying to convert it"
	@echo "Installing version-incrementor with pipenv"
	@pip install pipenv --upgrade
	@pipenv --python $(PYTHON_VERSION)
	@pipenv run pip install -i https://artefacts.tax.service.gov.uk/artifactory/api/pypi/pips/simple 'version-incrementor<2'

clean: ## Remove the docker image
	@echo '********** Cleaning up ************'
	@docker rmi -f $$(docker images artefacts.tax.service.gov.uk/accessibility-assessment:$$(cat .version) -q)

help:
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'
