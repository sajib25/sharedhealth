#!/bin/sh

ln -s /opt/healthId/bin/healthId /etc/init.d/healthId
ln -s /opt/healthId/etc/healthId /etc/default/healthId
ln -s /opt/healthId/var /var/run/healthId

if [ ! -e /var/log/healthId ]; then
    mkdir /var/log/healthId
fi

# Add healthId service to chkconfig
chkconfig --add healthId