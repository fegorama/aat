#!/bin/bash

export JAVA_HOME="/usr/local/java/jdk1.8.0_112"
export MAVEN_OPTS="-Xms256m -Xmx2G -XXaltjvm=dcevm -javaagent:/home/mikel/opt/hotswap-agent.jar"
export MAVEN_OPTS="$MAVEN_OPTS -agentlib:jdwp=transport=dt_socket,address=9999,server=y,suspend=n"
echo "The environment variable 'MAVEN_OPTS' is not set, setting it for you";
mvn clean install alfresco:run
