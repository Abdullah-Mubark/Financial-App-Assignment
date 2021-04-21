#!/bin/bash
export COMPOSE_PROJECT_NAME="financialapp"

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
docker-compose down -v --remove-orphans
print_title "Cleanup finished"

# Start Kafka
print_title "Start Kafka"
docker-compose -f docker-compose-kafka.yml up -d --build zookeeper kafka kafka2 kafka3 kafdrop 
print_title "Kafka is up"

# Run app
print_title "Run App"
docker-compose up --build financialapp