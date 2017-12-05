#!/bin/bash

if [ -f "/opt/docker/docker-compose" ];
then
    DOCKER_COMPOSE_CMD="/opt/docker/docker-compose -f docker-compose-db.yml"
else
    DOCKER_COMPOSE_CMD="docker-compose -f docker-compose-db.yml"
fi

export MTU=$(/sbin/ifconfig | grep MTU | sed 's/.*MTU://' | sed 's/ .*//' | sort -n | head -1);
export HBASE_IMAGE="${HBASE_IMAGE:-aaionap/hbase}";
export HBASE_VERSION="${HBASE_VERSION:-1.2.0}";

function wait_for_container() {

    CONTAINER_NAME="$1";
    START_TEXT="$2";

    TIMEOUT=600

    # wait for the real startup
    AMOUNT_STARTUP=$(docker logs ${CONTAINER_NAME} 2>&1 | grep "$START_TEXT" | wc -l)
    while [[ ${AMOUNT_STARTUP} -ne 1 ]];
    do
        echo "Waiting for '$CONTAINER_NAME' deployment to finish ..."
        AMOUNT_STARTUP=$(docker logs ${CONTAINER_NAME} 2>&1 | grep "$START_TEXT" | wc -l)
        if [ "$TIMEOUT" = "0" ];
        then
            echo "ERROR: $CONTAINER_NAME deployment failed."
            exit 1
        fi
        let TIMEOUT-=5
        sleep 5
    done
}

docker pull ${HBASE_IMAGE}:${HBASE_VERSION};

# cleanup
$DOCKER_COMPOSE_CMD stop
$DOCKER_COMPOSE_CMD rm -f -v

HBASE_CONTAINER_NAME=$($DOCKER_COMPOSE_CMD up -d aai.hbase.simpledemo.onap.org 2>&1 | grep 'Creating' | grep -v 'volume' | grep -v 'network' | awk '{ print $2; }' | head -1);
wait_for_container $HBASE_CONTAINER_NAME ' Started SelectChannelConnector@0.0.0.0:8085';
wait_for_container $HBASE_CONTAINER_NAME ' Started SelectChannelConnector@0.0.0.0:8080';
wait_for_container $HBASE_CONTAINER_NAME ' Started SelectChannelConnector@0.0.0.0:9095';

$DOCKER_COMPOSE_CMD up -d aai.elasticsearch.simpledemo.openecomp.org
