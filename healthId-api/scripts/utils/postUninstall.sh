#!/bin/sh

rm -f /etc/init.d/healthId
rm -f /etc/default/healthId
rm -f /var/run/healthId

#Remove healthId from chkconfig
chkconfig --del healthId || true