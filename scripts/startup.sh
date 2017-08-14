#!/bin/sh

DONE_FILE=/root/.iotbox-done

if [ -e $DONE_FILE ]
then
    echo "exists $DONE_FILE"
else
    nohup sed -i.bak -e "s%http://mirrordirector.raspbian.org/raspbian/%http://ftp.jaist.ac.jp/raspbian%g" /etc/apt/sources.list

    apt-get update
    apt-get install -y nodejs npm bluetooth libbluetooth-dev libudev-dev
    npm install n -g
    n stable

    npm install -g iotbox-rpi

    touch $DONE_FILE
fi

sudo -u pi "nohup iotbox-rpi"
