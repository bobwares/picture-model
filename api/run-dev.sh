#!/bin/bash

# Set Java 21 as JAVA_HOME
export JAVA_HOME=$(/usr/libexec/java_home -v 21)

# Run Spring Boot in dev mode (uses H2 database)
mvn spring-boot:run -Dspring-boot.run.profiles=dev
