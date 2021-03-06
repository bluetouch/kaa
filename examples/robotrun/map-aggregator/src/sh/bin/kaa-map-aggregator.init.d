#!/bin/bash
#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# Starts a Kaa Flume Sink
#
# chkconfig: 2345 90 10
# description: Kaa Map Aggregator
#
### BEGIN INIT INFO
# Provides:          kaa-map-aggregator
# Required-Start:    $remote_fs
# Should-Start:
# Required-Stop:     $remote_fs
# Should-Stop:
# Default-Start:     2 3 4 5
# Default-Stop:      0 1 6
# Short-Description: Kaa Map Aggregator
### END INIT INFO

. /lib/lsb/init-functions

if [ -f /etc/default/flume-ng-agent ] ; then
  . /etc/default/flume-ng-agent
fi

# Autodetect JAVA_HOME if not defined
if [ -e /usr/libexec/bigtop-detect-javahome ]; then
  . /usr/libexec/bigtop-detect-javahome
elif [ -e /usr/lib/bigtop-utils/bigtop-detect-javahome ]; then
  . /usr/lib/bigtop-utils/bigtop-detect-javahome
fi

STATUS_RUNNING=0
STATUS_DEAD=1
STATUS_DEAD_AND_LOCK=2
STATUS_NOT_RUNNING=3

ERROR_PROGRAM_NOT_INSTALLED=5

FLUME_LOG_DIR=/var/log/kaa
FLUME_CONF_DIR=/etc/kaa-map-aggregator/conf
FLUME_RUN_DIR=/var/run/kaa-map-aggregator
FLUME_HOME=/usr/lib/flume-ng
FLUME_USER=kaa

FLUME_LOCK_DIR="/var/lock/subsys/"
LOCKFILE="${FLUME_LOCK_DIR}/kaa-map-aggregator"
desc="Kaa Map Aggregator daemon"

FLUME_CONF_FILE=${FLUME_CONF_FILE:-${FLUME_CONF_DIR}/flume.conf}
EXEC_PATH=/usr/bin/flume-ng
FLUME_PID_FILE=${FLUME_RUN_DIR}/kaa-map-aggregator.pid

# These directories may be tmpfs and may or may not exist
# depending on the OS (ex: /var/lock/subsys does not exist on debian/ubuntu)
for dir in "$FLUME_RUN_DIR" "$FLUME_LOCK_DIR"; do
  [ -d "${dir}" ] || install -d -m 0755 -o $FLUME_USER -g $FLUME_USER ${dir}
done


DEFAULT_FLUME_AGENT_NAME="kaa-map-aggregator"
FLUME_AGENT_NAME=${FLUME_AGENT_NAME:-${DEFAULT_FLUME_AGENT_NAME}}
FLUME_SHUTDOWN_TIMEOUT=${FLUME_SHUTDOWN_TIMEOUT:-300}

start() {
  [ -x $exec ] || exit $ERROR_PROGRAM_NOT_INSTALLED

  checkstatus
  status=$?
  if [ "$status" -eq "$STATUS_RUNNING" ]; then
    exit 0
  fi

  log_success_msg "Starting $desc (flume-ng-agent): "
  /bin/su -s /bin/bash -c "/bin/bash -c 'echo \$\$ >${FLUME_PID_FILE} && exec ${EXEC_PATH} agent --conf $FLUME_CONF_DIR --conf-file $FLUME_CONF_FILE --name $FLUME_AGENT_NAME >>${FLUME_LOG_DIR}/kaa-map-aggregator.init.log 2>&1' &" $FLUME_USER
  RETVAL=$?
  [ $RETVAL -eq 0 ] && touch $LOCKFILE
  return $RETVAL
}

stop() {
  if [ ! -e $FLUME_PID_FILE ]; then
    log_failure_msg "Kaa map aggregator is not running"
    exit 0
  fi

  log_success_msg "Stopping $desc (flume-ng-agent): "

  FLUME_PID=`cat $FLUME_PID_FILE`
  if [ -n $FLUME_PID ]; then
    kill -TERM ${FLUME_PID} &>/dev/null
    for i in `seq 1 ${FLUME_SHUTDOWN_TIMEOUT}` ; do
      kill -0 ${FLUME_PID} &>/dev/null || break
      sleep 1
    done
    kill -KILL ${FLUME_PID} &>/dev/null
  fi
  rm -f $LOCKFILE $FLUME_PID_FILE
  return 0
}

restart() {
  stop
  start
}

checkstatus(){
  pidofproc -p $FLUME_PID_FILE java > /dev/null
  status=$?

  case "$status" in
    $STATUS_RUNNING)
      log_success_msg "Kaa map aggregator is running"
      ;;
    $STATUS_DEAD)
      log_failure_msg "Kaa map aggregator is dead and pid file exists"
      ;;
    $STATUS_DEAD_AND_LOCK)
      log_failure_msg "Kaa map aggregator is dead and lock file exists"
      ;;
    $STATUS_NOT_RUNNING)
      log_failure_msg "Kaa map aggregator is not running"
      ;;
    *)
      log_failure_msg "Kaa map aggregator status is unknown"
      ;;
  esac
  return $status
}

condrestart(){
  [ -e ${LOCKFILE} ] && restart || :
}

case "$1" in
  start)
    start
    ;;
  stop)
    stop
    ;;
  status)
    checkstatus
    ;;
  restart)
    restart
    ;;
  condrestart|try-restart)
    condrestart
    ;;
  *)
    echo $"Usage: $0 {start|stop|status|restart|try-restart|condrestart}"
    exit 1
esac

exit $RETVAL
