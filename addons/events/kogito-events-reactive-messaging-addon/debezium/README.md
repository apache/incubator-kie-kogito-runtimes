# References

**Outbox pattern**: https://debezium.io/blog/2019/02/19/reliable-microservices-data-exchange-with-the-outbox-pattern/

**debezium-examples:** https://github.com/debezium/debezium-examples/blob/master/tutorial/README.md#using-mongodb

**debezium-images:** https://github.com/debezium/docker-images/tree/master/examples/mongodb/1.3

# Using MongoDB

```shell
# Build the customized MongoDB image
docker build -f docker/Dockerfile -t debezium/example-mongodb-4.4:1.3 docker

# Deploy MongoDB, Debezium and Kafka
export DEBEZIUM_VERSION=1.3
docker-compose -f docker-compose-mongodb.yaml up

# Initialize MongoDB replica set and insert some test data
docker-compose -f docker-compose-mongodb.yaml exec mongodb bash -c '/usr/local/bin/init-inventory.sh'

# Start MongoDB connector
curl -i -X POST -H "Accept:application/json" -H  "Content-Type:application/json" http://localhost:8083/connectors/ -d @register-mongodb.json

# Access the database via MongoDB client if needed
docker-compose -f docker-compose-mongodb.yaml exec mongodb bash -c 'mongo -u $MONGODB_USER -p $MONGODB_PASSWORD --authenticationDatabase admin inventory'

# Rebuild Kogito runtime if needed
mvn clean install

# Configure Kogito App to use the MongoDB
kogito.persistence.type=mongodb
quarkus.mongodb.connection-string = mongodb://localhost:27017
quarkus.mongodb.credentials.username=debezium
quarkus.mongodb.credentials.password=dbz
quarkus.mongodb.credentials.auth-source=admin
quarkus.mongodb.database=inventory
kogito.events.database=inventory

# Build and run Kogito App and enable persistence and events
mvn clean compile quarkus:dev -Ppersistence,events

# Consume messages from an event topic
docker-compose -f docker-compose-mongodb.yaml exec kafka /kafka/bin/kafka-console-consumer.sh \
    --bootstrap-server kafka:9092 \
    --from-beginning \
    --property print.key=false \
    --topic kogito-processinstances-events

# Shut down the cluster
docker-compose -f docker-compose-mongodb.yaml down
```