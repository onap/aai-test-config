#!/bin/bash

# run short-living command and prevent docker from stopping

JANUS_EXEC="janusgraph/bin/janusgraph.sh"

onStart() {
  ${JANUS_EXEC} start
}

onStop() {
  ${JANUS_EXEC} stop
}

waitLoop() {
  tail -f /dev/null &
  wait $!
}

trap 'onStop; exit 0' SIGTERM SIGINT

onStart || exit $?

waitLoop