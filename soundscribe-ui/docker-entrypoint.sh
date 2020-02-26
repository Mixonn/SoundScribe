#!/usr/bin/env sh
set -eu

echo "dupa"
envsub --env HOST_NAME /app/.env.template /app/.env

exec "$@"
