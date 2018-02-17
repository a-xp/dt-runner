#!/bin/sh

ROOT="${root}"
SERVICE_NAME="${meta.code}"
PATH_TO_JAR="$ROOT/jar/${meta.code}"
MEM="${meta.memory}"
VERSION="${meta.version!''}"
ARGS="${meta.args!''}"
ENV="${meta.env!'dev'}"

if [[ "$1" != "" ]]; then
    VERSION="$1"
fi

LOG_FILE="$ROOT/logs/$SERVICE_NAME/$VERSION.log"
JAR="$SERVICE_NAME-$VERSION.jar"
JAVA="$SOA_JAVA_HOME/bin/java"

nohup $JAVA -jar -DSL_ENV=$ENV $ARGS -Xmx$MEM $PATH_TO_JAR/$JAR >> $LOG_FILE 2>&1 &