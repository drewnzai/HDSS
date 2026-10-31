#!/usr/bin/env bash

set -a
source .env.local
set +a

mvn spring-boot:run -DskipTests
