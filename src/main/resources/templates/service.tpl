#!/bin/sh
### BEGIN INIT INFO
# Provides:          ${meta.code}
# Required-Start:    $all
# Required-Stop:     $all
# Default-Start:     2 3 4 5
# Default-Stop:      0 1 6
# Short-Description: starts ${meta.code}
# Description:       starts ${meta.code}
### END INIT INFO


SERVICE_NAME="${meta.code}"
PATH_TO_JAR="${root}/jar/${meta.code}"
MEM="${meta.memory!'100m'}"
VERSION="${meta.version!''}"
ARGS="${meta.args!''}"
ENV="${meta.env!'dev'}"

if [[ "$2" != "" ]]; then
    VERSION="$2"
    echo "Using passed param $2"
fi

LOG_FILE="${root}/logs/$SERVICE_NAME/$VERSION.log"
JAR="$SERVICE_NAME-$VERSION.jar"

PID_PATH_NAME="/var/run/$SERVICE_NAME$VERSION.pid"
JAVA="$SOA_JAVA_HOME/bin/java"

# Initialize stop wait time if not provided by the config file
[[ -z "$STOP_WAIT_TIME" ]] && STOP_WAIT_TIME="{{stopWaitTime:60}}"

# ANSI Colors
echoRed() { echo $'\e[0;31m'"$1"$'\e[0m'; }
echoGreen() { echo $'\e[0;32m'"$1"$'\e[0m'; }
echoYellow() { echo $'\e[0;33m'"$1"$'\e[0m'; }

# functions

isRunning() {
  ps -p "$1" &> /dev/null
}

start() {
  if [[ -f "$PID_PATH_NAME" ]]; then
    pid=$(cat "$PID_PATH_NAME")
    isRunning "$pid" && { echoYellow "Already running [$pid]"; return 0; }
  fi
  echo "Starting $SERVICE_NAME ..."
  cd $SOA_JAVA_HOME
  nohup $JAVA $ARGS -Xms$MEM -Xmx$MEM -DSL_ENV=$ENV -jar $PATH_TO_JAR/$JAR >> $LOG_FILE 2>&1 &
  pgrep -u root -f $JAR > $PID_PATH_NAME
  echo "$SERVICE_NAME started ..."
}

stop() {
  [[ -f $PID_PATH_NAME ]] || { echoYellow "Not running (pidfile not found)"; return 0; }
  pid=$(cat "$PID_PATH_NAME")
  isRunning "$pid" || { echoYellow "Not running (process $SERVICE_NAME). Removing stale pid file."; rm -f "$PID_PATH_NAME"; return 0; }
  do_stop "$pid" "$PID_PATH_NAME"
}

do_stop() {
  kill "$1" &> /dev/null || { echoRed "Unable to kill process $1"; return 1; }
  for i in $(seq 1 $STOP_WAIT_TIME); do
    isRunning "$1" || { echoGreen "Stopped [$1]"; rm -f "$2"; return 0; }
    [[ $i -eq STOP_WAIT_TIME/2 ]] && kill "$1" &> /dev/null
    sleep 1
  done
  echoRed "Unable to kill process $1";
  return 1;
}

force_stop() {
  [[ -f $PID_PATH_NAME ]] || { echoYellow "Not running (pidfile not found)"; return 0; }
  pid=$(cat "$PID_PATH_NAME")
  isRunning "$pid" || { echoYellow "Not running (process ${r"${pid}"}). Removing stale pid file."; rm -f "$PID_PATH_NAME"; return 0; }
  do_force_stop "$pid" "$PID_PATH_NAME"
}

do_force_stop() {
  kill -9 "$1" &> /dev/null || { echoRed "Unable to kill process $1"; return 1; }
  for i in $(seq 1 $STOP_WAIT_TIME); do
    isRunning "$1" || { echoGreen "Stopped [$1]"; rm -f "$2"; return 0; }
    [[ $i -eq STOP_WAIT_TIME/2 ]] && kill -9 "$1" &> /dev/null
    sleep 1
  done
  echoRed "Unable to kill process $1";
  return 1;
}

restart() {
  stop && start
}

# BEGIN

case $1 in
    start)
        start
    ;;
    stop)
        echo "$SERVICE_NAME stoping ..."
        stop
    ;;
    force-stop)
        force_stop
    ;;
    restart)
        restart
    ;;
    *)
        echo "Usage: $0 {start|stop|force-stop|restart}"; exit 1;
esac