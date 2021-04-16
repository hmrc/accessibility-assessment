#!/bin/bash -e

echo "INFO: Stopping a11y container if already running"
docker stop a11y || true

echo "INFO: Building accessibility-assessment:SNAPSHOT"
./scripts/build-local-image.sh

echo "INFO: Running accessibility-assessment:SNAPSHOT in background"
./scripts/run-local.sh

# Checks if accessibility-assessment is alive and can accept requests
check_accessibility_assessment_service_running () {
  max_iterations=5
  iterations=0
  while true
  do
    ((iterations++))
    echo "INFO: Checking if accessibility-assessment is alive (attempt $iterations of 5)"
    sleep 5

    status=$(curl -sS  -I http://localhost:6010/api/status  2> /dev/null | head -n 1 | cut -d' ' -f2)

    if [ "$status" = "200" ]; then
      echo "INFO: Request to accessibility-assessment /api/status endpoint returned 200"
		  break
	  fi

    if [ "$iterations" -ge "$max_iterations" ]; then
      echo "INFO: Timeout - could not make request to accessibility-assessment service"
      exit 1
    fi
  done
}
check_accessibility_assessment_service_running

echo "INFO: Navigating to accessibility-assessment tests directory"
cd tests/

echo "INFO: Running API specs"
sbt clean "testOnly uk.gov.hmrc.aat.api.*"