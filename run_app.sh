#!/bin/bash
export COMPOSE_PROJECT_NAME="fa"

docker info > /dev/null 2>&1
if [ $? -ne 0 ]; then
  echo "Unable to talk to the docker daemon"
  echo "Make sure docker is running"
  exit 3
fi

# functions
print_title(){
    echo ""
    echo "----------------------------------------"
    tput setaf 2; echo $1; tput sgr0 
    echo "----------------------------------------"
    echo ""
}

# Cleanup 
print_title "Start cleanup"
docker-compose -f docker-compose-base.yml -f docker-compose-mysql.yml -f docker-compose-kafka.yml down -v --remove-orphans
print_title "Cleanup finished"

# Start MySql
print_title "Start MySql cluster"
docker-compose -f docker-compose-base.yml -f docker-compose-mysql.yml up -d ndb_mgmd ndbd1 ndbd2 mysql
print_title "MySql cluster is up"

# Start Kafka
print_title "Start Kafka cluster"
docker-compose -f docker-compose-base.yml -f docker-compose-kafka.yml up -d zookeeper kafka kafka2 kafka3 kafdrop 
print_title "Kafka cluster is up"

# Run app
print_title "Run App"
docker-compose -f docker-compose-base.yml up --build financialapp