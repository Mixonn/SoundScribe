#!/usr/bin/env sh
set -eu

envsubst '${LISTEN_HOST} ${LISTEN_PORT}' < /etc/nginx/proxy.conf.template > /etc/nginx/nginx.conf

exec "$@"
