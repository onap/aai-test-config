#!/bin/bash

if [ -f "/opt/docker/docker-compose" ];
then
    DOCKER_COMPOSE_CMD="/opt/docker/docker-compose -f docker-compose-app.yml"
else
    DOCKER_COMPOSE_CMD="docker-compose -f docker-compose-app.yml"
fi

export RESOURCES_LOGS="/opt/aai/logroot/AAI-RESOURCES";
export TRAVERSAL_LOGS="/opt/aai/logroot/AAI-TRAVERSAL";
export SEARCH_LOGS="/opt/aai/logroot/AAI-SEARCH";
export DATA-ROUTER_LOGS="/opt/aai/logroot/AAI-DATA-ROUTER";
export MODEL-LOADER_LOGS="/opt/aai/logroot/AAI-MODEL-LOADER";

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

if [ ! -d "$SEARCH_LOGS" ];
then
    echo "Warning: Unable to find the volume directory $SEARCH_LOGS so creating it as regular directory";
    mkdir -p $SEARCH_LOGS;
fi;

if [ ! -d "$DATA-ROUTER_LOGS" ];
then
    echo "Warning: Unable to find the volume directory $DATA-ROUTER_LOGS so creating it as regular directory";
    mkdir -p $DATA-ROUTER_LOGS;
fi;

if [ ! -d "$MODEL-LOADER_LOGS" ];
then
    echo "Warning: Unable to find the volume directory $MODEL-LOADER_LOGS so creating it as regular directory";
    mkdir -p $MODEL-LOADER_LOGS;
fi;

export MTU=${MTU:-1500};
export DOCKER_REGISTRY="${DOCKER_REGISTRY:-localhost:5000}";
export HBASE_IMAGE="${HBASE_IMAGE:-wc9368/aai-hbase-1.2.3}";
export GREMLIN_SERVER_IMAGE="${GREMLIN_SERVER_IMAGE:-gremlin-server}";
export AAI_HAPROXY_IMAGE="${AAI_HAPROXY_IMAGE:-aai-haproxy}";

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

USER_ID=$(docker run -it --rm --entrypoint=id $DOCKER_REGISTRY/openecomp/aai-resources -u | sed 's/[^0-9]//g')
GROUP_ID=$(docker run -it --rm --entrypoint=id $DOCKER_REGISTRY/openecomp/aai-resources -g | sed 's/[^0-9]//g')

chown -R $USER_ID:$GROUP_ID $RESOURCES_LOGS;
chown -R $USER_ID:$GROUP_ID $TRAVERSAL_LOGS;

RESOURCES_CONTAINER_NAME=$($DOCKER_COMPOSE_CMD up -d aai-resources.api.simpledemo.openecomp.org 2>&1 | grep 'Creating' | awk '{ print $2; }' | head -1);
wait_for_container $RESOURCES_CONTAINER_NAME '0.0.0.0:8447';

GRAPH_CONTAINER_NAME=$($DOCKER_COMPOSE_CMD up -d aai-traversal.api.simpledemo.openecomp.org 2>&1 | grep 'Creating' | awk '{ print $2; }' | head -1);
wait_for_container $GRAPH_CONTAINER_NAME '0.0.0.0:8446';

# deploy
$DOCKER_COMPOSE_CMD up -d aai.api.simpledemo.openecomp.org

docker exec -it $GRAPH_CONTAINER_NAME "/opt/app/aai-traversal/scripts/install/updateQueryData.sh";
