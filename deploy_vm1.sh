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
export DATA_ROUTER_LOGS="/opt/aai/logroot/AAI-DATA-ROUTER";
export MODEL_LOADER_LOGS="/opt/aai/logroot/AAI-MODEL-LOADER";
export UI_LOGS="/opt/aai/logroot/AAI-UI";

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

if [ ! -d "$DATA_ROUTER_LOGS" ];
then
    echo "Warning: Unable to find the volume directory $DATA_ROUTER_LOGS so creating it as regular directory";
    mkdir -p $DATA_ROUTER_LOGS;
fi;

if [ ! -d "$MODEL_LOADER_LOGS" ];
then
    echo "Warning: Unable to find the volume directory $MODEL_LOADER_LOGS so creating it as regular directory";
    mkdir -p $MODEL_LOADER_LOGS;
fi;

if [ ! -d "$UI_LOGS" ];
then
    echo "Warning: Unable to find the volume directory $UI_LOGS so creating it as regular directory";
    mkdir -p $UI_LOGS;
fi;

export MTU=$(/sbin/ifconfig | grep MTU | sed 's/.*MTU://' | sed 's/ .*//' | sort -n | head -1);
export DOCKER_REGISTRY="${DOCKER_REGISTRY:-localhost:5000}";
export AAI_HAPROXY_IMAGE="${AAI_HAPROXY_IMAGE:-aaionap/haproxy}";
export AAI_HAPROXY_VERSION="${AAI_HAPROXY_VERSION:-1.1.0}";

NEXUS_USERNAME=$(cat /opt/config/nexus_username.txt)
NEXUS_PASSWD=$(cat /opt/config/nexus_password.txt)
NEXUS_DOCKER_REPO=$(cat /opt/config/nexus_docker_repo.txt)
DMAAP_TOPIC=$(cat /opt/config/dmaap_topic.txt)
DOCKER_IMAGE_VERSION=$(cat /opt/config/docker_version.txt)
DOCKER_REGISTRY=${NEXUS_DOCKER_REPO}

docker login -u $NEXUS_USERNAME -p $NEXUS_PASSWD $NEXUS_DOCKER_REPO

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

function check_if_user_exists(){

        local user_id=$1;

        if [ -z "$user_id" ]; then
                echo "Needs to provide at least one argument for check_if_user_exists func";
                exit 1;
        fi;

        id -u ${user_id} > /dev/null 2>&1 && {
                echo "1";
        } || {
                echo "0";
        }
}

docker pull ${DOCKER_REGISTRY}/openecomp/aai-resources:${DOCKER_IMAGE_VERSION};
docker tag $DOCKER_REGISTRY/openecomp/aai-resources:$DOCKER_IMAGE_VERSION $DOCKER_REGISTRY/openecomp/aai-resources:latest;

docker pull ${DOCKER_REGISTRY}/openecomp/aai-traversal:${DOCKER_IMAGE_VERSION};
docker tag $DOCKER_REGISTRY/openecomp/aai-traversal:$DOCKER_IMAGE_VERSION $DOCKER_REGISTRY/openecomp/aai-traversal:latest;

docker pull ${DOCKER_REGISTRY}/openecomp/search-data-service:${DOCKER_IMAGE_VERSION};
docker tag $DOCKER_REGISTRY/openecomp/search-data-service:$DOCKER_IMAGE_VERSION $DOCKER_REGISTRY/openecomp/search-data-service:latest;

docker pull ${DOCKER_REGISTRY}/openecomp/data-router:${DOCKER_IMAGE_VERSION};
docker tag $DOCKER_REGISTRY/openecomp/data-router:$DOCKER_IMAGE_VERSION $DOCKER_REGISTRY/openecomp/data-router:latest;

docker pull ${DOCKER_REGISTRY}/openecomp/model-loader:${DOCKER_IMAGE_VERSION};
docker tag $DOCKER_REGISTRY/openecomp/model-loader:$DOCKER_IMAGE_VERSION $DOCKER_REGISTRY/openecomp/model-loader:latest;

docker pull ${DOCKER_REGISTRY}/openecomp/sparky-be:${DOCKER_IMAGE_VERSION};
docker tag $DOCKER_REGISTRY/openecomp/sparky-be:$DOCKER_IMAGE_VERSION $DOCKER_REGISTRY/openecomp/sparky-be:latest;

# cleanup
$DOCKER_COMPOSE_CMD stop
$DOCKER_COMPOSE_CMD rm -f -v


USER_EXISTS=$(check_if_user_exists aaiadmin);

if [ "${USER_EXISTS}" -eq 0 ]; then
        export USER_ID=9000;
else
        export USER_ID=$(id -u aaiadmin);
fi;

chown -R $USER_ID:$USER_ID $RESOURCE_LOGS $TRAVERSAL_LOGS;

RESOURCES_CONTAINER_NAME=$($DOCKER_COMPOSE_CMD up -d aai-resources.api.simpledemo.openecomp.org 2>&1 | grep 'Creating' | grep -v 'volume' | grep -v 'network' | awk '{ print $2; }' | head -1);
wait_for_container $RESOURCES_CONTAINER_NAME '0.0.0.0:8447';

GRAPH_CONTAINER_NAME=$($DOCKER_COMPOSE_CMD up -d aai-traversal.api.simpledemo.openecomp.org 2>&1 | grep 'Creating' | awk '{ print $2; }' | head -1);
wait_for_container $GRAPH_CONTAINER_NAME '0.0.0.0:8446';

# Deploy haproxy and traversal and resources
$DOCKER_COMPOSE_CMD up -d aai.api.simpledemo.openecomp.org

$DOCKER_COMPOSE_CMD up -d sparky-be

$DOCKER_COMPOSE_CMD up -d model-loader datarouter aai.searchservice.simpledemo.openecomp.org

echo "A&AI Microservices are successfully started";

crontab < /opt/test-config/aaicrontab
