#!/bin/sh

DONE_FILE=/root/.iotbox-done

led_blink() {
    echo timer > /sys/class/leds/led0/trigger
    echo 250 > /sys/class/leds/led0/delay_on
    echo 250 > /sys/class/leds/led0/delay_off
}

led_on() {
 echo none > /sys/class/leds/led0/trigger
 echo 1 > /sys/class/leds/led0/brightness
}

if [ -e $DONE_FILE ]
then
    echo "exists $DONE_FILE"
else
    led_blink

    cd /root

    nohup sed -i.bak -e "s%http://mirrordirector.raspbian.org/raspbian/%http://ftp.jaist.ac.jp/raspbian%g" /etc/apt/sources.list

    apt-get update
    apt-get install -y nodejs npm bluetooth libbluetooth-dev libudev-dev
    npm install n -g
    n stable

    npm install -g iotbox-rpi

    touch $DONE_FILE

    led_on
fi

sudo -u pi "nohup iotbox"
