#!/bin/bash

if [ -f "/opt/docker/docker-compose" ];
then
    DOCKER_COMPOSE_CMD="/opt/docker/docker-compose -f docker-compose-app.yml"
else
    DOCKER_COMPOSE_CMD="docker-compose -f docker-compose-app.yml"
fi

export RESOURCES_LOGS="/opt/aai/logroot/AAI-RESOURCES";
export TRAVERSAL_LOGS="/opt/aai/logroot/AAI-TRAVERSAL";
export GRAPHADMIN_LOGS="/opt/aai/logroot/AAI-GRAPHADMIN";
export SEARCH_LOGS="/opt/aai/logroot/AAI-SEARCH";
export DATA_ROUTER_LOGS="/opt/aai/logroot/AAI-DATA-ROUTER";
export MODEL_LOADER_LOGS="/opt/aai/logroot/AAI-MODEL-LOADER";
export UI_LOGS="/opt/aai/logroot/AAI-UI";
export CHAMP_LOGS="/opt/aai/logroot/AAI-CHAMP-SERVICE";
export CRUD_LOGS="/opt/aai/logroot/AAI-CRUD-SERVICE";
export SPIKE_LOGS="/opt/aai/logroot/AAI-SPIKE-SERVICE";
export BABEL_LOGS="/opt/aai/logroot/AAI-BAS";

if [ ! -d "$GRAPHADMIN_LOGS" ];
then
    echo "Warning: Unable to find the volume directory $GRAPHADMIN_LOGS so creating it as regular directory";
    mkdir -p $GRAPHADMIN_LOGS;
fi;

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

if [ ! -d "$CHAMP_LOGS" ];
then
    echo "Warning: Unable to find the volume directory $CHAMP_LOGS so creating it as regular directory";
    mkdir -p $CHAMP_LOGS;
fi;

if [ ! -d "$CRUD_LOGS" ];
then
    echo "Warning: Unable to find the volume directory $CRUD_LOGS so creating it as regular directory";
    mkdir -p $CRUD_LOGS;
fi;

if [ ! -d "$SPIKE_LOGS" ];
then
    echo "Warning: Unable to find the volume directory $SPIKE_LOGS so creating it as regular directory";
    mkdir -p $SPIKE_LOGS;
fi;

if [ ! -d "$BABEL_LOGS" ];
then
    echo "Warning: Unable to find the volume directory $BABEL_LOGS so creating it as regular directory";
    mkdir -p $BABEL_LOGS;
fi;

export MTU=$(/sbin/ifconfig | grep MTU | sed 's/.*MTU://' | sed 's/ .*//' | sort -n | head -1);
export DOCKER_REGISTRY="${DOCKER_REGISTRY:-localhost:5000}";
export AAI_HAPROXY_IMAGE="${AAI_HAPROXY_IMAGE:-aaionap/haproxy}";
export AAI_HAPROXY_VERSION="${AAI_HAPROXY_VERSION:-1.2.3}";

NEXUS_USERNAME=$(cat /opt/config/nexus_username.txt)
NEXUS_PASSWD=$(cat /opt/config/nexus_password.txt)
NEXUS_DOCKER_REPO=$(cat /opt/config/nexus_docker_repo.txt)
DMAAP_TOPIC=$(cat /opt/config/dmaap_topic.txt)
DOCKER_IMAGE_VERSION=$(cat /opt/config/docker_version.txt)
DOCKER_REGISTRY=${NEXUS_DOCKER_REPO}

RESOURCES_DOCKER_IMAGE_VERSION=1.3-STAGING-latest
TRAVERSAL_DOCKER_IMAGE_VERSION=1.3-STAGING-latest
GRAPHADMIN_DOCKER_IMAGE_VERSION=1.0-STAGING-latest
SEARCH_DATA_SERVICE_DOCKER_IMAGE_VERSION=1.3-STAGING-latest
DATA_ROUTER_DOCKER_IMAGE_VERSION=1.2.2
MODEL_LOADER_DOCKER_IMAGE_VERSION=1.3-STAGING-latest
SPARKY_BE_DOCKER_IMAGE_VERSION=1.2.1
CHAMP_DOCKER_IMAGE_VERSION=1.3-STAGING-latest
GIZMO_DOCKER_IMAGE_VERSION=1.3-STAGING-latest
SPIKE_DOCKER_IMAGE_VERSION=1.0-STAGING-latest
BABEL_DOCKER_IMAGE_VERSION=1.3-STAGING-latest

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

docker pull ${DOCKER_REGISTRY}/onap/aai-graphadmin:${GRAPHADMIN_DOCKER_IMAGE_VERSION};
docker tag $DOCKER_REGISTRY/onap/aai-graphadmin:${GRAPHADMIN_DOCKER_IMAGE_VERSION} $DOCKER_REGISTRY/onap/aai-graphadmin:latest;

docker pull ${DOCKER_REGISTRY}/onap/aai-resources:${RESOURCES_DOCKER_IMAGE_VERSION};
docker tag $DOCKER_REGISTRY/onap/aai-resources:$RESOURCES_DOCKER_IMAGE_VERSION $DOCKER_REGISTRY/onap/aai-resources:latest;

docker pull ${DOCKER_REGISTRY}/onap/aai-traversal:${TRAVERSAL_DOCKER_IMAGE_VERSION};
docker tag $DOCKER_REGISTRY/onap/aai-traversal:$TRAVERSAL_DOCKER_IMAGE_VERSION $DOCKER_REGISTRY/onap/aai-traversal:latest;

docker pull ${DOCKER_REGISTRY}/onap/search-data-service:${SEARCH_DATA_SERVICE_DOCKER_IMAGE_VERSION};
docker tag $DOCKER_REGISTRY/onap/search-data-service:$SEARCH_DATA_SERVICE_DOCKER_IMAGE_VERSION $DOCKER_REGISTRY/onap/search-data-service:latest;

docker pull ${DOCKER_REGISTRY}/onap/data-router:${DATA_ROUTER_DOCKER_IMAGE_VERSION};
docker tag $DOCKER_REGISTRY/onap/data-router:$DATA_ROUTER_DOCKER_IMAGE_VERSION $DOCKER_REGISTRY/onap/data-router:latest;

docker pull ${DOCKER_REGISTRY}/onap/model-loader:${MODEL_LOADER_DOCKER_IMAGE_VERSION};
docker tag $DOCKER_REGISTRY/onap/model-loader:$MODEL_LOADER_DOCKER_IMAGE_VERSION $DOCKER_REGISTRY/onap/model-loader:latest;

docker pull ${DOCKER_REGISTRY}/onap/sparky-be:${SPARKY_BE_DOCKER_IMAGE_VERSION};
docker tag $DOCKER_REGISTRY/onap/sparky-be:$SPARKY_BE_DOCKER_IMAGE_VERSION $DOCKER_REGISTRY/onap/sparky-be:latest;

docker pull ${DOCKER_REGISTRY}/onap/champ:${CHAMP_DOCKER_IMAGE_VERSION};
docker tag $DOCKER_REGISTRY/onap/champ:$CHAMP_DOCKER_IMAGE_VERSION $DOCKER_REGISTRY/onap/champ:latest;

docker pull ${DOCKER_REGISTRY}/onap/gizmo:${GIZMO_DOCKER_IMAGE_VERSION};
docker tag $DOCKER_REGISTRY/onap/gizmo:$GIZMO_DOCKER_IMAGE_VERSION $DOCKER_REGISTRY/onap/gizmo:latest;

docker pull ${DOCKER_REGISTRY}/onap/spike:${SPIKE_DOCKER_IMAGE_VERSION};
docker tag $DOCKER_REGISTRY/onap/spike:$SPIKE_DOCKER_IMAGE_VERSION $DOCKER_REGISTRY/onap/spike:latest;

docker pull ${DOCKER_REGISTRY}/onap/babel:${BABEL_DOCKER_IMAGE_VERSION};
docker tag $DOCKER_REGISTRY/onap/babel:$BABEL_DOCKER_IMAGE_VERSION $DOCKER_REGISTRY/onap/babel:latest;

# cleanup
$DOCKER_COMPOSE_CMD stop
$DOCKER_COMPOSE_CMD rm -f -v


USER_EXISTS=$(check_if_user_exists aaiadmin);

if [ "${USER_EXISTS}" -eq 0 ]; then
        export USER_ID=9000;
        export GROUP_ID=9000;
else
        export USER_ID=$(id -u aaiadmin);
        export GROUP_ID=$(id -g aaiadmin);
fi;

chown -R $USER_ID:$USER_ID $RESOURCE_LOGS $TRAVERSAL_LOGS;
chown -R 341790:492381 $BABEL_LOGS;

$DOCKER_COMPOSE_CMD run --rm aai-graphadmin.api.simpledemo.onap.org createDBSchema.sh

GRAPHADMIN_CONTAINER_NAME=$($DOCKER_COMPOSE_CMD up -d aai-graphadmin.api.simpledemo.onap.org 2>&1 | grep 'Creating' | grep -v 'volume' | grep -v 'network' | awk '{ print $2; }' | head -1);
wait_for_container $GRAPHADMIN_CONTAINER_NAME 'GraphAdmin Microservice Started';

# Deploy haproxy and traversal at the same time for traversal to make updateQuery against resources using haproxy

RESOURCES_CONTAINER_NAME=$($DOCKER_COMPOSE_CMD up -d aai-resources.api.simpledemo.onap.org 2>&1 | grep 'Creating' | grep -v 'volume' | grep -v 'network' | awk '{ print $2; }' | head -1);
wait_for_container $RESOURCES_CONTAINER_NAME 'Resources Microservice Started';

$DOCKER_COMPOSE_CMD up -d aai-traversal.api.simpledemo.onap.org aai.api.simpledemo.onap.org

sleep 3;

$DOCKER_COMPOSE_CMD run --rm aai-traversal.api.simpledemo.onap.org install/updateQueryData.sh

$DOCKER_COMPOSE_CMD up -d sparky-be

$DOCKER_COMPOSE_CMD up -d model-loader datarouter aai.searchservice.simpledemo.openecomp.org

$DOCKER_COMPOSE_CMD up -d champ-service

$DOCKER_COMPOSE_CMD up -d crud-service

$DOCKER_COMPOSE_CMD up -d spike-service

$DOCKER_COMPOSE_CMD up -d babel

echo "A&AI Microservices are successfully started";
