#!/usr/bin/env bash

set -e

echo "Checking Ollama..."
ollama list >/dev/null

echo "Starting AutoMinutes backend with Ollama profile..."
./mvnw spring-boot:run -Dspring-boot.run.profiles=ollama