#!/bin/sh
nohup java -Dserver.port=$HEALTH_ID_SERVICE_PORT -DHID_LOG_LEVEL=$HID_LOG_LEVEL -jar  /opt/healthid/lib/healthId-api.war > /dev/null 2>&1  &
echo $! > /var/run/healthid/healthid.pid
