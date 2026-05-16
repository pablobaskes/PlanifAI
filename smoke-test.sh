#!/bin/bash

set -e
set -o pipefail

SERVICES=("config-server" "eureka-server" "api-gateway" "auth-service")

declare -A PORTS=(
  ["config-server"]=8888
  ["eureka-server"]=8761
  ["api-gateway"]=8080
  ["auth-service"]=8081
)

echo "🚀 Iniciando smoke test..."

for SERVICE in "${SERVICES[@]}"; do
  PORT=${PORTS[$SERVICE]}
  echo "🔎 Comprobando $SERVICE en puerto $PORT..."

  # 1. Health check
  HEALTH=$(curl -s "http://localhost:$PORT/actuator/health" | jq -r .status || echo "DOWN")
  if [[ "$HEALTH" == "UP" ]]; then
    echo "✅ $SERVICE responde /actuator/health con UP"
  else
    echo "❌ $SERVICE no responde correctamente en /actuator/health"
    exit 1
  fi

  # 2. Validar configuración desde Config Server
  if [[ "$SERVICE" != "config-server" && "$SERVICE" != "eureka-server" ]]; then
    CONF=$(curl -s "http://localhost:8888/$SERVICE/dev" | jq -r .name || echo "none")
    if [[ "$CONF" == "$SERVICE" ]]; then
      echo "✅ $SERVICE obtiene configuración del Config Server"
    else
      echo "❌ $SERVICE NO carga config del Config Server"
      exit 1
    fi
  fi
done

echo "🎉 Todos los microservicios están arriba y configurados correctamente."
