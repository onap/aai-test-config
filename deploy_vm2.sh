#!/bin/bash

if [ -f "/opt/docker/docker-compose" ];
then
    DOCKER_COMPOSE_CMD="/opt/docker/docker-compose -f docker-compose-db.yml"
else
    DOCKER_COMPOSE_CMD="docker-compose -f docker-compose-db.yml"
fi

export RESOURCES_LOGS="/opt/aai/logroot/AAI-RESOURCES";
export TRAVERSAL_LOGS="/opt/aai/logroot/AAI-TRAVERSAL";

if [ ! -d "$RESOURCES_LOGS" ];
then
    echo "Warning: Unable to find the volume directory $RESOURCES_LOGS so creating it as regular directory";
    mkdir -p $RESOURCES_LOGS;
fi;

if [ ! -d "$TRAVERSAL_LOGS" ];
then
    echo "Warning: Unable to find the volume directory $TRAVERSAL_LOGS so creating it as regular directory";
    mkdir -p $TRAVERSAL_LOGS;
fi;

export MTU=${MTU:-1500};
export HBASE_IMAGE="${HBASE_IMAGE:-wc9368/aai-hbase-1.2.3}";
#export HBASE_IMAGE="${HBASE_IMAGE:-harisekhon/hbase}";
export GREMLIN_SERVER_IMAGE="${GREMLIN_SERVER_IMAGE:-gremlin-server}";

function wait_for_container() {

    CONTAINER_NAME="$1";
    START_TEXT="$2";

    TIMEOUT=160

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
        let TIMEOUT-=1
        sleep 1
    done
}

# cleanup
$DOCKER_COMPOSE_CMD stop
$DOCKER_COMPOSE_CMD rm -f -v

HBASE_CONTAINER_NAME=$($DOCKER_COMPOSE_CMD up -d aai.hbase.simpledemo.openecomp.org 2>&1 | grep 'Creating' | grep -v 'network' | awk '{ print $2; }' | head -1);
wait_for_container $HBASE_CONTAINER_NAME '^starting regionserver';
#wait_for_container $HBASE_CONTAINER_NAME 'HBase metrics system started';

GREMLIN_CONTAINER_NAME=$($DOCKER_COMPOSE_CMD up -d aai.gremlinserver.simpledemo.openecomp.org 2>&1 | grep 'Creating' | awk '{ print $2; }' | head -1);
wait_for_container $GREMLIN_CONTAINER_NAME 'Channel started at port 8182';

