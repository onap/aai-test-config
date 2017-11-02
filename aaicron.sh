#!/bin/bash

RESOURCES_COUNT=$(docker ps | grep 'testconfig_aai-resources.api.simpledemo.openecomp.org_1' | wc -l);

if [ ${RESOURCES_COUNT} -eq 0 ]; then
	docker exec -u aaiadmin testconfig_aai-resources.api.simpledemo.openecomp.org_1 /opt/app/aai-resources/bin/dataSnapshot.sh >> /var/log/aaicron.log 2>&1	
fi;
