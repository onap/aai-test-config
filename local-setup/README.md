# Local setup for AAI

### Usage

Build dev images:

```
mvn clean package
```

Run integration tests:

```
mvn clean verify -P integration-test
```

### Start local AAI

1. Janus setup

Modify both `janus-cached.properties` and `janus-realtime.properties` to the following (for all micro-services that will connect to the local Cassandra backend)

```
storage.backend=cassandra
storage.hostname=localhost
storage.cassandra.keyspace=onap # or different keyspace name of your choosing
```

2. Start compose

```
cd src/main/docker
docker-compose up --force-recreate
```