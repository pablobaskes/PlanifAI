#!/bin/bash

set -e
set -o pipefail

SERVICES=("api-gateway" "auth-service")

declare -A PORTS=(
  ["api-gateway"]=8080
  ["auth-service"]=8081
)

echo "Starting PlanifAI runtime smoke test..."

for SERVICE in "${SERVICES[@]}"; do
  PORT=${PORTS[$SERVICE]}
  echo "Checking $SERVICE on port $PORT..."

  HEALTH=$(curl -s "http://localhost:$PORT/actuator/health" | jq -r .status || echo "DOWN")
  if [[ "$HEALTH" == "UP" ]]; then
    echo "$SERVICE responds to /actuator/health with UP"
  else
    echo "$SERVICE did not respond correctly to /actuator/health"
    exit 1
  fi
done

echo "Runtime services are up."
